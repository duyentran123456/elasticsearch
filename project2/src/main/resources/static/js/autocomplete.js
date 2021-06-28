$(document).ready(function() {
	$('#category').autocomplete({
		source: function(request, response) {
			$.get("http://localhost:8080/autocomplete?", { q: request.term }, function(data, status) {
				if (status == 'success') {
					response(data);
				}
			});
		}
	}
	);
});