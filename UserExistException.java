/**
 * User Exist Exception
 *
 * Purdue University -- CS18000 -- Spring 2022 -- Project 4
 *
 * @author William Yu, yuwl; Lamiya Laxmidhar, llaxmidh; Mohnish Harwani, mharwan; Ben Hartley, hartleyb;
 * @version July 22, 2023
 */

public class UserExistException extends Exception {
    public UserExistException(String message) {
        super(message);
    }
}
