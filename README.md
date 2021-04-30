# csv-converter
A simple CSV manipulator using FS2 Streams. This takes a CSV input containing FAQ ID and Product SKUs in 2 columns
and outputs a CSV containing a FAQ ID - PRODUCT SKU pair per row.

## Build

The application is packaged with its dependencies using the `sbt-native-packager` plugin. To build
the archive run the following command:

```
esbt universal:packageBin
```

This will create an `faqconverter-<version>.zip` file in the `target/universal/` directory.

If you just want a local copy to test on your system, not a ZIP file, you can run the following
command that will generate the same contents as the ZIP, but unpacked and located in the
`target/universal/stage/` directory.

```
esbt stage
```

## Usage Examples
Unzipping the archive will create a directory named `faqconverter-<version>`.
```
.
├── bin
│   ├── faqconverter
│   └── faqconverter.bat
└── lib
    ├── co.fs2.fs2-core_2.12-2.5.3.jar
    ├── co.fs2.fs2-io_2.12-2.5.3.jar
    ├── com.monovore.decline-effect_2.12-1.3.0.jar
    ├── com.monovore.decline_2.12-1.3.0.jar
    ├── faqconverter.faqconverter-0.1.jar
    ├── org.scala-lang.scala-library-2.12.13.jar
    ├── org.scodec.scodec-bits_2.12-1.1.24.jar
    ├── org.typelevel.cats-core_2.12-2.5.0.jar
    ├── org.typelevel.cats-effect_2.12-2.4.1.jar
    ├── org.typelevel.cats-kernel_2.12-2.5.0.jar
    └── org.typelevel.simulacrum-scalafix-annotations_2.12-0.5.4.jar
```

- View the usage details of the utility:
  ```
  faqconverter-<version>/bin/faqconverter --help
  ```

- Convert the FAQ CSV file into a the CSV file format
  ```
  faqconverter-<version>/bin/faqconverter
    --in inputfile.csv
    --out outputfile.csv
  ```