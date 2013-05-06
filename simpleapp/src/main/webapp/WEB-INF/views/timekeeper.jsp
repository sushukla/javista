<%@ taglib prefix="h" tagdir="/WEB-INF/tags" %>
<html>
<body>
<h2>World Clock</h2>
<div id="pst"><h:pst model="${pstmodel}"/></div>
<div id="mst"><h:mst model="${mstmodel}"/></div>
<div id="gmt"><h:gmt model="${gmtmodel}"/></div>
<div id="jst"><h:jstluse /></div>
</body>
</html>
