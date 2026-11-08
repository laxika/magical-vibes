#!/usr/bin/env python3
"""Step 15, run 17x: the storm family's copy counts read the stack entry.

Rewrites every card class whose "when you cast this spell, copy it for each …" ability was a record the self-cast
collector judged by class and rebuilt into a `StormCopyEffect(snapshot, caster, copies)`. Each becomes the existing
`CopyTriggeringSpellEffect` with its count an amount read as the ability resolves (`withCopies(DynamicAmount)`):

- storm (`StormEffect`): `CopyTriggeringSpellEffect.storm()`, every player's spells cast before the triggering spell
  this turn (`SpellsCastBeforeTriggeringSpellThisTurn.byAnyPlayer()`); `StormEffect(true)` adds
  `permanentSpellCopiesAreTokens()`, Aeve's `tokenCopyWithoutLegendary()` also `withoutSupertypes(LEGENDARY)`;
  Prismari's granted storm and Indris's conjured Lightning Bolt the same; Ral, Crackling Wit's emblem a triggered
  ability ("whenever you cast an instant or sorcery spell, copy it for each spell cast before it this turn");
- Show of Confidence (`StormEffect(false, true)`): `SpellsCastThisTurn(instant or sorcery, CONTROLLER, true)`;
- replicate (`ReplicateEffect(cost[, tokenCopy])`): the intervening "if" `RepeatedAdditionalCostPaid(cost)` and
  `RepeatedAdditionalCostCount(cost)`; Exterminate's tap replicate `SpellXAtLeast(1)` and `TriggeringSpellXValue`;
- "copy it X times" (`CopyThisSpellForXValueEffect`): `TriggeringSpellXValue`;
- gravestorm: the new `PermanentsPutIntoGraveyardFromBattlefieldThisTurn`; coststorm: the new
  `DistinctManaValuesAmongOtherSpellsAndLandsPlayedThisTurn`; the counter kinds:
  `DistinctCounterKindsAmongControlledPermanents`; the commander casts: `CommanderCastsFromCommandZoneThisGame`
  (`CopyThisSpellForEachCommanderCastEffect()`'s token copies `permanentSpellCopiesAreTokens()`).

Idempotent: a file already converted is left alone. Per letter module ("shared" for the shared module).

Usage: step15-run17x-cards.py <letter|shared> [...]   (or "all")
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"

COPY = "model.effect.CopyTriggeringSpellEffect"
OLD_IMPORTS = [
    "model.effect.StormEffect", "model.effect.ReplicateEffect", "model.effect.CopyThisSpellForXValueEffect",
    "model.effect.GravestormEffect", "model.effect.CoststormEffect",
    "model.effect.CopyThisSpellForEachCounterKindEffect", "model.effect.CopyThisSpellForEachCommanderCastEffect",
]

INSTANT_OR_SORCERY = """CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));"""
INSTANT_OR_SORCERY_IMPORTS = ["model.CardType", "model.filter.CardAnyOfPredicate", "model.filter.CardTypePredicate",
                              "java:java.util.List"]

# card -> [(old, new)], imports to add: the cards whose ability isn't one of the plain forms below.
SPECIAL = {
    "ShowOfConfidence": ([
        ("""        addTrigger(Triggers.youCastThisSpell(), new StormEffect(false, true));""",
         """        """ + INSTANT_OR_SORCERY + """
        addTrigger(Triggers.youCastThisSpell(), new CopyTriggeringSpellEffect()
                .withCopies(new SpellsCastThisTurn(instantOrSorcery, CountScope.CONTROLLER, true)));"""),
    ], [COPY, "model.amount.SpellsCastThisTurn", "model.amount.CountScope"] + INSTANT_OR_SORCERY_IMPORTS),
    "RalCracklingWit": ([
        ("""                List.of(new DrawCardEffect(3), new CreateEmblemEffect(
                        List.of(StormEffect.forInstantOrSorcery()),
                        "Instant and sorcery spells you cast have storm.")),""",
         """                List.of(new DrawCardEffect(3), new CreateEmblemEffect(
                        new TriggeredAbility(Triggers.youCastSpell().withFilter(new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY)))),
                                CopyTriggeringSpellEffect.storm()),
                        "Instant and sorcery spells you cast have storm.")),"""),
    ], [COPY, "model.filter.CardAnyOfPredicate"]),
    "AeveProgenitorOoze": ([
        ("""        addTrigger(Triggers.youCastThisSpell(), StormEffect.tokenCopyWithoutLegendary());""",
         """        addTrigger(Triggers.youCastThisSpell(), CopyTriggeringSpellEffect.storm().permanentSpellCopiesAreTokens()
                .withoutSupertypes(Set.of(CardSupertype.LEGENDARY)));"""),
    ], [COPY, "model.CardSupertype", "java:java.util.Set"]),
    "Exterminate": ([
        ("""        addTrigger(Triggers.youCastThisSpell(), ReplicateEffect.forTapCost(
                new PermanentHasSubtypePredicate(CardSubtype.DALEK)));""",
         """        addTrigger(Triggers.youCastThisSpell(),
                TriggerOptions.DEFAULT.withInterveningIf(new SpellXAtLeast(1)),
                new CopyTriggeringSpellEffect().withCopies(new TriggeringSpellXValue()));"""),
    ], [COPY, "model.trigger.TriggerOptions", "model.condition.SpellXAtLeast", "model.amount.TriggeringSpellXValue"]),
}

# effect expression -> (replacement, imports): the plain forms, wherever the card writes them.
PLAIN = [
    ("new StormEffect(true, false)", "CopyTriggeringSpellEffect.storm().permanentSpellCopiesAreTokens()", [COPY]),
    ("new StormEffect(true)", "CopyTriggeringSpellEffect.storm().permanentSpellCopiesAreTokens()", [COPY]),
    ("new StormEffect()", "CopyTriggeringSpellEffect.storm()", [COPY]),
    ("new CopyThisSpellForXValueEffect()", "new CopyTriggeringSpellEffect().withCopies(new TriggeringSpellXValue())",
     [COPY, "model.amount.TriggeringSpellXValue"]),
    ("new GravestormEffect()",
     "new CopyTriggeringSpellEffect().withCopies(new PermanentsPutIntoGraveyardFromBattlefieldThisTurn())",
     [COPY, "model.amount.PermanentsPutIntoGraveyardFromBattlefieldThisTurn"]),
    ("new CoststormEffect()",
     "new CopyTriggeringSpellEffect().withCopies(new DistinctManaValuesAmongOtherSpellsAndLandsPlayedThisTurn())",
     [COPY, "model.amount.DistinctManaValuesAmongOtherSpellsAndLandsPlayedThisTurn"]),
    ("new CopyThisSpellForEachCounterKindEffect()",
     "new CopyTriggeringSpellEffect().withCopies(new DistinctCounterKindsAmongControlledPermanents())",
     [COPY, "model.amount.DistinctCounterKindsAmongControlledPermanents"]),
    ("new CopyThisSpellForEachCommanderCastEffect(false)",
     "new CopyTriggeringSpellEffect().withCopies(new CommanderCastsFromCommandZoneThisGame())",
     [COPY, "model.amount.CommanderCastsFromCommandZoneThisGame"]),
    ("new CopyThisSpellForEachCommanderCastEffect()",
     "new CopyTriggeringSpellEffect().permanentSpellCopiesAreTokens()\n"
     "                .withCopies(new CommanderCastsFromCommandZoneThisGame())",
     [COPY, "model.amount.CommanderCastsFromCommandZoneThisGame"]),
]

REPLICATE = re.compile(r'^(?P<indent> *)addTrigger\(Triggers\.youCastThisSpell\(\), new ReplicateEffect\('
                       r'"(?P<cost>[^"]+)"(?P<token>, true)?\)\);$', re.M)
REPLICATE_IMPORTS = [COPY, "model.trigger.TriggerOptions", "model.condition.RepeatedAdditionalCostPaid",
                     "model.amount.RepeatedAdditionalCostCount"]


def import_line(name):
    return f"import {name[5:]};" if name.startswith("java:") else f"import {PKG}.{name};"


def replicate(match):
    indent = match.group("indent")
    cost = match.group("cost")
    tokens = (f".permanentSpellCopiesAreTokens()\n{indent}                " if match.group("token") else "")
    return (f'{indent}addTrigger(Triggers.youCastThisSpell(),\n'
            f'{indent}        TriggerOptions.DEFAULT.withInterveningIf(new RepeatedAdditionalCostPaid("{cost}")),\n'
            f'{indent}        new CopyTriggeringSpellEffect(){tokens}.withCopies(new RepeatedAdditionalCostCount("{cost}")));')


def wrap_long_lines(text, replacement):
    """Moves a plain replacement that made its line too long onto a continuation line of its own."""
    lines = text.split("\n")
    out = []
    for line in lines:
        first = replacement.split("\n")[0]
        if first in line and len(line) > 120 and not line.strip().startswith(first):
            head, tail = line.split(first, 1)
            indent = len(line) - len(line.lstrip())
            continuation = " " * (indent + 8)
            out.append(head.rstrip())
            out.append(continuation + first + tail)
        else:
            out.append(line)
    return "\n".join(out)


def add_imports(text, add):
    lines = text.split("\n")
    for cls in add:
        line = import_line(cls)
        if line in lines:
            continue
        imports = [i for i, l in enumerate(lines) if l.startswith("import ")]
        same_kind = [i for i in imports if lines[i].startswith("import java.") == line.startswith("import java.")]
        if same_kind:
            position = next((i for i in same_kind if lines[i] > line), same_kind[-1] + 1)
        else:
            position = imports[-1] + 1
            lines.insert(position, "")
            position += 1
        lines.insert(position, line)
    return "\n".join(lines)


def convert(path):
    name = path.stem
    text = path.read_text()
    original = text
    add = []
    if name in SPECIAL:
        edits, imports = SPECIAL[name]
        for old, new in edits:
            if new in text:
                continue
            if old not in text:
                raise SystemExit(f"{name}: expected text not found:\n{old}")
            text = text.replace(old, new)
        add += imports
    if REPLICATE.search(text):
        text = REPLICATE.sub(replicate, text)
        add += REPLICATE_IMPORTS
    for old, new, imports in PLAIN:
        if old in text:
            text = text.replace(old, new)
            text = wrap_long_lines(text, new)
            add += imports
    if text == original:
        return
    for cls in OLD_IMPORTS:
        text = text.replace(import_line(cls) + "\n", "")
    for leftover in ("StormEffect", "ReplicateEffect", "CopyThisSpellForXValueEffect", "GravestormEffect",
                     "CoststormEffect", "CopyThisSpellForEachCounterKindEffect",
                     "CopyThisSpellForEachCommanderCastEffect"):
        if re.search(r"\b" + leftover + r"\b", text):
            raise SystemExit(f"{name}: still names {leftover}")
    text = add_imports(text, add)
    path.write_text(text)
    print(f"converted {name}")


def main():
    modules = ([*"abcdefghijklmnopqrstuvwxyz", "shared"] if sys.argv[1:] == ["all"] else sys.argv[1:])
    if not modules:
        raise SystemExit(__doc__)
    for module in modules:
        base = ROOT / f"magical-vibes-card/{module}/src/main/java/com/github/laxika/magicalvibes/cards"
        for path in sorted(base.rglob("*.java")):
            text = path.read_text()
            if re.search(r"\b(StormEffect|ReplicateEffect|CopyThisSpellForXValueEffect|GravestormEffect|CoststormEffect"
                         r"|CopyThisSpellForEachCounterKindEffect|CopyThisSpellForEachCommanderCastEffect)\b", text):
                convert(path)


if __name__ == "__main__":
    main()
