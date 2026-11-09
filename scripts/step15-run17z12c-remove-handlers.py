#!/usr/bin/env python3
"""Step 15, run 17z12c: removes the damage and death collectors' class-keyed handlers.

Each handler recorded the trigger event's data only for its effect class, or recorded less than its watch's default:
the enchanted creature's controller as `targetId` and the damage as `xValue` (Spiteful Shadows, Binding Agony,
Repercussion), its snapshot and power on a target choice (Fiendlash), its card as damage source (Pain for All), the
enchanted creature's controller as the ability's controller (a `CreateTokenEffect` no card writes), the dying creature's
controller as `targetId` (Dingus Staff, Bounty Board), the source snapshot (Yoke of the Damned), or nothing (Sleep
Magic, Soul Link, Sadistic Glee, Blade of the Bloodchief, Dreadhound). The watches' defaults now record the event for
every ability and the cards say what their effects read (see effect-slot-refactor.md, run 17z12c's Findings), so the
handlers go with their `@Collects...Trigger` annotations, Javadoc and registrations.
"""
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent
T = ROOT / "magical-vibes-engine/src/main/java/com/github/laxika/magicalvibes/service/trigger"
REMOVE = {
    "DamageTriggerCollectorService.java": [
        "handleAnyCreatureDealtDamageToController", "handleEnchantedCreatureDealtDamageToController",
        "handleEnchantedCreatureDealtDamagePowerTarget", "handleEnchantedCreatureDealtDamageToEachOpponent",
        "handleEnchantedCreatureDealtDamageGainLife", "handleEnchantedCreatureDealtDamageCreateTokens",
        "handleEnchantedCreatureDealtDamageSacrificeSelf"],
    "DeathTriggerCollectorService.java": [
        "handleAnyCreatureDeathBountyReward", "handleAnyCreatureDeathDamageController",
        "handleAnyCreatureDeathDyingControllerLosesLife", "handleAnyCreatureDeathDestroyEnchanted",
        "handleAnyCreatureDeathCounterOnEnchanted", "handleAnyCreatureDeathConditionalReplacement"],
}
SIGNATURE = r"\n    (?:private )?boolean {}\("


def remove_method(text, name):
    sig = re.search(SIGNATURE.format(name), text)
    assert sig, name
    # Walk back over the annotations, Javadoc and blank line that precede the method.
    start = sig.start()
    lines_before = text[:start].split("\n")
    while lines_before and (lines_before[-1].strip().startswith("@") or lines_before[-1].strip().startswith("watch")
                            or lines_before[-1].strip().startswith("*") or lines_before[-1].strip().startswith("/**")
                            or lines_before[-1].strip() == ""):
        lines_before.pop()
    start = len("\n".join(lines_before))
    brace = text.index("{", sig.end())
    depth, i = 1, brace + 1
    while depth:
        depth += {"{": 1, "}": -1}.get(text[i], 0)
        i += 1
    return text[:start] + "\n" + text[i:]


def remove_registration(text, name):
    """Removes a `registerCreatureDeath(..., (match, effect, context) -> name(...));` statement."""
    pattern = re.compile(r"\n        registerCreatureDeath\((?:(?!registerCreatureDeath).)*?\b" + name
                         + r"\((?:(?!registerCreatureDeath).)*?\);", re.S)
    return pattern.sub("", text, count=1)


for file, names in REMOVE.items():
    path = T / file
    text = path.read_text()
    removed = 0
    for name in names:
        text = remove_registration(text, name)
        if re.search(SIGNATURE.format(name), text):
            text = remove_method(text, name)
            removed += 1
    path.write_text(text)
    print(file, removed, "removed")
