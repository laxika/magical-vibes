#!/usr/bin/env python3
"""Step 15, run 17z13e2: the tap-for-mana cards write the trigger conditions their class-keyed handlers judged.

The land-tap and mana-tap watches have one default each now (`LandTapTriggerCollectorService`), which records the tap
whatever the effects are and resolves a triggered mana ability at once through its effects' handlers. What the deleted
handlers judged from an effect's class or fields becomes the card's trigger, its `withFilter`, its event condition or its
intervening "if":

- "whenever enchanted / fortified land is tapped for mana" (the Auras' `AddManaOnEnchantedLandTapEffect`, C.A.M.P.):
  `Triggers.enchantedLandTappedForMana()`;
- "whenever you tap a land for mana" (the effects' `controllerOnly`, Savage Firecat's, Sasaya's Essence's and Forsaken
  Monument's handler checks): `Triggers.youTapLandForMana()`;
- "whenever an opponent taps a land for mana" (War's Toll's and Mana Web's handler checks):
  `Triggers.opponentTapsLandForMana()`; War's Toll's "tap all lands that player controls" taps the lands the triggering
  player controls (`PermanentControlledByTriggeringPlayerPredicate`): it targets no player, and the default chooses a
  targeted effect's target; "whenever you tap this land for mana" (Forbidden Orchard):
  `Triggers.youTapThisLandForMana()`;
- the land's subtype, supertype and name ("a Swamp", "a basic land", "a land with the same name as the exiled card"):
  the trigger's `withFilter`; Regal Behemoth's "while you're the monarch" its event condition (`ControllerIsMonarch`);
  Price of Glory's "if it's not that player's turn" its intervening "if" (`NotCondition(TargetPlayerTurn)`, CR 603.4);
- Treasure Nabber's "an opponent taps an artifact for mana": `Triggers.opponentTapsPermanentForMana().withFilter(artifact)`,
  its "gain control of that artifact" `GainControlOfTargetEffect.ofTriggeringPermanent` (no target: the default chooses
  the target of a targeted effect);
- the fixed-color "add an additional {C}" effects (`AddManaWhenLandOfSubtypeTappedForManaEffect`,
  `AddManaWhenLandTappedForManaEffect`, `AddManaWhenCreatureTappedForManaEffect`) are one `AddManaWhenTappedForManaEffect`;
  Snowfall's effect is `AddRestrictedManaWhenLandTappedForManaEffect`, its Island the filter;
- The Temporal Anchor's "whenever you choose to put one or more cards on the bottom of your library while scrying"
  (the scry collector skipped its effect class for a scry that bottomed nothing): its event condition
  `EventValueAtLeast(1)` on the number of cards put on the bottom;
- Rainbow Vale's "an opponent gains control of this land at the beginning of the next end step" is part of its mana
  ability (it was written as a land-tap trigger).

Idempotent. Usage: step15-run17z13e2-cards.py [LETTER...]  (run per letter module: b c d e f g h k l m n o p r s t u v
w z)
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"

IMPORTS = {
    "AddManaWhenTappedForManaEffect": f"{PKG}.model.effect.AddManaWhenTappedForManaEffect",
    "AddRestrictedManaWhenLandTappedForManaEffect": f"{PKG}.model.effect.AddRestrictedManaWhenLandTappedForManaEffect",
    "PermanentHasSubtypePredicate": f"{PKG}.model.filter.PermanentHasSubtypePredicate",
    "PermanentHasSupertypePredicate": f"{PKG}.model.filter.PermanentHasSupertypePredicate",
    "PermanentSharesNameWithImprintedCardPredicate": f"{PKG}.model.filter.PermanentSharesNameWithImprintedCardPredicate",
    "PermanentIsArtifactPredicate": f"{PKG}.model.filter.PermanentIsArtifactPredicate",
    "TriggerOptions": f"{PKG}.model.trigger.TriggerOptions",
    "ControllerIsMonarch": f"{PKG}.model.condition.ControllerIsMonarch",
    "NotCondition": f"{PKG}.model.condition.NotCondition",
    "TargetPlayerTurn": f"{PKG}.model.condition.TargetPlayerTurn",
    "EventValueAtLeast": f"{PKG}.model.condition.EventValueAtLeast",
    "ActivatedAbility": f"{PKG}.model.ActivatedAbility",
    "AwardAnyColorManaEffect": f"{PKG}.model.effect.AwardAnyColorManaEffect",
    "PermanentAllOfPredicate": f"{PKG}.model.filter.PermanentAllOfPredicate",
    "PermanentControlledByTriggeringPlayerPredicate": f"{PKG}.model.filter.PermanentControlledByTriggeringPlayerPredicate",
    "List": "java.util.List",
}
GONE = [
    f"{PKG}.model.effect.AddManaWhenLandOfSubtypeTappedForManaEffect",
    f"{PKG}.model.effect.AddManaWhenLandTappedForManaEffect",
    f"{PKG}.model.effect.AddManaWhenCreatureTappedForManaEffect",
    f"{PKG}.model.effect.AddRestrictedManaWhenLandOfSubtypeTappedForManaEffect",
]

TAPS = r"Triggers\.playerTapsLandForMana\(\)"


def subtype(m):
    """A subtype land-tap bonus: the subtype the trigger's filter; "whenever you tap a [subtype], add" (controller-only)
    the trigger "you tap" and the ability's controller adding the mana, else "its controller adds"."""
    if m.group(4):
        return (f"Triggers.youTapLandForMana().withFilter(new PermanentHasSubtypePredicate(CardSubtype.{m.group(2)})),"
                f"{m.group(1)}new AddManaWhenTappedForManaEffect(ManaColor.{m.group(3)})")
    return (f"Triggers.playerTapsLandForMana().withFilter(new PermanentHasSubtypePredicate(CardSubtype.{m.group(2)})),"
            f"{m.group(1)}AddManaWhenTappedForManaEffect.ofItsController(ManaColor.{m.group(3)})")


