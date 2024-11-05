document.addEventListener('DOMContentLoaded', (event) => {
  const resultsTableBody = document.getElementById('results-table').querySelector('tbody');
  const accuracyValue = document.getElementById('accuracy-value');
  const precisionValue = document.getElementById('precision-value');

  // Function to handle errors when fetching data
  const handleFetchError = (error) => {
    console.error('Error fetching data:', error);
    // Display an error message to the user or handle the error in some other way
  };

  // Populate test results
  fetch('http://localhost:8080/spamDetector-1.0/api/spam')
    .then(response => {
      if (!response.ok) {
        throw new Error('Failed to fetch test results');
      }
      return response.json();
    })
    .then(data => {
      data.forEach(result => {
        const row = resultsTableBody.insertRow();
        row.insertCell().textContent = result.filename;
        row.insertCell().textContent = result.spamProbability;
        row.insertCell().textContent = result.actualClass;
      });
    })
    .catch(handleFetchError);

  // Populate accuracy
  fetch('http://localhost:8080/spamDetector-1.0/api/spam/accuracy')
    .then(response => {
      if (!response.ok) {
        throw new Error('Failed to fetch accuracy data');
      }
      return response.json();
    })
    .then(data => {
      accuracyValue.textContent = `${(data.accuracy * 100).toFixed(2)}%`;
    })
    .catch(handleFetchError);

  // Populate precision
  fetch('http://localhost:8080/spamDetector-1.0/api/spam/precision')
    .then(response => {
      if (!response.ok) {
        throw new Error('Failed to fetch precision data');
      }
      return response.json();
    })
    .then(data => {
      precisionValue.textContent = `${(data.precision * 100).toFixed(2)}%`;
    })
    .catch(handleFetchError);
});
