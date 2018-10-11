
# react-native-zebra-bt-printer

This library is built for react native to work with Portable Zebra Bluetooth Printers. This library uses the libraries provided by Zebra. It has been tested with the QLN220, but should work with other QLN series printers that have bluetooth (non BLE mode).

## Getting started

`$ npm install react-native-zebra-bt-printer --save`

### Mostly automatic installation

`$ react-native link react-native-zebra-bt-printer`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-zebra-bt-printer` and add `RCTZebraBTPrinter.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRCTZebraBTPrinter.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.cyclelution.RCTZebraBTPrinter.RCTZebraBTPrinterPackage;` to the imports at the top of the file
  - Add `new RCTZebraBTPrinterPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
    include ':react-native-zebra-bt-printer'
    project(':react-native-zebra-bt-printer').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-zebra-bt-printer/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
    implementation project(':react-native-zebra-bt-printer')
  	```


## Usage

API:

| Method        | Description   |
| ------------- | ------------- |
| `printLabel(printerSerial, userCommand)`  | Prints data on zebra bt printer. `userCommand` is presented in CPCL printer programming language. Returns a promise with result  |
| `checkPrinterStatus(printerSerial)`  | Checks if printer is ready to print. Returns a promise with result  |

You must pair your printer first with the device.

iOS requires the printer serial#.

Android requires the MAC ADDRESS.

```javascript
import React, { Component } from 'react';

import {
	AsyncStorage,
	Text,
	TextInput,
	TouchableOpacity,
	StyleSheet,
	Alert,
	View
} from 'react-native';

import ZebraBTPrinter from 'react-native-zebra-bt-printer';

const printLabel = async(userPrintCount, userText1, userText2, userText3) => {

	console.log('printLabel APP');

	// if (userText1 === '') {
	// 	Alert.alert('Your label seems to be missing content!');
	// 	return false;
	// }

	try {

		//Store your printer serial or mac, ios needs serial, android needs mac
		const printerSerial = String('CC78AB7A48BA');

		//check if printer is set
		if (printerSerial !== null && printerSerial !== '') {

      console.log('printerSerial', printerSerial);
			ZebraBTPrinter.printLabel(printerSerial, userPrintCount, userText1, userText2, userText3).then((result) => {

					console.log(result);

					if (result === false) {
						Alert.alert('Print failed, please check printer connection');
					}

				})
				.catch((err) => console.log(err.message));

		} else {

			Alert.alert('Print failed, no printer setup found');

		}

	} catch (error) {
		// Error retrieving data
		console.log('Async getItem failed');
	}

}

class App extends Component {

	constructor() {
		super();

		this.state = {
			userText1: 'testing',
			userText2: 'hello',
			userText3: 'word',
			userPrintCount: '1'
		};

	}

	componentDidMount() {

	}

	render() {

		return (
			<View style={styles.container}>
				<Text style={styles.welcome}>
					Welcome to React Native Zebra Printer Example!
				</Text>
				<TouchableOpacity
				style={{borderWidth:1, borderColor:'grey', padding:3, width:100, backgroundColor:'white'}}
				onPress={ () => {
					printLabel(this.state.userPrintCount, this.state.userText1, this.state.userText2, this.state.userText3);
				} }>
					<Text>Print!</Text>
				</TouchableOpacity>
			</View>
		);

	}

}

const styles = StyleSheet.create({
	container: {
		flex: 1,
		justifyContent: 'center',
		alignItems: 'center',
		paddingBottom: 30,
		backgroundColor: '#F5FCFF',
	},
	welcome: {
		fontSize: 20,
		textAlign: 'center',
		margin: 10,
	},
	instructions: {
		textAlign: 'center',
		color: '#333333',
		marginBottom: 5,
	},
});

export default App;

```
