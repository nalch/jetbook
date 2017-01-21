# jetbook ![Build status](https://travis-ci.org/nalch/jetbook.svg?branch=master)
A simple Java-Script to generate a printable PDF, that is compatible with the [Rocketbook](https://getrocketbook.com/) app.
Of course, you will not be able to erase your notes with water, but the erasing properties of the usual [Frixion pens](http://www.frixion-shop.com/pilot-frixion-ball.html) is sufficient for more than enough repetitions.
So simply buy some Frixion ball pens, generate some pages and bind them (or let them bind online) into a notebook to your liking.

# Images
![Original Rocketbook](https://raw.githubusercontent.com/nalch/jetbook/master/images/original.jpg) ![Image processing](https://raw.githubusercontent.com/nalch/jetbook/master/images/beforeafter_phones.jpg) ![Individual template](https://raw.githubusercontent.com/nalch/jetbook/master/images/nalch-logo-result.png)


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
| argument      | cli arg       | documentation                                                                    | default value  |
|---------------|---------------|----------------------------------------------------------------------------------|----------------|
| pagecount     | -n            | how many pages the resulting document should have                                | 1              |
| inputtemplate | -i            | choose an available template. Currently: nalch-default, nalch-logo               | nalch-default  |
| outputfile    | -o            | choose the resulting file (without the ending, that is automatically set to pdf) | results/result |