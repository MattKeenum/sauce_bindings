"""Most basic example of using Simple Sauce.

Here we start a session on Sauce, perform some actions then close the session.
"""
from simplesauce.session import SauceSession
from simplesauce.options import SauceOptions

options = SauceOptions("safari")

session = SauceSession(options=options)

session.start()
session.driver.get("https://www.saucedemo.com")

assert "Swag" in session.driver.title

session.stop()