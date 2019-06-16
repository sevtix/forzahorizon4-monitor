package dev.sevtix.horizonmonitor;

public class Utils {
    public float getFloatOfString(String input) {
        Long i = Long.parseLong(input, 16);
        Float f = Float.intBitsToFloat(i.intValue());
        return swap(f);
    }

    public String stringFromToInString(int from, int to, String string) {
        char[] array = string.toCharArray();
        String result = "";
        for(int i = from; i < to; i++) {
            result = result + array[i];
        }
        return result;
    };

    private final char[] hexArray = "0123456789ABCDEF".toCharArray();
    public String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public float swap(float value) {
        int intValue = Float.floatToRawIntBits(value);
        intValue = swap(intValue);
        return Float.intBitsToFloat(intValue);
    }

    public int swap(int value) {

        int b1 = (value >> 0) & 0xff;
        int b2 = (value >> 8) & 0xff;
        int b3 = (value >> 16) & 0xff;
        int b4 = (value >> 24) & 0xff;

        return b1 << 24 | b2 << 16 | b3 << 8 | b4 << 0;
    }
}