# (pattern, replacement), applied in order to every card file of the module.
RULES = [
    (re.compile(TAPS + r"(,\s*)(SequenceEffect\.of\(\s*)?new AddManaOnEnchantedLandTapEffect\("),
     r"Triggers.enchantedLandTappedForMana()\1\2new AddManaOnEnchantedLandTapEffect("),
    (re.compile(TAPS + r"(,\s*)new PutCounterOnTargetCreatureAndCreateTokenIfSharesManaColorEffect\("),
     r"Triggers.enchantedLandTappedForMana()\1new PutCounterOnTargetCreatureAndCreateTokenIfSharesManaColorEffect("),
    (re.compile(TAPS + r"(,\s*)new RemoveCounterFromSourceEffect\("),
     r"Triggers.youTapLandForMana()\1new RemoveCounterFromSourceEffect("),
    (re.compile(TAPS + r"(,\s*)new AddExtraManaOfChosenColorOnLandTapEffect\(\)\)"),
     r"Triggers.youTapLandForMana()\1new AddExtraManaOfChosenColorOnLandTapEffect())"),
    (re.compile(TAPS + r"(,\s*)new AddExtraManaOfChosenColorOnLandTapEffect\(false,\s*"
                       r"new PermanentHasSupertypePredicate\(CardSupertype\.BASIC\)\)\)"),
     r"Triggers.playerTapsLandForMana().withFilter(new PermanentHasSupertypePredicate(CardSupertype.BASIC))"
     r"\1AddExtraManaOfChosenColorOnLandTapEffect.ofItsController())"),
    (re.compile(TAPS + r"(,\s*)new AddOneOfEachManaTypeProducedByLandEffect\(false, true\)"),
     r"Triggers.playerTapsLandForMana().withFilter(new PermanentSharesNameWithImprintedCardPredicate())"
     r"\1new AddOneOfEachManaTypeProducedByLandEffect()"),
    (re.compile(TAPS + r"(,\s*)AddOneOfEachManaTypeProducedByLandEffect\.anyColor\(true, true\)"),
     r"Triggers.youTapLandForMana()\1TriggerOptions.DEFAULT.withEventCondition(new ControllerIsMonarch())"
     r"\1AddOneOfEachManaTypeProducedByLandEffect.ofAnyColor()"),
    (re.compile(TAPS + r"(,\s*)new AddOneOfEachManaTypeProducedByLandEffect\(true\)"),
     r"Triggers.youTapLandForMana()\1new AddOneOfEachManaTypeProducedByLandEffect()"),
    (re.compile(r"new AddOneOfEachManaTypeProducedByLandEffect\((?:false|true)\)"),
     "new AddOneOfEachManaTypeProducedByLandEffect()"),
    (re.compile(TAPS + r"(,\s*)new AddManaForEachOtherLandWithSameNameEffect\("),
     r"Triggers.youTapLandForMana()\1new AddManaForEachOtherLandWithSameNameEffect("),
    (re.compile(TAPS + r"(,\s*)new AddManaOfTypeProducedByTappedPermanentEffect\("),
     r"Triggers.youTapLandForMana()\1new AddManaOfTypeProducedByTappedPermanentEffect("),
    (re.compile(TAPS + r",(\s*)new AddManaWhenLandOfSubtypeTappedForManaEffect\(CardSubtype\.(\w+), ManaColor\.(\w+)"
                       r"(, true)?\)"),
     subtype),
    (re.compile(TAPS + r"(,\s*)new AddManaWhenLandTappedForManaEffect\(ManaColor\.(\w+)\)"),
     r"Triggers.youTapLandForMana()\1new AddManaWhenTappedForManaEffect(ManaColor.\2)"),
    (re.compile(TAPS + r"(,\s*)new AddManaWhenLandOfColorTappedForManaEffect\("),
     r"Triggers.youTapLandForMana()\1new AddManaWhenLandOfColorTappedForManaEffect("),
    (re.compile(TAPS + r"(,\s*)new AddRestrictedManaWhenLandOfSubtypeTappedForManaEffect\(\s*CardSubtype\.(\w+),\s*"),
     r"Triggers.playerTapsLandForMana().withFilter(new PermanentHasSubtypePredicate(CardSubtype.\2))"
     r"\1new AddRestrictedManaWhenLandTappedForManaEffect("),
    (re.compile(r"addTrigger\(" + TAPS + r",(\s*)new DestroyReferencedPermanentEffect\("),
     r"addTrigger(Triggers.playerTapsLandForMana(),\1"
     r"TriggerOptions.DEFAULT.withInterveningIf(new NotCondition(new TargetPlayerTurn())),"
     r"\1new DestroyReferencedPermanentEffect("),
    (re.compile(TAPS + r"(,\s*)new TapPermanentsEffect\(\s*TapUntapScope\.TARGET_PLAYERS_PERMANENTS,\s*"
                       r"new PermanentIsLandPredicate\(\)\)\)"),
     r"Triggers.opponentTapsLandForMana()\1new TapPermanentsEffect(TapUntapScope.ALL_PERMANENTS,"
     r"\n                new PermanentAllOfPredicate(List.of(new PermanentIsLandPredicate(),"
     r"\n                        new PermanentControlledByTriggeringPlayerPredicate()))))"),
    (re.compile(TAPS + r"(,\s*)new TapLandsThatCouldProduceSameManaAsTappedLandEffect\("),
     r"Triggers.opponentTapsLandForMana()\1new TapLandsThatCouldProduceSameManaAsTappedLandEffect("),
    (re.compile(TAPS + r"(,\s*)new CreateTokenForTargetPlayerEffect\("),
     r"Triggers.youTapThisLandForMana()\1new CreateTokenForTargetPlayerEffect("),
    (re.compile(r"Triggers\.playerTapsPermanentForMana\(\)(,\s*)new GainControlOfTargetEffect\("),
     r"Triggers.opponentTapsPermanentForMana().withFilter(new PermanentIsArtifactPredicate())\1"
     r"GainControlOfTargetEffect.ofTriggeringPermanent("),
    (re.compile(r"new AddManaWhenCreatureTappedForManaEffect\("), "new AddManaWhenTappedForManaEffect("),
    (re.compile(r"addTrigger\(Triggers\.youScry\(\), new ExileBottomCardsToSourceEffect\(new EventValue\(\)\)\);"),
     "addTrigger(Triggers.youScry(), TriggerOptions.DEFAULT.withEventCondition(new EventValueAtLeast(1)),\n"
     "                new ExileBottomCardsToSourceEffect(new EventValue()));"),
    (re.compile(r"addActivatedAbility\(ManaAbilities\.tapForAnyColor\(\)\);\s*addTrigger\(" + TAPS
                + r",\s*new RegisterDelayedChooseOpponentGainsControlOfSourceEffect\(\)\);"),
     "addActivatedAbility(new ActivatedAbility(\n"
     "                true,\n"
     "                null,\n"
     "                List.of(new AwardAnyColorManaEffect(),\n"
     "                        new RegisterDelayedChooseOpponentGainsControlOfSourceEffect()),\n"
     "                \"{T}: Add one mana of any color. An opponent gains control of this land at the beginning of the \"\n"
     "                        + \"next end step.\"));"),
]


