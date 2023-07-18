<?php
class Query{
	private $server = array('HOST'=>'','DATABASE'=>'','USER'=>'','PASSWORD'=>'');
	
	protected function Query_constructor(){ 
        $l = ($GLOBALS["___mysqli_ston"] = mysqli_connect($this->server['HOST'], $this->server['USER'], $this->server['PASSWORD'], $this->server['DATABASE'])); 
        mysqli_set_charset($l,'utf8');
        return $l;
    }
	
	public function AffectedRows($all, $table, $row, $var, $orderby, $flag, $limit = ''){ 
        (($flag<-1)||($flag>1)) ? $flag = 0 : $flag; 
        $o = array(-1 => 'DESC', 0 => '', 1 => 'ASC'); 
        if($all){
	        $q = 'SELECT * FROM '.$table;
	    } else {
		    $q = 'SELECT * FROM '.$table.' WHERE '.$row.'="'.$var.'" ';
		} 
        (!(empty($orderby))) ? $q.= ' ORDER BY '.$orderby.' ' : $q; 
        $q.= $o[$flag].' '.$limit; 
        $l = $this->Query_constructor(); 
        $r = mysqli_query($l, $q); 
        $n = mysqli_affected_rows($GLOBALS["___mysqli_ston"]); 
        ((is_null($___mysqli_res = mysqli_close($GLOBALS["___mysqli_ston"]))) ? false : $___mysqli_res); 
        return $n; 
    }
    
    public function Alter($table, $options){ 
        $q = 'ALTER TABLE '.$table.' '.$options;     
        $l = $this->Query_constructor(); 
        $r = mysqli_query($l, $q); 
        $x = (mysqli_affected_rows($GLOBALS["___mysqli_ston"])) ?  true : false; 
        ((is_null($___mysqli_res = mysqli_close($GLOBALS["___mysqli_ston"]))) ? false : $___mysqli_res); 
        return $x; 
    }
     
    public function Create($name, $rows){ 
        $q = 'CREATE TABLE IF NOT EXISTS '.$name.' ('.$rows.')';     
        $l = $this->Query_constructor(); 
        $r = mysqli_query($l, $q); 
        $x = (mysqli_affected_rows($GLOBALS["___mysqli_ston"])) ? true : false; 
        ((is_null($___mysqli_res = mysqli_close($GLOBALS["___mysqli_ston"]))) ? false : $___mysqli_res); 
        return $x; 
    }
     
    public function Delete($table, $row, $var){ 
        $q = 'DELETE FROM '.$table.' WHERE '.$row.'="'.$var.'"';     
        $l = $this->Query_constructor(); 
        $r = mysqli_query($l, $q); 
        $x = (mysqli_affected_rows($GLOBALS["___mysqli_ston"])) ? true : false; 
        ((is_null($___mysqli_res = mysqli_close($GLOBALS["___mysqli_ston"]))) ? false : $___mysqli_res); 
        return $x; 
    }
     
    public function Distinct($row, $table){ 
        $q = 'SELECT DISTINCT '.$row.' FROM '.$table; 
        $l = $this->Query_constructor(); 
        $r = mysqli_query($l, $q); 
        $d = (mysqli_affected_rows($GLOBALS["___mysqli_ston"])) ? mysqli_fetch_array($r) : false; 
        ((is_null($___mysqli_res = mysqli_close($GLOBALS["___mysqli_ston"]))) ? false : $___mysqli_res); 
        return $d; 
    }
     
    public function Drop($table){ 
        $q = 'DROP TABLE IF EXISTS '.$table;     
        $l = $this->Query_constructor(); 
        $r = mysqli_query($l, $q); 
        $x = (mysqli_affected_rows($GLOBALS["___mysqli_ston"])) ? true : false; 
        ((is_null($___mysqli_res = mysqli_close($GLOBALS["___mysqli_ston"]))) ? false : $___mysqli_res); 
        return $x; 
    }
     
