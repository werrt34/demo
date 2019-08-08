package demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.soecode.wxtools.api.IService;
import com.soecode.wxtools.api.WxMessageHandler;
import com.soecode.wxtools.bean.WxXmlMessage;
import com.soecode.wxtools.bean.WxXmlOutMessage;
import com.soecode.wxtools.exception.WxErrorException;

public class DemoHandler implements WxMessageHandler {
	
	@SuppressWarnings("unused")
	private WxXmlMessage wxXmlMessage;
	
	public DemoHandler() {
		
	}
	
	public Connection  getConnection() {
		String driver = "com.mysql.jdbc.Driver";
		String username = System.getenv("ACCESSKEY");
		String password = System.getenv("SECRETKEY");
		String dbUrl = String.format("jdbc:mysql://%s:%s/%s", System.getenv("MYSQL_HOST_S"), System.getenv("MYSQL_PORT"), System.getenv("MYSQL_DB"));
		Connection con = null;
		try {
		    Class.forName(driver).newInstance();
		    con = DriverManager.getConnection(dbUrl, username, password);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return con;
	}
	public DemoHandler( WxXmlMessage wxXmlMessage) {
		this.wxXmlMessage = wxXmlMessage;
	}
	
	@Override
	public WxXmlOutMessage handle(WxXmlMessage wxMessage,
			Map<String, Object> context, IService iService)
			throws WxErrorException {
		String content = wxMessage.getContent();
		StringBuilder sbf = new StringBuilder();
		StringBuilder sb = new StringBuilder();
		WxXmlOutMessage xmlOutMs = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		sbf.append("SELECT `locale` FROM `LOCALETABLE` ");
		if(content.startsWith("#") && content.length()>1) {
			sbf.append("  where `locale` like '").append("%").append(content.substring(1)).append("%'");
		}else {
			sbf.setLength(0);
		}
		int line = 0;
		boolean lengthFlag = false;
		if(sbf.length() > 0) {
			try {
				conn = this.getConnection();
				System.out.println("��ѯ����ǣ�\n " + sbf.toString());
				pstmt = conn.prepareStatement(sbf.toString());
				rs = pstmt.executeQuery();
				sb.append("���" + "\t" + "�Ϻ�"+"\t"+"��λ" +"\t" + "ÿ������" +"\t" + "����").append(System.getProperty("line.separator"));
				while(rs.next()) {
					String locale = rs.getString(1);
					String[] local = locale.split("_");
					sb.append(++line+"\t"+local[0] +"\t"+local[1] +"\t"+ local[2]+ "\t"+local[3]).append(System.getProperty("line.separator"));
					if(sb.toString().getBytes().length>2048) {
						lengthFlag = true;
						break;
					}
				}
			} catch (SQLException e) {
				System.out.println("��ѯ����");
				e.printStackTrace();
			}finally {
				try {
					rs.close();
					pstmt.close();
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			if(line==0){
				xmlOutMs = WxXmlOutMessage.TEXT().content("û������").toUser(wxMessage.getFromUserName()).fromUser(wxMessage.getToUserName()).build();
			}else {
				if(lengthFlag) {
					System.out.println("������:"+ sb.toString().getBytes().length);
					System.out.println("sb������"+sb.length());
					xmlOutMs = WxXmlOutMessage.TEXT().content(sb.substring(0, sb.length()-80)+ System.getProperty("line.separator") +"����̫�࣬û��ȫ����ʾ").toUser(wxMessage.getFromUserName()).fromUser(wxMessage.getToUserName()).build();
				}else {
					xmlOutMs = WxXmlOutMessage.TEXT().content(sb.toString()).toUser(wxMessage.getFromUserName()).fromUser(wxMessage.getToUserName()).build();
				}
			}
		}else {
			xmlOutMs = WxXmlOutMessage.TEXT().content("��ѯ������#��ͷ,���Ҳ�ѯ������#������һλ").toUser(wxMessage.getFromUserName()).fromUser(wxMessage.getToUserName()).build();
		}
		return xmlOutMs;
	}

}
