// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Blowfish.java


public class Blowfish
{

    public Blowfish(long p_boxes[], long s_boxes[][])
    {
        this.p_boxes = p_boxes;
        this.s_boxes = s_boxes;
    }

    protected long[] cipher(long xl, long xr, int direction)
    {
        long result[] = new long[2];
        if(direction == 0)
        {
            long temp_x;
            for(int i = 0; i < 16; i++)
            {
                xl ^= p_boxes[i];
                xr = roundFunc(xl) ^ xr;
                temp_x = xl;
                xl = xr;
                xr = temp_x;
            }

            temp_x = xl;
            xl = xr;
            xr = temp_x;
            xr ^= p_boxes[16];
            xl ^= p_boxes[17];
        } else
        if(direction == 1)
        {
            long temp_x;
            for(int i = 17; i > 1; i--)
            {
                xl ^= p_boxes[i];
                xr = roundFunc(xl) ^ xr;
                temp_x = xl;
                xl = xr;
                xr = temp_x;
            }

            temp_x = xl;
            xl = xr;
            xr = temp_x;
            xr ^= p_boxes[1];
            xl ^= p_boxes[0];
        }
        result[0] = xl;
        result[1] = xr;
        return result;
    }

    private long roundFunc(long xl)
    {
        long a = (xl & 0xffffffffff000000L) >> 24;
        long b = (xl & 0xff0000L) >> 16;
        long c = (xl & 65280L) >> 8;
        long d = xl & 255L;
        long f = (s_boxes[0][(int)a] + s_boxes[1][(int)b]) % modulus;
        f ^= s_boxes[2][(int)c];
        f += s_boxes[3][(int)d];
        f = f % modulus & -1L;
        return f;
    }

    public long[] encrypt(char data[])
    {
        long chars[] = new long[8];
        if(data.length != 8)
        {
            throw new RuntimeException((new StringBuilder("Attempted to encrypt data of invalid block length: ")).append(data.length).toString());
        } else
        {
            long xl = (long)data[3] | (long)data[2] << 8 | (long)data[1] << 16 | (long)data[0] << 24;
            long xr = (long)data[7] | (long)data[6] << 8 | (long)data[5] << 16 | (long)data[4] << 24;
            long temp_x[] = cipher(xl, xr, 0);
            long cl = temp_x[0];
            long cr = temp_x[1];
            chars[0] = cl >> 24 & 255L;
            chars[1] = cl >> 16 & 255L;
            chars[2] = cl >> 8 & 255L;
            chars[3] = cl & 255L;
            chars[4] = cr >> 24 & 255L;
            chars[5] = cr >> 16 & 255L;
            chars[6] = cr >> 8 & 255L;
            chars[7] = cr & 255L;
            return chars;
        }
    }

    public String decrypt(char data[])
    {
        long chars[] = new long[8];
        StringBuilder result = new StringBuilder(8);
        if(data.length != 8)
            throw new RuntimeException((new StringBuilder("Attempted to encrypt data of invalid block length: ")).append(data.length).toString());
        long cl = (long)data[3] | (long)data[2] << 8 | (long)data[1] << 16 | (long)data[0] << 24;
        long cr = (long)data[7] | (long)data[6] << 8 | (long)data[5] << 16 | (long)data[4] << 24;
        long temp_x[] = cipher(cl, cr, 1);
        long xl = temp_x[0];
        long xr = temp_x[1];
        chars[0] = xl >> 24 & 255L;
        chars[1] = xl >> 16 & 255L;
        chars[2] = xl >> 8 & 255L;
        chars[3] = xl & 255L;
        chars[4] = xr >> 24 & 255L;
        chars[5] = xr >> 16 & 255L;
        chars[6] = xr >> 8 & 255L;
        chars[7] = xr & 255L;
        for(int c = 0; c < chars.length; c++)
            result.append(String.valueOf((char)(int)chars[c]));

        return result.toString();
    }

    public int blocksize()
    {
        return 8;
    }

    public int keyLength()
    {
        return 56;
    }

    public int keyBits()
    {
        return 448;
    }

    public static void main(String args1[])
    {
    }

    private static final int ENCRYPT = 0;
    private static final int DECRYPT = 1;
    private static final long modulus = (long)Math.pow(2D, 32D);
    private long p_boxes[];
    private long s_boxes[][];

}