    public function Insert($bool, $table, $rows, $n_arguments){ 
        $n = func_num_args(); 
        $r = explode(',',$rows); 
        $r = count($r); 
        if(!($n-3 === $r)){
	        die('ERROR! Invalid number of arguments');
	    } 
        $q = 'INSERT INTO '.$table.' ('.$rows.') VALUES ('; 
        for($i = 3; $i<$n; $i++){ 
            $d = func_get_arg($i); 
            ($i === $n-1) ? $q.= '\''.$d.'\'' : $q.='\''.$d.'\','; 
        } 
        $q.= ')'; 
        $l = $this->Query_constructor(); 
        $r = mysqli_query($l, $q); 
        if($bool){
	        $i = (mysqli_affected_rows($GLOBALS["___mysqli_ston"])) ? true : false;
	    } else {
		    $i = ((is_null($___mysqli_res = mysqli_insert_id($GLOBALS["___mysqli_ston"]))) ? false : $___mysqli_res);
		} 
        ((is_null($___mysqli_res = mysqli_close($GLOBALS["___mysqli_ston"]))) ? false : $___mysqli_res); 
        return $i; 
    }
    
    public function Invoke($query){ 
        $l = $this->Query_constructor(); 
        $r = mysqli_query($l, $query); 
        ((is_null($___mysqli_res = mysqli_close($GLOBALS["___mysqli_ston"]))) ? false : $___mysqli_res); 
        return $r;     
    } 
    
    public function InvokeLoop($query){ 
        $l = $this->Query_constructor(); 
        $r = mysqli_query($l, $query);
        ((is_null($___mysqli_res = mysqli_close($GLOBALS["___mysqli_ston"]))) ? false : $___mysqli_res); 
        return $r;     
    } 
    
    public function InvokeAffectedRows($query){ 
        $l = $this->Query_constructor(); 
        $r = mysqli_query($l, $query); 
        $n = mysqli_affected_rows($GLOBALS["___mysqli_ston"]); 
        ((is_null($___mysqli_res = mysqli_close($GLOBALS["___mysqli_ston"]))) ? false : $___mysqli_res); 
        return $n;     
    } 
    
    public function Loop($all, $table, $row, $var, $orderby, $flag, $limit = ''){ 
        (($flag<-1)||($flag>1)) ? $flag = 0 : $flag; 
        $o = array(-1 => 'DESC', 0 => '', 1 => 'ASC'); 
        if($all){
	        $q = 'SELECT * FROM '.$table;
	    } else {
		    $q = 'SELECT * FROM '.$table.' WHERE '.$row.'="'.$var.'" ';
		} 
        (!(empty($orderby))) ? $q.= ' ORDER BY '.$orderby.' ' : $q; 
        $q.= $o[$flag].' '.$limit; 
        $l = $this->Query_constructor(); 
        $r = mysqli_query($l, $q); 
        ((is_null($___mysqli_res = mysqli_close($GLOBALS["___mysqli_ston"]))) ? false : $___mysqli_res); 
        return $r; 
    } 
    
    public function LoopAND($table, $row_1, $row_2, $var_1, $var_2, $orderby, $flag){ 
        (($flag<-1)||($flag>1)) ? $flag = 0 : $flag; 
        $o = array(-1 => 'DESC', 0 => '', 1 => 'ASC'); 
        $q = 'SELECT * FROM '.$table.' WHERE '.$row_1.'="'.$var_1.'" AND '.$row_2.'="'.$var_2.'"'; 
        (!(empty($orderby))) ? $q.= ' ORDER BY '.$orderby.' ' : $q; 
        $q.= $o[$flag]; 
        $l = $this->Query_constructor(); 
        $r = mysqli_query($l, $q); 
        ((is_null($___mysqli_res = mysqli_close($GLOBALS["___mysqli_ston"]))) ? false : $___mysqli_res); 
        return $r; 
    } 
    
    public function LoopOR($table, $row_1, $row_2, $var_1, $var_2, $orderby, $flag){ 
        (($flag<-1)||($flag>1)) ? $flag = 0 : $flag; 
        $o = array(-1 => 'DESC', 0 => '',1 => 'ASC'); 
        $q = 'SELECT * FROM '.$table.' WHERE '.$row_1.'="'.$var_1.'" OR '.$row_2.'="'.$var_2.'"'; 
        (!(empty($orderby))) ? $q.= ' ORDER BY '.$orderby.' ' : $q; 
        $q.= $o[$flag]; 
        $l = $this->Query_constructor(); 
        $r = mysqli_query($l, $q); 
        ((is_null($___mysqli_res = mysqli_close($GLOBALS["___mysqli_ston"]))) ? false : $___mysqli_res); 
        return $r; 
    } 
    
