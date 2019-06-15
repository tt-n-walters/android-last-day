from flask import Flask, request
from json import dumps, loads

master_ip = "127.0.0.1"

app = Flask(__name__)
users = []
quiz_status = "pending"
current_question = 0
questions = None

def get_user(address):
    for user in users:
        if user["address"] == address:
            return user
    return None

def add_user(address, name):
    user = get_user(address)
    if user == None:
        users.append({
            "name": name,
            "address": address,
            "scores": [],
            "answer": -1
        })


@app.route("/")
def index():
    return "Hello there."


@app.route("/quiz", methods=["GET"])
def get_quiz():
    return dumps({ "state": quiz_status, "users": list(map(lambda x: { "name": x["name"] }, users))})


@app.route("/users", methods=["GET"])
def get_users():
    return dumps({"users": list(map(lambda x: { "name": x["name"] }, users))} )


@app.route("/users.json", methods=["GET"])
def get_users_json():
    return dumps(users)


@app.route("/users", methods=["POST"])
def post_users():
    user = get_user(request.remote_addr)
    if user == None:
        add_user(request.remote_addr, request.form.get("name"))
    else:
        name = request.form.get("name")
        if name == "":
            users.remove(user)
        else:
            user["name"] = request.form.get("name")
    return dumps({"success": True})
        

@app.route("/question", methods=["GET"])
def get_question():
    if quiz_status == "running":
        return dumps({"state": quiz_status})
    elif quiz_status == "complete":
        return dumps({"state": quiz_status, "users": list(map(lambda user: {"name": user["name"], "scores": user["scores"]}, users))})


@app.route("/question", methods=["POST"])
def post_question():
    user = get_user(request.remote_addr)
    user["answer"] = request.form.get("answer")
    return dumps({"success": True})




def check_answers(correct):
    for user in users:
        user["scores"].append(1 if user["answer"] == correct else 0)
        






@app.route("/control/state/<string:state>", methods=["GET"])
def control_state(state):
    global quiz_status
    quiz_status = state
    return quiz_status

@app.route("/control/user/<string:user>")
def control_user(user):
    add_user(users[len(users) - 1]["address"] + 1, user)
    return "Added"

@app.route("/control/question/next")
def control_question_next():
    global current_question
    check_answers(questions[current_question]["correct"])
    current_question += 1
    return "Checked"

@app.route("/control/question/get")
def control_question_get():
    return dumps({"question": questions[current_question]["question"], "answers": questions[current_question]["answers"]})





@app.route("/master", methods=["GET"])
def master():
    if request.remote_addr != master_ip:
        return "Permission denied."
    else:
        with open("page.html", "r") as page:
            return page.read()

@app.route("/static/<string:file>")
def statics(file):
    with open("static/" + file, "r") as page:
        return page.read()








def main():
    app.run(host="0.0.0.0", port=5000, debug=True)

if __name__ == "__main__":
    # add_user(0, "Bob")
    # add_user(1, "Alice")
    with open("questions.json", "r") as file:
        questions = loads(file.read())
    main()
