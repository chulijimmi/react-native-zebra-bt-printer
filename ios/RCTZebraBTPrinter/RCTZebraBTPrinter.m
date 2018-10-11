//
//  RCTZebraBTPrinter.m
//  RCTZebraBTPrinter
//
//  Created by Jakub Martyčák on 17.04.16.
//  Copyright © 2016 Jakub Martyčák. All rights reserved.
//

#import "RCTZebraBTPrinter.h"

//ZEBRA
#import "ZebraPrinterConnection.h"
#import "ZebraPrinter.h"
#import "ZebraPrinterFactory.h"
#import "MfiBtPrinterConnection.h"
#import <SGD.h>

@interface RCTZebraBTPrinter ()



@end


@implementation RCTZebraBTPrinter

RCT_EXPORT_MODULE();

- (dispatch_queue_t)methodQueue
{
    // run all module methods in main thread
    // if we don't no timer callbacks got called
    return dispatch_get_main_queue();
}

#pragma mark - Methods available form Javascript

RCT_EXPORT_METHOD(
    printLabel: (NSString *)userPrinterSerial
    userCommand:(NSString *)userCommand
    resolve: (RCTPromiseResolveBlock)resolve
    rejector:(RCTPromiseRejectBlock)reject){

    //userPrinterSerial = userPrinterSerial;

    NSLog(@"IOS >> printLabel triggered");

    //NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];

    NSLog(@"IOS >> Connecting");

    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^ {

        id<ZebraPrinterConnection, NSObject> thePrinterConn = [[MfiBtPrinterConnection alloc] initWithSerialNumber:userPrinterSerial];

        [((MfiBtPrinterConnection*)thePrinterConn) setTimeToWaitAfterWriteInMilliseconds:30];

        BOOL success = [thePrinterConn open];

        if(success == YES){

//          NSLog(@"IOS >> Connected %@", userText1);

//          NSString *testLabel = @"^XA^FO100,60^A0N,25,25^FB400,2,10,C,0^FDAlex Kuzmenya. Alex Kuzmenya. long ling 231^FS^XZ";

            NSString *printLabel;
            // A label file always begins with the “!” character followed by an “x” offset parameter, “x” and “y” axis resolutions, a label length and finally a quantity of labels to print.

            printLabel = [NSString stringWithFormat: @"! %@", userCommand];

//          NSString *testLabel = @"! 0 200 200 210 1\r\nTEXT 4 0 30 40 This is a CPCL test.\r\nFORM\r\nPRINT\r\n";

            NSError *error = nil;

            // Send the data to printer as a byte array.
            // NSData *data = [NSData dataWithBytes:[testLabel UTF8String] length:[testLabel length]];

            success = success && [thePrinterConn write:[printLabel dataUsingEncoding:NSUTF8StringEncoding] error:&error];

            NSLog(@"IOS >> Sending Data");

            dispatch_async(dispatch_get_main_queue(), ^{
                if (success != YES || error != nil) {

                    NSLog(@"IOS >> Failed to send");

                    UIAlertView *errorAlert = [[UIAlertView alloc] initWithTitle:@"Error" message:[error localizedDescription] delegate:nil cancelButtonTitle:@"Ok" otherButtonTitles:nil];
                    [errorAlert show];
                    //[errorAlert release];
                }
            });
            // Close the connection to release resources.
            [thePrinterConn close];
            //[thePrinterConn release];
            resolve((id)kCFBooleanTrue);
        } else {

            NSLog(@"IOS >> Failed to connect");
            resolve((id)kCFBooleanFalse);

        }
    });

    /*
     id<ZebraPrinterConnection, NSObject> connection = nil;

     NSString *printerSerial = @"XXQPJ171800079";

     NSLog(@"SERIAL## IS %@", printerSerial);


     connection = [[MfiBtPrinterConnection alloc] initWithSerialNumber:printerSerial];

     [((MfiBtPrinterConnection*)connection) setTimeToWaitAfterWriteInMilliseconds:80];

     BOOL didOpen = [connection open];

     if(didOpen == YES){



     NSLog(@"IOS >> Connected");

     NSLog(@"IOS >> Determining Printer Language...");

     NSError *error;

     id<ZebraPrinter,NSObject> printer = [ZebraPrinterFactory getInstance:connection error:&error];

     PrinterLanguage language = [printer getPrinterControlLanguage];

     NSLog(@"IOS >> Printer Language %@",[self getLanguageName:language]);

     NSLog(@"IOS >> Sending Data");


     //Construct msg
     NSString *testLabel;

     NSString *userText = @"hahaha";

     NSLog(@"USER INPUT ## IS %@", userText);

     testLabel = [NSString stringWithFormat:@"! 0 200 200 210 1\r\nTEXT 4 0 30 40 Hello %@\r\nFORM\r\nPRINT\r\n", userText];

     NSData *data = [NSData dataWithBytes:[testLabel UTF8String] length:[testLabel length]];
     [connection write:data error:&error];

     NSLog(@"%@",error);

     //BOOL sentOK = [self printTestLabel:language onConnection:connection withError:&error];
     BOOL sentOK = 1;

     if (sentOK == 1) {
     NSLog(@"IOS >> Test Label Sent");

     } else {
     NSLog(@"IOS >> Test Label Failed to Print");

     }

     NSLog(@"IOS >> Disconnecting");

     [connection close];

     } else {
     NSLog(@"IOS >> Connection not open");
     }

     */

}


RCT_EXPORT_METHOD(checkPrinterStatus: (NSString *)serialCode
                            resolver: (RCTPromiseResolveBlock)resolve
                            rejector: (RCTPromiseRejectBlock)reject) {
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^ {
        id<ZebraPrinterConnection, NSObject> connection = [[MfiBtPrinterConnection alloc] initWithSerialNumber:serialCode];
        [((MfiBtPrinterConnection*)connection) setTimeToWaitAfterWriteInMilliseconds:80];
        BOOL success = [connection open];
        if (success) {
            NSError *error = nil;
            [SGD SET:@"device.languages" withValue:@"zpl" andWithPrinterConnection:connection error:&error];
            [SGD SET:@"ezpl.media_type" withValue:@"continuous" andWithPrinterConnection:connection error:&error];
            [SGD SET:@"zpl.label_length" withValue:@"100" andWithPrinterConnection:connection error:&error];
            if (error) {
                NSLog(@"asssddd %@", error.localizedDescription);
                resolve((id)kCFBooleanFalse);
                return;
            }
        }
        if (success) {
            NSError *error = nil;
            id<ZebraPrinter, NSObject> printer = [ZebraPrinterFactory getInstance:connection error:&error];
            if (error) {
                NSLog(@"%@", error.localizedDescription);
                [connection close];
                resolve((id)kCFBooleanFalse);
                return;
            }

            PrinterStatus *status = [printer getCurrentStatus:&error];
            if (error) {
                NSLog(@"wtf %@", error.localizedDescription);
                [connection close];
                resolve((id)kCFBooleanFalse);
                return;
            }

            NSLog(@"Is printer ready to print: %d", (int)status.isReadyToPrint);
            [connection close];
            resolve(status.isReadyToPrint ? (id)kCFBooleanTrue : (id)kCFBooleanFalse);
        } else {
            [connection close];
            resolve((id)kCFBooleanFalse);
            NSLog(@"Failed to connect to printer");
        }
    });
}

@end