    public function Match($table, $match, $against, $expansion){ 
        $q = 'SELECT * FROM '.$table.' WHERE MATCH('.$match.') AGAINST ("'.$against.'"'; 
        $expansion ? $q.= ' WITH QUERY EXPANSION) ' : $q.= ') '; 
        $l = $this->Query_constructor(); 
        $r = mysqli_query($l, $q); 
        ((is_null($___mysqli_res = mysqli_close($GLOBALS["___mysqli_ston"]))) ? false : $___mysqli_res); 
        return $q;     
    } 
    
    public function Select($all, $table, $row, $var, $orderby, $flag){ 
        (($flag<-1)||($flag>1)) ? $flag = 0 : $flag; 
        $o = array(-1 => 'DESC', 0 => '', 1 => 'ASC'); 
        if($all){
	        $q = 'SELECT * FROM '.$table;
	    } else {
		    $q = 'SELECT * FROM '.$table.' WHERE '.$row.'="'.$var.'" ';  
            (!(empty($orderby))) ? $q.= 'ORDER BY '.$orderby.' ' : $q;
        } 
        $q.= $o[$flag]; 
        $l = $this->Query_constructor(); 
        $r = mysqli_query($l, $q); 
        $d = (mysqli_affected_rows($GLOBALS["___mysqli_ston"])) ? mysqli_fetch_array($r) : false; 
        ((is_null($___mysqli_res = mysqli_close($GLOBALS["___mysqli_ston"]))) ? false : $___mysqli_res); 
        return $d; 
    } 
    
    public function SelectAND($table, $row_1, $row_2, $var_1, $var_2, $orderby, $flag){ 
        (($flag<-1)||($flag>1)) ? $flag = 0 : $flag; 
        $o = array(-1 => 'DESC', 0 => '', 1 => 'ASC'); 
        $q = 'SELECT * FROM '.$table.' WHERE '.$row_1.'="'.$var_1.'" AND '.$row_2.'="'.$var_2.'" ';  
        (!(empty($orderby))) ? $q.= 'ORDER BY '.$orderby.' ' : $q; 
        $q.= $o[$flag]; 
        $l = $this->Query_constructor(); 
        $r = mysqli_query($l, $q); 
        if(mysqli_affected_rows($GLOBALS["___mysqli_ston"])){ 
            $d = mysqli_fetch_array($r); 
            ((is_null($___mysqli_res = mysqli_close($GLOBALS["___mysqli_ston"]))) ? false : $___mysqli_res); 
            return $d; 
        } else {
	        ((is_null($___mysqli_res = mysqli_close($GLOBALS["___mysqli_ston"]))) ? false : $___mysqli_res);  
            return false; 
        } 
    } 
    
    public function SelectAVG($all, $table, $avg, $as, $row, $var){ 
        $o = array(-1 => 'DESC', 0 => '', 1 => 'ASC'); 
        $q = 'SELECT AVG('.$avg.') AS '.$as.' FROM '.$table; 
        if(!$all){
	        $q.= ' WHERE '.$row.'="'.$var.'"';
	    } 
        $l = $this->Query_constructor(); 
        $r = mysqli_query($l, $q); 
        $d = (mysqli_affected_rows($GLOBALS["___mysqli_ston"])) ? mysqli_fetch_array($r) : false; 
        ((is_null($___mysqli_res = mysqli_close($GLOBALS["___mysqli_ston"]))) ? false : $___mysqli_res); 
        return $d; 
    } 
    
