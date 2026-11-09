#!/usr/bin/env python3
"""Step 15, run 17z12c: the damage and death cards say what their effects read from the trigger event.

- "That much damage to that creature's controller" (Binding Agony, Backfire, Repercussion) is
  `DealDamageToPlayersEffect(EventValue, TRIGGERING_PERMANENT_CONTROLLER)`; Spiteful Shadows' "it deals that much damage
  to its controller" the same, `.dealtByTriggeringPermanent()`. `EnchantedCreatureDealsDamageEqualToDealtDamageToController
  Effect` goes.
- "It deals damage equal to its power" (Fiendlash) and "it deals that much damage" (Wrathful Raptors, Wrathful Red
  Dragon) are `DealDamageToAnyTargetEffect.dealtByTriggeringPermanent(...)`, the factory `fromEnteringPermanent` renamed
  (it was never only the entering permanent's).
- Bounty Board's "a creature with a bounty counter on it" is the trigger's filter.
"""
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent
CARDS = ROOT / "magical-vibes-card"
OLD_CONTROLLER = "com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect"
OLD_POWER = "com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDealsPowerDamageToAnyTargetEffect"
DAMAGE_PLAYERS = "com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect"
DAMAGE_ANY = "com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect"
RECIPIENT = "com.github.laxika.magicalvibes.model.effect.DamageRecipient"
EVENT_VALUE = "com.github.laxika.magicalvibes.model.amount.EventValue"
SOURCE_POWER = "com.github.laxika.magicalvibes.model.amount.SourcePower"
X_VALUE = "com.github.laxika.magicalvibes.model.amount.XValue"
HAS_COUNTERS = "com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate"
COUNTER_TYPE = "com.github.laxika.magicalvibes.model.CounterType"

TO_CONTROLLER = "new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER)"
EDITS = {
    "s/SpitefulShadows.java": [
        ("new EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect()",
         TO_CONTROLLER + ".dealtByTriggeringPermanent()", [OLD_CONTROLLER], [DAMAGE_PLAYERS, RECIPIENT, EVENT_VALUE])],
    "b/BindingAgony.java": [
        ("new EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect(true)", TO_CONTROLLER,
         [OLD_CONTROLLER], [DAMAGE_PLAYERS, RECIPIENT, EVENT_VALUE])],
    "b/Backfire.java": [
        ("new EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect(true)", TO_CONTROLLER,
         [OLD_CONTROLLER], [DAMAGE_PLAYERS, RECIPIENT, EVENT_VALUE])],
    "r/Repercussion.java": [
        ("new EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect(true)", TO_CONTROLLER,
         [OLD_CONTROLLER], [DAMAGE_PLAYERS, RECIPIENT, EVENT_VALUE])],
    "f/Fiendlash.java": [
        ("new EnchantedCreatureDealsPowerDamageToAnyTargetEffect()",
         "DealDamageToAnyTargetEffect.dealtByTriggeringPermanent(new SourcePower())",
         [OLD_POWER], [DAMAGE_ANY, SOURCE_POWER])],
    "w/WrathfulRaptors.java": [
        ("new DealDamageToAnyTargetEffect(new XValue(), false, false, -1, null, false, false, true)",
         "DealDamageToAnyTargetEffect.dealtByTriggeringPermanent(new EventValue())", [X_VALUE], [EVENT_VALUE])],
    "w/WrathfulRedDragon.java": [
        ("new DealDamageToAnyTargetEffect(new XValue(), false, false, -1, null, false, false, true)",
         "DealDamageToAnyTargetEffect.dealtByTriggeringPermanent(new EventValue())", [X_VALUE], [EVENT_VALUE])],
    "b/BountyBoard.java": [
        ("addTrigger(Triggers.creatureDies(ObjectRelation.ANY),",
         "addTrigger(Triggers.creatureDies(new PermanentHasCountersPredicate(CounterType.BOUNTY), ObjectRelation.ANY),",
         [], [HAS_COUNTERS, COUNTER_TYPE])],
}


def card_path(rel):
    letter, name = rel.split("/")
    return CARDS / letter / "src/main/java/com/github/laxika/magicalvibes/cards" / letter / name


def fix_imports(text, drop, add):
    for imp in drop:
        simple = imp.rsplit(".", 1)[1]
        body = text.replace(f"import {imp};\n", "")
        if not re.search(r"\b" + simple + r"\b", body):
            text = body
    for imp in add:
        if f"import {imp};" not in text:
            lines = text.split("\n")
            imports = [i for i, line in enumerate(lines) if line.startswith("import ")]
            line = f"import {imp};"
            later = [i for i in imports if lines[i] > line]
            lines.insert(later[0] if later else imports[-1] + 1, line)
            text = "\n".join(lines)
    return text


for rel, edits in EDITS.items():
    path = card_path(rel)
    text = path.read_text()
    for old, new, drop, add in edits:
        assert old in text, (rel, old)
        text = text.replace(old, new)
        text = fix_imports(text, drop, add)
    path.write_text(text)
    print("edited", rel)

renamed = 0
for path in list(ROOT.glob("magical-vibes-*/**/src/**/*.java")):
    text = path.read_text()
    if "fromEnteringPermanent" in text:
        path.write_text(text.replace("fromEnteringPermanent", "dealtByTriggeringPermanent"))
        renamed += 1
print("renamed fromEnteringPermanent in", renamed, "files")
