import os
import subprocess
from google import genai
from google.genai import types

client = genai.Client()


def execute_shell(command: str) -> str:
    """Executes a shell command in the local project directory."""
    print(f"\n[Running]: {command}")
    res = subprocess.run(
        command, shell=True, capture_output=True, text=True, timeout=120
    )
    return res.stdout if res.returncode == 0 else f"Error: {res.stderr}"


prompt = """
You are setting up and publishing this project to GitHub.
1. Check the directory and review files.
2. Create a .gitignore for Java/Maven/Oracle SQL.
3. Initialize git on 'main', stage, and commit.
4. Execute: gh repo create ausmun-management-system --public --source=. --remote=origin --push
Use the execute_shell tool to complete every step.
"""

response = client.models.generate_content(
    model="gemini-3.5-flash-lite",
    contents=prompt,
    config=types.GenerateContentConfig(
        tools=[execute_shell],
        temperature=0.2,
    ),
)

print(response.text)