def add_import(text, fqcn):
    """Inserts {@code import fqcn;} in order among the file's imports of the same root package."""
    line = f"import {fqcn};\n"
    lines = text.splitlines(keepends=True)
    imports = [i for i, l in enumerate(lines) if l.startswith("import ")]
    root = fqcn.split(".")[0]
    same = [i for i in imports if lines[i][7:].split(".")[0] == root]
    if not same:
        at = imports[-1] + 1
        lines[at:at] = ["\n", line] if root == "java" else [line]
        return "".join(lines)
    after = [i for i in same if lines[i] < line]
    at = (after[-1] + 1) if after else same[0]
    lines.insert(at, line)
    return "".join(lines)


def fix_imports(text):
    for gone in GONE:
        text = text.replace(f"import {gone};\n", "")
    body = text[text.index("\n@"):] if "\n@" in text else text
    for unused in ("ManaAbilities", "Triggers"):
        fqcn = {"ManaAbilities": f"{PKG}.model.ManaAbilities", "Triggers": f"{PKG}.model.trigger.Triggers"}[unused]
        if f"import {fqcn};" in text and unused + "." not in body:
            text = text.replace(f"import {fqcn};\n", "")
    for name, fqcn in IMPORTS.items():
        if re.search(r"\b" + name + r"\b", body) and f"import {fqcn};" not in text:
            text = add_import(text, fqcn)
    return text


def convert(path):
    text = path.read_text()
    new = text
    for pattern, replacement in RULES:
        new = pattern.sub(replacement, new)
    if new == text:
        return False
    path.write_text(fix_imports(new))
    return True


def main():
    letters = sys.argv[1:] or [p.name for p in sorted((ROOT / "magical-vibes-card").iterdir())
                               if (p / "src/main/java").is_dir()]
    changed = []
    for letter in letters:
        base = ROOT / "magical-vibes-card" / letter / "src/main/java"
        for path in sorted(base.rglob("*.java")):
            if convert(path):
                changed.append(path.stem)
    print(f"{len(changed)} card classes changed: {' '.join(changed)}")


if __name__ == "__main__":
    main()
