#!/usr/bin/env python3
"""Step 15, run 17z12f: the "you may pay" enter cards and Flayer of the Hatebound say what their effects read.

- "Create a token that's a copy of that creature / artifact" (Mirrorworks, Minion Reflector, Replication Specialist,
  Riku of Two Reflections, Ultron, Inalla, Flameshadow Conjuring) is `.ofTriggeringPermanent()`: the enter handler no
  longer binds the entering permanent as the payload's non-targeting `targetId`.
- Emiel the Blessed's "put a +1/+1 counter on it, two if it's a Unicorn" is `PutCounterOnTargetPermanentEffect
  .onTriggeringPermanent` with `FixedIfCondition(TriggeringPermanentHasSubtype(UNICORN), 2, 1)`; Brilliant Wings'
  "attach this Aura to that creature" is `AttachSourceAuraToTargetCreatureEffect.ofTriggeringPermanent()`.
- "Have that creature deal damage equal to its power to any target" (Electropotence, Flayer of the Hatebound) is
  `DealDamageToAnyTargetEffect.dealtByTriggeringPermanent(new SourcePower())`: the watcher is the ability's source
  (CR 113.7), the creature the damage's. `MayPayManaEffect.sourceIsTriggeringPermanent` goes (Electropotence, Arni
  Metalbrow, whose effect already reads the triggering permanent).
"""
import pathlib
import re

ROOT = pathlib.Path(__file__).resolve().parent.parent
CARDS = ROOT / "magical-vibes-card"
FIXED_IF_TARGET = "com.github.laxika.magicalvibes.model.amount.FixedIfTargetMatches"
FIXED_IF_CONDITION = "com.github.laxika.magicalvibes.model.amount.FixedIfCondition"
HAS_SUBTYPE_PREDICATE = "com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate"
HAS_SUBTYPE_CONDITION = "com.github.laxika.magicalvibes.model.condition.TriggeringPermanentHasSubtype"

COPY = "new CreateTokenCopyOfTargetPermanentEffect()"
EDITS = {
    "m/Mirrorworks.java": [(COPY + ",", COPY + ".ofTriggeringPermanent(),", [], [])],
    "r/ReplicationSpecialist.java": [(COPY + ",", COPY + ".ofTriggeringPermanent(),", [], [])],
    "r/RikuOfTwoReflections.java": [(COPY + ",", COPY + ".ofTriggeringPermanent(),", [], [])],
    "u/UltronArtificialMalevolence.java": [
        (COPY + ".withAnimationIfNotCreature(", COPY + ".ofTriggeringPermanent().withAnimationIfNotCreature(", [], [])],
    "m/MinionReflector.java": [
        ("new Fixed(1), false, Set.of()),", "new Fixed(1), false, Set.of()).ofTriggeringPermanent(),", [], [])],
    "f/FlameshadowConjuring.java": [
        ("new CreateTokenCopyOfTargetPermanentEffect(true, true),",
         "new CreateTokenCopyOfTargetPermanentEffect(true, true).ofTriggeringPermanent(),", [], [])],
    "i/InallaArchmageRitualist.java": [
        ("new CreateTokenCopyOfTargetPermanentEffect(true, true)),",
         "new CreateTokenCopyOfTargetPermanentEffect(true, true).ofTriggeringPermanent()),", [], [])],
    "e/EmielTheBlessed.java": [
        ("new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE,\n"
         "                        new FixedIfTargetMatches(new PermanentHasSubtypePredicate(CardSubtype.UNICORN), 2, 1)),",
         "PutCounterOnTargetPermanentEffect.onTriggeringPermanent(CounterType.PLUS_ONE_PLUS_ONE,\n"
         "                        new FixedIfCondition(new TriggeringPermanentHasSubtype(CardSubtype.UNICORN), 2, 1)),",
         [FIXED_IF_TARGET, HAS_SUBTYPE_PREDICATE], [FIXED_IF_CONDITION, HAS_SUBTYPE_CONDITION])],
    "b/BrilliantWings.java": [
        ("new AttachSourceAuraToTargetCreatureEffect(),", "AttachSourceAuraToTargetCreatureEffect.ofTriggeringPermanent(),",
         [], [])],
    "e/Electropotence.java": [
        ("                new DealDamageToAnyTargetEffect(new SourcePower()),\n"
         "                \"Pay {2}{R} to have that creature deal damage equal to its power to any target?\",\n"
         "                true));",
         "                DealDamageToAnyTargetEffect.dealtByTriggeringPermanent(new SourcePower()),\n"
         "                \"Pay {2}{R} to have that creature deal damage equal to its power to any target?\"));",
         [], [])],
    "a/ArniMetalbrow.java": [
        ("                \"Put a creature card from your hand onto the battlefield tapped and attacking?\",\n"
         "                true\n"
         "        );",
         "                \"Put a creature card from your hand onto the battlefield tapped and attacking?\"\n"
         "        );",
         [], [])],
    "f/FlayerOfTheHatebound.java": [
        ("        // damage equal to its power to any target. The entering creature is the damage source.\n",
         "        // damage equal to its power to any target. Flayer is the ability's source, the entering creature the\n"
         "        // damage's.\n", [], []),
        ("new DealDamageToAnyTargetEffect(new SourcePower())",
         "DealDamageToAnyTargetEffect.dealtByTriggeringPermanent(new SourcePower())", [], [])],
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
        assert text.count(old) == 1, (rel, old)
        text = text.replace(old, new)
        text = fix_imports(text, drop, add)
    path.write_text(text)
    print("edited", rel)
