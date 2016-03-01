/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package imagenameformatchanger;

import java.io.File;
import java.io.FilenameFilter;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 *
 * @author Alp Sayin
 */
public class ImageNameFormatChanger {

    /**
     * @param args the command line arguments
     */
    private static final int YEAR_LENGTH = 4;
    private static final int MONTH_LENGTH = 2;
    private static final int DATE_LENGTH = 2;
    private static final int HOUR_LENGTH = 2;
    private static final int MINUTE_LENGTH = 2;
    private static final int SECOND_LENGTH = 2;
    
    private static final int YEAR_INDEX = 4; //IMG_20160301-154722.jpg -> 2016-03-01 15.47.22.jpg
    private static final int MONTH_INDEX = YEAR_INDEX+YEAR_LENGTH;
    private static final int DATE_INDEX = MONTH_INDEX+MONTH_LENGTH;
    private static final int HOUR_INDEX = DATE_INDEX+DATE_LENGTH+1;
    private static final int MINUTE_INDEX = HOUR_INDEX+HOUR_LENGTH;
    private static final int SECOND_INDEX = MINUTE_INDEX+MINUTE_LENGTH;
    
    private final File photoFolder;
    private final PrintStream ps;
    private final LinkedHashMap<File, File> fileMappings;
    public ImageNameFormatChanger(File photoFolder)
    {
        this.photoFolder = photoFolder;
        this.ps = System.out;
        fileMappings = new LinkedHashMap<File, File>();
    }
    public ImageNameFormatChanger(File photoFolder, OutputStream os)
    {
        this.photoFolder = photoFolder;
        this.ps = new PrintStream(os);
        fileMappings = new LinkedHashMap<File, File>();
    }
    public boolean changeFilenames(boolean commitChanges)
    {
        ps.println("ChangeFilenames Started: commitChanges="+commitChanges);
        FilenameFilter IMG_filter = new FilenameFilter() 
        {
            @Override
            public boolean accept(File dir, String name) 
            {
                return (name.contains("IMG") && name.length()>=19);
            }
        };
        int initialNumOfPhotoFiles = photoFolder.listFiles(IMG_filter).length;
        for(File photoFile : photoFolder.listFiles(IMG_filter))
        {
            String filename = photoFile.getName();
            String yearString = filename.substring(YEAR_INDEX, YEAR_INDEX+YEAR_LENGTH);
            String monthString = filename.substring(MONTH_INDEX, MONTH_INDEX+MONTH_LENGTH);
            String dateString = filename.substring(DATE_INDEX, DATE_INDEX+DATE_LENGTH);
            String hourString = filename.substring(HOUR_INDEX, HOUR_INDEX+HOUR_LENGTH);
            String minuteString = filename.substring(MINUTE_INDEX, MINUTE_INDEX+MINUTE_LENGTH);
            String secondString = filename.substring(SECOND_INDEX, SECOND_INDEX+SECOND_LENGTH);
            String extensionString = filename.substring(filename.length()-4);
            //ps.println(yearString+"/"+monthString+"/"+dateString+" "+hourString+":"+minuteString+":"+secondString);
            
            String newFilename = yearString+"-"+monthString+"-"+dateString+" "+hourString+"."+minuteString+"."+secondString+extensionString;
            File renamedPhotoFile = new File(photoFile.getParent()+File.separator+newFilename);
            int copyCount = 1;
            while(renamedPhotoFile.exists())
            {
                newFilename = newFilename.substring(0, newFilename.length()-4) + " ("+copyCount+")"+extensionString;
                renamedPhotoFile = new File(photoFile.getParent()+File.separator+newFilename);
                copyCount++;
            }
            //ps.println(renamedPhotoFile.getAbsolutePath());
            if(commitChanges)
            {
                if(!photoFile.renameTo(renamedPhotoFile))
                {
                    ps.println("Renaming of \""+photoFile+"\" to \""+newFilename+"\" failed.");
                }
                else
                {
                    ps.println("Successful renaming of \""+photoFile+"\" to \""+newFilename+"\".");
                    fileMappings.put(renamedPhotoFile, photoFile);
                }
            }
            else
            {
                ps.println("\""+photoFile+"\" to \""+newFilename+"\".");
            }
        }
        ps.println("ChangeFilenames Finished: commitChanges="+commitChanges);
        if(commitChanges && fileMappings.size()!=initialNumOfPhotoFiles)
        {
            ps.println("You can try to Do It again to try and rename the remaining files.");
        }
        return !fileMappings.isEmpty();
    }
    public boolean undo(boolean commitChanges)
    {
        if(fileMappings.size() == 0)
            return false;
        
        ps.println("Undo Started: commitChanges="+commitChanges);
        ArrayList<File> successfulRenames = new ArrayList<File>();
        for( File renamedPhotoFile : fileMappings.keySet())
        {
            File photoFile = fileMappings.get(renamedPhotoFile);
            if(commitChanges)
            {
                if(!renamedPhotoFile.renameTo(photoFile))
                {
                    ps.println("Renaming of \""+renamedPhotoFile.getName()+"\" to \""+photoFile.getName()+"\" failed.");
                }
                else
                {
                    ps.println("Successful renaming of \""+renamedPhotoFile.getName()+"\" to \""+photoFile.getName()+"\".");
                    successfulRenames.add(renamedPhotoFile);
                }
            }
            else
            {
                ps.println("\""+renamedPhotoFile.getName()+"\" to \""+photoFile.getName()+"\".");
            }
        }
        for(File successfullyRenamed : successfulRenames)
        {
            fileMappings.remove(successfullyRenamed);
        }
        ps.println("Undo Finished: commitChanges="+commitChanges);
        if (commitChanges && !fileMappings.isEmpty())
        {
            ps.println("You can try to \"Undo\" again to try and rename the remaining files.");
        }
        return fileMappings.isEmpty();
    }
    
}
