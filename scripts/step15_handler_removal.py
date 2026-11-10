"""Shared helper of the step 15 run 17z13e scripts: removes a collector method with its annotations and Javadoc."""
import re

SIGNATURE = r"\n    (?:private |public |protected )?(?:static )?boolean {}\("


def has_method(text, name):
    return re.search(SIGNATURE.format(name), text) is not None


def remove_method(text, name):
    sig = re.search(SIGNATURE.format(name), text)
    assert sig, name
    lines_before = text[:sig.start()].split("\n")
    while lines_before:
        line = lines_before[-1].strip()
        if (line.startswith("@") or line.startswith("watch") or line.startswith("value") or line.startswith("})")
                or line.startswith("*") or line.startswith("/**") or line == ""):
            lines_before.pop()
        else:
            break
    start = len("\n".join(lines_before))
    brace = text.index("{", sig.end())
    depth, i = 1, brace + 1
    while depth:
        depth += {"{": 1, "}": -1}.get(text[i], 0)
        i += 1
    return text[:start] + "\n" + text[i:]
