public class Encryption
{
    public static String Encrypt(String text)
    {
        String encryptedText = "";
        for(int i = 0; i < text.length(); i++)
        {
            int a = (int)text.charAt(i);
            //System.out.print(a+"--");
            a = (a * 3 * 5 * 8) + 1;

            encryptedText += (char)a;
            //System.out.println(a);
        }

        return encryptedText;
    }

    public static String Decrypt(String encryptedText)
    {
        String decryptedText = "";

        for(int i = 0; i < encryptedText.length(); i++)
        {
            int a = (int)encryptedText.charAt(i);
            a = (a-1);
            a = a / 8;
            a = a / 5;
            a = a / 3;

            //System.out.println(a);

            decryptedText += (char)a;
        }
        return decryptedText;
    }
}