    public function SelectInMap($origLat, $origLon, $dist, $tableName, $km){ 
        if($km){
	        $dist /= 1.60934;
        }
        $q = "SELECT *, 3956 * 2 *  
              ASIN(SQRT( POWER(SIN(($origLat - abs(lat))*pi()/180/2),2) 
              +COS($origLat*pi()/180 )*COS(abs(lat)*pi()/180) 
              *POWER(SIN(($origLon-lon)*pi()/180/2),2)))  
              as distance FROM $tableName WHERE  
              lon between ($origLon-$dist/abs(cos(radians($origLat))*69))  
              and ($origLon+$dist/abs(cos(radians($origLat))*69))  
              and lat between ($origLat-($dist/69))  
              and ($origLat+($dist/69)) 
              having distance < $dist ORDER BY distance;";  
        $l = $this->Query_constructor(); 
        $r = mysqli_query($l, $q); 
        ((is_null($___mysqli_res = mysqli_close($GLOBALS["___mysqli_ston"]))) ? false : $___mysqli_res); 
        return $r; 
    } 
    
    public function SelectOR($table, $row_1, $row_2, $var_1, $var_2, $orderby, $flag){ 
        (($flag<-1)||($flag>1)) ? $flag = 0 : $flag; 
        $o = array(-1 => 'DESC', 0 => '', 1 => 'ASC'); 
        $q = 'SELECT * FROM '.$table.' WHERE '.$row_1.'="'.$var_1.'" OR '.$row_2.'="'.$var_2.'"';  
        (!(empty($orderby))) ? $q.= 'ORDER BY '.$orderby.' ' : $q; 
        $q.= $o[$flag]; 
        $l = $this->Query_constructor(); 
        $r = mysqli_query($l, $q); 
        if(mysqli_affected_rows($GLOBALS["___mysqli_ston"])){ 
            $d = mysqli_fetch_array($r); 
            ((is_null($___mysqli_res = mysqli_close($GLOBALS["___mysqli_ston"]))) ? false : $___mysqli_res); 
            return $d; 
        } else { 
            ((is_null($___mysqli_res = mysqli_close($GLOBALS["___mysqli_ston"]))) ? false : $___mysqli_res);
            return false; 
        } 
    } 
    
    public function Update($table, $row, $var, $n_arguments){ 
        $n = func_num_args(); 
        $q = 'UPDATE '.$table.' SET '; 
        for($i = 3; $i<$n; $i+=2){ 
            $d = func_get_arg($i); 
            $e = func_get_arg($i+1); 
            ($i === $n-2) ? $q.= ''.$d.'="'.$e.'" ' : $q.= ''.$d.'="'.$e.'", '; 
        } 
        $q.= 'WHERE '.$row.'="'.$var.'"'; 
        $l = $this->Query_constructor(); 
        $r = mysqli_query($l, $q); 
        $x = (mysqli_affected_rows($GLOBALS["___mysqli_ston"])) ? true : false;
        ((is_null($___mysqli_res = mysqli_close($GLOBALS["___mysqli_ston"]))) ? false : $___mysqli_res); 
        return $x;
    } 
    
    public function Truncate($table){ 
        $q = 'TRUNCATE TABLE '.$table;     
        $l = $this->Query_constructor(); 
        $r = mysqli_query($l, $q); 
		$x = (mysqli_affected_rows($GLOBALS["___mysqli_ston"])) ? true : false; 
        ((is_null($___mysqli_res = mysqli_close($GLOBALS["___mysqli_ston"]))) ? false : $___mysqli_res); 
        return $x; 
    } 
}

class ImageEditor{
	private $image, $image_type;

	public function Load($filename) {
		$info = getimagesize($filename);
		$this->image_type = $info[2];
		switch($this->image_type){
			case IMAGETYPE_JPEG:
				$this->image = imagecreatefromjpeg($filename);
			break;
			case IMAGETYPE_GIF:
				$this->image = imagecreatefromgif($filename);
			break;
			case IMAGETYPE_PNG:
				$this->image = imagecreatefrompng($filename);
			break;
		}
	}
	
	public function GetHeight() {
		return imagesy($this->image);
	}
	
	public function GetWidth() {
		return imagesx($this->image);
	}
	
