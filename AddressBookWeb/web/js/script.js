$(document).ready(function (){
    $("#panel").mouseenter(function (){
       $("#adder").clearQueue();
       $("#panel").css("background-position", "-999px, -999px");
       $("#adder").slideDown(100, function (){$(this).css("visibility", "visible");}); 
    });
    
    $("#panel").mouseleave(function (){
       $("#adder").clearQueue();
       $("#adder").css("visibility", "hidden");
       $("#adder").slideUp(100, function (){$("#panel").css("background-position", "center");}); 
    });
       
    setTimeout('$("#message").animate({opacity: 0, height: 0, paddingTop: 0, paddingBottom: 0}, 1000, function(){$(this).remove()});', 4000);
    
    $('.tooltip').tooltipster({
        theme: 'tooltipster-addressbook',
        delay: 100,
        speed: 200,
        timer: 99999,
        arrow: false
    });
});

function editp(pid)
{
    $("li[pid="+pid+"] .firstname").replaceWith("<input type='text' name='firstname' value='"+($("li[pid="+pid+"] .firstname").text())+"' />");
    $("li[pid="+pid+"] .nickname").replaceWith("<input type='text' name='nickname' value='"+stripquotes($("li[pid="+pid+"] .nickname").text())+"' />");
    $("li[pid="+pid+"] .lastname").replaceWith("<input type='text' name='lastname' value='"+($("li[pid="+pid+"] .lastname").text())+"' />");
    $("li[pid="+pid+"] .name").wrap("<form method='post'></form>");
    $(".editbutton[pid="+pid+"]").replaceWith("<input type='hidden' name='pid' value='"+pid+"'/><input type='submit' name='submit' value='editp' class='editp tooltip' title='Finish editing'>");
    $('.editp').tooltipster({
        theme: 'tooltipster-addressbook',
        delay: 100,
        speed: 200,
        timer: 99999,
        arrow: false
    });
}

function editc(cid)
{
    $("span[cid="+cid+"] form").remove();
    $("span[cid="+cid+"] .ckey").replaceWith("<input type='text' name='ckey' value='"+($("span[cid="+cid+"] .ckey").text())+"' />");
    $("span[cid="+cid+"] .cval").replaceWith("<input type='text' name='cval' value='"+($("span[cid="+cid+"] .cval").text())+"' />");
    $("span[cid="+cid+"]").wrap("<form method='post'></form>");
    $(".editbutton[cid="+cid+"]").replaceWith("<input type='hidden' name='cid' value='"+cid+"'><input type='submit' name='submit' value='editc' class='editc tooltip' title='Finish editing'>");
    $('.editc').tooltipster({
        theme: 'tooltipster-addressbook',
        delay: 100,
        speed: 200,
        timer: 99999,
        arrow: false
    });
}

function stripquotes(str)
{
    if(str[0] === '"')
        return str.substr(1, str.length - 2);
    else
        return str;
}
