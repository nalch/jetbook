# jetbook ![Build status](https://travis-ci.org/nalch/jetbook.svg?branch=master)
A simple Java-Script to generate a printable PDF, that is compatible with the Rocketbook app

# Usage
Execute the jar with: 
```
java -jar jetbook-0.1.jar -n <pagecount> -i <inputtemplate> -o <outputfile>
```

For example:
```
java -jar jetbook-0.1.jar -n 98 -i nalch-default -o /home/nalch/jetbook
```

The arguments are:
| argument | cli arg | documentation | default value |
| ------------- | ------------- | - | - |
| pagecount | -i | how many pages the resulting document should have | 1 |
| inputtemplate | -i | choose an available template. Currently: nalch-default, nalch-logo | nalch-default |
| outputfile | -o | choose the resulting file (without the ending, that is automatically set to pdf) | results/result |