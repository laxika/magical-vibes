#!/usr/bin/env python3
"""Step 15, run 17z13e8: the cards whose step abilities the step readers judged or merged by an effect's class.

The cards say what the readers decided by class (effect-slot-refactor.md, run 17z13e8's Findings):

- vanishing's "at the beginning of your upkeep, if this permanent has a time counter on it, remove a time counter from
  it" is an intervening "if" (CR 702.63a, 603.4): `SourceCounterThreshold(1, TIME)`, not the your-upkeep reader's
  removal of `RemoveCounterAndSacrificeSelfOnLastEffect` with no counter (Deadwood Treefolk wrote it as an event
  condition);
- Ghazbán Ogre's and Wild Dogs' "if a player has more life than each other player" is the intervening "if"
  `APlayerHasMoreLifeThanEachOtherPlayer`;
- Hellfire Mongrel's, Lavaborn Muse's, Pauper's Cage's and Prickle Faeries' "if that player has two or fewer cards in
  hand, this deals N damage to that player" is the intervening "if" `ActivePlayerHandAtMost(2)` with
  `DealDamageToPlayersEffect(N, TRIGGERING_PLAYER)` (`DealDamageIfFewCardsInHandEffect` is gone);
- Paradox Haze's "enchanted player's first upkeep each turn" is its event condition `FirstUpkeepThisTurn`;
- All Hallow's Eve's "if this card is exiled with a scream counter on it" is the intervening "if"
  `SourceCardInExileWithScreamCounter`;
- Colfenor's Urn's "if three or more cards have been exiled with this artifact" is the intervening "if"
  `SourceExiledCardsThreshold(3)`;
- Archivist of Gondor's "at the beginning of the monarch's end step" is its event condition `ActivePlayerIsMonarch`;
- Malignant Growth's "each opponent's draw step" and Well of Ideas' "each other player's draw step" are
  `Triggers.drawStep(ActivePlayer.OPPONENT)` (the effect class's opponent-only mark is gone);
- Smile at Death's "return up to two target creature cards ... Put a +1/+1 counter on each of those creatures" is one
  ability (CR 603.3), not two the reader merged by the counter effect's class.

Idempotent. Usage: step15-run17z13e8-cards.py [LETTER...] (card modules: a..z, shared; none given: all of them).
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
CARDS = ROOT / "magical-vibes-card"
PKG = "com/github/laxika/magicalvibes/cards"
IMPORT = "import com.github.laxika.magicalvibes.model.{}.{};"

VANISHING_REMOVE = "new RemoveCounterAndSacrificeSelfOnLastEffect(CounterType.TIME)"
VANISHING_IF = "TriggerOptions.DEFAULT.withInterveningIf(new SourceCounterThreshold(1, CounterType.TIME))"
VANISHING = [
    ("c", "CrackInTime"), ("d", "DeadlyGrub"), ("d", "DeepForestHermit"), ("f", "FourKnocks"),
    ("i", "IdrisSoulOfTheTARDIS"), ("k", "KeldonMarauders"), ("l", "LostAuramancers"), ("l", "LavacoreElemental"),
    ("m", "MaelstromDjinn"), ("o", "OutOfTime"), ("o", "OmenpathToNaya"), ("r", "RealityAcid"),
    ("r", "RavagingRiftwurm"), ("s", "SoultetherGolem"), ("t", "Tidewalker"), ("t", "TheGirlInTheFireplace"),
    ("w", "WaningWurm"),
]


def add_import(text, package, name):
    line = IMPORT.format(package, name)
    if line in text:
        return text
    imports = list(re.finditer(r"^import [\w.]+;$", text, re.M))
    lines = sorted({m.group(0) for m in imports} | {line})
    start, end = imports[0].start(), imports[-1].end()
    return text[:start] + "\n".join(lines) + text[end:]


def drop_import(text, package, name):
    simple = name
    line = IMPORT.format(package, name) + "\n"
    body = text.replace(line, "")
    if re.search(r"\b" + simple + r"\b", body):
        return text
    return body


def vanishing(text):
    # An upkeep trigger (a card's, a SpellTarget chain's or a TriggeredAbility's) whose effect is the removal and that
    # has no options yet.
    pattern = re.compile(r"(Triggers\.upkeep\(ActivePlayer\.YOU\),)(\s*)" + re.escape(VANISHING_REMOVE))
    def sub(m):
        gap = m.group(2) if "\n" in m.group(2) else "\n                "
        return m.group(1) + gap + VANISHING_IF + "," + gap + VANISHING_REMOVE
    text = pattern.sub(sub, text)
    text = add_import(text, "trigger", "TriggerOptions")
    return add_import(text, "condition", "SourceCounterThreshold")


def deadwood(text):
    text = text.replace("TriggerOptions.DEFAULT.withEventCondition(new SourceCounterThreshold(1, CounterType.TIME))",
                        VANISHING_IF)
    return text


def life_leader(text):
    old = "addTrigger(Triggers.upkeep(ActivePlayer.YOU), new PlayerWithMostLifeGainsControlOfSourceCreatureEffect());"
    new = ("addTrigger(Triggers.upkeep(ActivePlayer.YOU),\n"
           "                TriggerOptions.DEFAULT.withInterveningIf(new APlayerHasMoreLifeThanEachOtherPlayer()),\n"
           "                new PlayerWithMostLifeGainsControlOfSourceCreatureEffect());")
    text = text.replace(old, new)
    text = add_import(text, "trigger", "TriggerOptions")
    return add_import(text, "condition", "APlayerHasMoreLifeThanEachOtherPlayer")


def few_cards(text):
    def sub(m):
        max_cards, damage = m.group(2), m.group(3)
        return (m.group(1).rstrip() + "\n                TriggerOptions.DEFAULT.withInterveningIf(new ActivePlayerHandAtMost(" + max_cards
                + ")),\n                new DealDamageToPlayersEffect(" + damage + ", DamageRecipient.TRIGGERING_PLAYER)")
    text = re.sub(r"(addTrigger\(Triggers\.upkeep\(ActivePlayer\.OPPONENT\),\s*)new DealDamageIfFewCardsInHandEffect"
                  r"\((\d+), (\d+)\)", sub, text)
    text = drop_import(text, "effect", "DealDamageIfFewCardsInHandEffect")
    text = add_import(text, "trigger", "TriggerOptions")
    text = add_import(text, "condition", "ActivePlayerHandAtMost")
    text = add_import(text, "effect", "DealDamageToPlayersEffect")
    return add_import(text, "effect", "DamageRecipient")


def paradox_haze(text):
    old = "addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PLAYER), new AdditionalUpkeepStepEffect());"
    new = ("addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PLAYER),\n"
           "                TriggerOptions.DEFAULT.withEventCondition(new FirstUpkeepThisTurn()),\n"
           "                new AdditionalUpkeepStepEffect());")
    text = text.replace(old, new)
    text = add_import(text, "trigger", "TriggerOptions")
    return add_import(text, "condition", "FirstUpkeepThisTurn")


def all_hallows_eve(text):
    old = "AbilityZones.EXILE), TriggerOptions.DEFAULT,\n"
    new = ("AbilityZones.EXILE),\n"
           "                TriggerOptions.DEFAULT.withInterveningIf(new SourceCardInExileWithScreamCounter()),\n")
    text = text.replace(old, new)
    return add_import(text, "condition", "SourceCardInExileWithScreamCounter")


def colfenors_urn(text):
    old = ("addTrigger(Triggers.endStep(ActivePlayer.ANY),\n"
           "                new SacrificeSelfAndReturnCardsExiledWithSourceEffect(3));")
    new = ("addTrigger(Triggers.endStep(ActivePlayer.ANY),\n"
           "                TriggerOptions.DEFAULT.withInterveningIf(new SourceExiledCardsThreshold(3)),\n"
           "                new SacrificeSelfAndReturnCardsExiledWithSourceEffect(3));")
    text = text.replace(old, new)
    text = add_import(text, "trigger", "TriggerOptions")
    return add_import(text, "condition", "SourceExiledCardsThreshold")


def archivist(text):
    old = "addTrigger(Triggers.endStep(ActivePlayer.ANY), new DrawCardForMonarchEndStepEffect());"
    new = ("addTrigger(Triggers.endStep(ActivePlayer.ANY),\n"
           "                TriggerOptions.DEFAULT.withEventCondition(new ActivePlayerIsMonarch()),\n"
           "                new DrawCardForMonarchEndStepEffect());")
    text = text.replace(old, new)
    return add_import(text, "condition", "ActivePlayerIsMonarch")


def malignant_growth(text):
    return text.replace("addTrigger(Triggers.drawStep(ActivePlayer.ANY), new DrawPerSourceCounterThenDamageEffect(",
                        "addTrigger(Triggers.drawStep(ActivePlayer.OPPONENT), new DrawPerSourceCounterThenDamageEffect(")


def well_of_ideas(text):
    return text.replace(
        "addTrigger(Triggers.drawStep(ActivePlayer.ANY), DrawCardForTargetPlayerEffect.forOpponentDrawStep(1));",
        "addTrigger(Triggers.drawStep(ActivePlayer.OPPONENT), new DrawCardForTargetPlayerEffect(1));")


def smile_at_death(text):
    old = ("                2, false, false));\n"
           "        addTrigger(Triggers.upkeep(ActivePlayer.YOU),\n"
           "                new PutCounterOnReturnedPermanentsEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));")
    new = ("                2, false, false),\n"
           "                new PutCounterOnReturnedPermanentsEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));")
    return text.replace(old, new)


EDITS = [(letter, name, vanishing) for letter, name in VANISHING] + [
    ("d", "DeadwoodTreefolk", deadwood),
    ("g", "GhazbNOgre", life_leader), ("w", "WildDogs", life_leader),
    ("h", "HellfireMongrel", few_cards), ("l", "LavabornMuse", few_cards), ("p", "PaupersCage", few_cards),
    ("shared", "p/PrickleFaeries", few_cards),
    ("p", "ParadoxHaze", paradox_haze), ("a", "AllHallowsEve", all_hallows_eve),
    ("c", "ColfenorsUrn", colfenors_urn), ("a", "ArchivistOfGondor", archivist),
    ("m", "MalignantGrowth", malignant_growth), ("w", "WellOfIdeas", well_of_ideas),
    ("s", "SmileAtDeath", smile_at_death),
]

# What each card must say after the edit (the script fails if an edit didn't apply).
CHECKS = {
    "DeadwoodTreefolk": VANISHING_IF, "GhazbNOgre": "APlayerHasMoreLifeThanEachOtherPlayer()",
    "WildDogs": "APlayerHasMoreLifeThanEachOtherPlayer()", "ParadoxHaze": "new FirstUpkeepThisTurn()",
    "AllHallowsEve": "new SourceCardInExileWithScreamCounter()", "ColfenorsUrn": "new SourceExiledCardsThreshold(3)",
    "ArchivistOfGondor": "new ActivePlayerIsMonarch()",
    "MalignantGrowth": "drawStep(ActivePlayer.OPPONENT)", "WellOfIdeas": "drawStep(ActivePlayer.OPPONENT)",
    "SmileAtDeath": "2, false, false),\n",
    "HellfireMongrel": "new ActivePlayerHandAtMost(2)", "LavabornMuse": "new ActivePlayerHandAtMost(2)",
    "PaupersCage": "new ActivePlayerHandAtMost(2)", "PrickleFaeries": "new ActivePlayerHandAtMost(2)",
}


def path_of(letter, name):
    if "/" in name:
        sub, cls = name.split("/")
        return CARDS / letter / "src/main/java" / PKG / sub / f"{cls}.java"
    return CARDS / letter / "src/main/java" / PKG / letter / f"{name}.java"


def main():
    letters = set(sys.argv[1:])
    for letter, name, edit in EDITS:
        if letters and letter not in letters:
            continue
        path = path_of(letter, name)
        text = path.read_text()
        new = edit(text)
        cls = name.split("/")[-1]
        check = CHECKS.get(cls, VANISHING_IF)
        if check not in new:
            raise SystemExit(f"{cls}: the edit did not apply")
        if "DealDamageIfFewCardsInHandEffect" in new or "forOpponentDrawStep" in new:
            raise SystemExit(f"{cls}: an old form is left")
        if new != text:
            path.write_text(new)
            print(f"edited {path.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
