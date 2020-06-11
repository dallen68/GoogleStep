// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

function checkLoginStatus() {
    fetch('/login').then(response => response.text()).then((status) => {
        console.log(status);
        var statusArr = status.split(",",2);
        console.log(statusArr[1]);
        const booleanString = "true";
        if(statusArr[0].localeCompare(booleanString)==0) {
            document.getElementById('commentSection').style.visibility = "visible";
            document.getElementById('loginButton').innerHTML =
            "<p>Logout <a href=\"" + statusArr[1] + "\">here</a>.</p>";
            console.log("you are logged in");
        } else {
            document.getElementById('commentSection').style.visibility = "hidden";
            document.getElementById('loginButton').innerHTML =
            "<p>Login <a href=\"" + statusArr[1] + "\">here</a>.</p>";
            console.log("you need to log in");
        }
        
    });
}
function getComments() {
    var commentCount = document.getElementById('commentCount').value;
    var translateCode = document.getElementById('language').value;
    
    fetch('/comments?count='+ commentCount +'&languageCode='+ translateCode).then(response => response.json()).then((comment) => {
        const commentEl = document.getElementById('comments');
        console.log(comment);
        console.log(commentCount);
        commentEl.innerHTML = '';
        comment.forEach((line) => {
            commentEl.appendChild(createListElement(line));
        });
    });
}

function deleteComments() {
    fetch('/delete-comments', {method: 'POST'});
    console.log("it made it here");
    getComments();
}

function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}