	public function Output($type) {
		switch($type){
			case IMAGETYPE_JPEG:
				imagejpeg($this->image);
			break;
			case IMAGETYPE_GIF:
				imagegif($this->image);
			break;
			case IMAGETYPE_PNG:
				imagepng($this->image);
			break;
		}
	}
	
	public function Resize($width,$height) {
		$new_image = imagecreatetruecolor($width, $height);
		imagecopyresampled($new_image, $this->image, 0, 0, 0, 0, $width, $height, $this->GetWidth(), $this->GetHeight());
		$this->image = $new_image;
	}  
	
	public function ResizeToHeight($height) {
		$ratio = $height / $this->GetHeight();
		$width = $this->GetWidth() * $ratio;
		$this->Resize($width, $height);
	}
	
	public function ResizeToWidth($width) {
		$ratio = $width / $this->GetWidth();
		$height = $this->Getheight() * $ratio;
		$this->Resize($width, $height);
	}
	
	public function Save($filename, $type, $compression, $permissions) {
		switch($type){
			case IMAGETYPE_JPEG:
				imagejpeg($this->image, $filename, $compression);
			break;
			case IMAGETYPE_GIF:
				imagegif($this->image, $filename);
			break;
			case IMAGETYPE_PNG:
				imagepng($this->image, $filename);
			break;
		}
		
		if($permissions <> NULL){
			chmod($filename, $permissions);
		}
	}
	
	public function Scale($scale) {
		$width = $this->GetWidth() * $scale/100;
		$height = $this->Getheight() * $scale/100;
		$this->Resize($width, $height);
	}    
}

class FTPConnection{
	private $ftp = array('HOST'=>NULL, 'USER'=>NULL, 'PASSWORD'=>NULL, 'PORT'=>21, 'PASV'=>true);	
	
	private function FTPconnexion(){
		$id = ftp_connect($this->ftp['HOST'], $this->ftp['PORT']);
		
		ftp_login($id, $this->ftp['USER'], $this->ftp['PASSWORD']);
		ftp_pasv($id, $this->ftp['PASV']);
		
		return $id;
	}
	
	public function UploadFTP($local,$remote){
		$id = $this->FTPconnexion();
		ftp_put($id, $remote, $local, FTP_BINARY);
		ftp_quit($id);
	}
	
	public function CreateDir($dir){
		$id = $this->FTPconnexion();
		ftp_mkdir($id, $dir);
		ftp_chmod($id, $dir, 0777);
		ftp_quit($id);	
	}
	
	public function RemoveDir($dir){
		$id = $this->FTPconnexion();
		ftp_rmdir($id, $dir);
		ftp_quit($id);
	}
}

class Session extends Query{
	private $user, $server = array('LIFETIME'=>1000, 'PATH'=>'/', 'DOMAIN'=>NULL, 'SECURE'=>false, 'HTTP_ONLY'=>false);
	public	$session = array('id','login');
			
	public function Checkuser($table,$login,$pass){
		if(!($this->user = $this->SelectAND($table,'login','password',$login,$pass,NULL,0))){
			return false;
		} else {
			$this->SetSession();
			return true;
		}
	}
	
	public function SetSession(){
		session_start();
		session_set_cookie_params($this->server['LIFETIME'],$this->server['PATH'],$this->server['DOMAIN'],$this->server['SECURE'],$this->server['HTTP_ONLY']);
		foreach($this->session as $item){
			$_SESSION[$item]=$this->user[$item];			
		}
	}
	
	public function StartSession(){
		session_start();
		session_set_cookie_params($this->server['LIFETIME'],$this->server['PATH'],$this->server['DOMAIN'],$this->server['SECURE'],$this->server['HTTP_ONLY']);
	}
	
	public function AddToSession($name, $value){
		$_SESSION[$name] = $value;
	}
	
	public function UnsetSession(){ 
		session_unset();
		session_destroy();
	}
}

class XSLT{
	public function Processor($template,$data){
		$xslt = new XSLTProcessor(); 
		$document = new DOMDocument(); 
		
		$document->load($template); 
		$xslt->importStyleSheet($document);
		
		$xmldoc = new DOMDocument(); 
		$xmldoc->load($data); 
		
		return $xslt->transformToXML($xmldoc);
	}
}

?>