#!/usr/bin/env python3
"""Step 14, run 15 of effect-slot-refactor.md: the abilities of step 13's family 11 (player and keyword actions,
Planechase, control change, attaching, crew, saddle and station, day and night, Rooms, state triggers) lose their
trigger-only wrappers.

- "This ability triggers only once each turn" (new OncePerTurnTriggerEffect(e)) is TriggerLimit.ONCE_EACH_TURN: the
  crime abilities, Curator of Sun's Creation (discover) and Inchblade Companion (attach, MANUAL: its token copy drops
  the ability by its CreateTokenCopyOfSourceEffect now).
- "Becomes crewed / saddled for the first time each turn" is its own event (MANUAL: Mindlink Mech, Stubborn
  Burrowfiend): Triggers.becomesCrewedForFirstTimeEachTurn(), becomesSaddledForFirstTimeEachTurn().
- A two-argument ConditionalEffect directly under the trigger is the intervening "if" (CR 603.4) on the cards of
  INTERVENING (Knights of the Black Rose, Overzealous Muscle's "during your turn", Barbarian Class's level); the
  ones of KEEP stay effects (an "if" that doesn't follow the trigger condition, or a die result, which is a condition
  of the event: Valor's Reach, Pompeii, Mr. House), and one the tables don't decide stops the script.
- "When you unlock this door" (new TriggeringRoomDoorConditionalEffect(n, e) under Triggers.doorUnlocked()) is
  Triggers.doorUnlocked(n).
- A state trigger's StateTriggerEffect is its game state: Triggers.state(predicate), sourceState(…),
  battlefieldHasAtMost(…), battlefieldHasAtLeast(…), with .withSource(…), .withBattlefieldAtMost(…) and
  .referencing(…) for the rest, and the record's effects are the ability's. Its description is dropped (the reader
  names the ability after the card). MANUAL: the four "when …, if this permanent is an enchantment, …" cards (Opal
  Avenger, Lurking Jackals, Veiled Crocodile, Hidden Predators), whose intervening "if" is an option now.
- MANUAL: the explore and exploit card filters (Nicanzil's two abilities, Henry Wu) and the scheme triggers (Plots
  That Span Centuries, My Laughter Echoes), whose SchemeSetInMotionTriggerEffect is the trigger's filter and effects.

The peeling, re-indenting and import fixing are run 13's (scripts/step14-life-options.py). Running it again changes
nothing.

Usage: python3 scripts/step14-misc-options.py [--dry-run] [--report]
"""
import importlib.util
import os
import re
import sys

HERE = os.path.dirname(__file__)


def load(name, file):
    spec = importlib.util.spec_from_file_location(name, os.path.join(HERE, file))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


life = load("life_options", "step14-life-options.py")
saga = load("saga_options", "step14-saga-options.py")
saga_placed = saga.placed
scan = load("misc_scan", "step14-misc-scan.py")
close_paren, split_args, placed, column = life.close_paren, life.split_args, life.placed, life.column

life.FAMILY = re.compile(r"Triggers\.(" + "|".join(scan.FACTORIES) + r")\(")
life.INTERVENING = {"KnightsOfTheBlackRose", "OverzealousMuscle", "BarbarianClass"}
life.KEEP = {"ValorsReach", "Pompeii", "MrHousePresidentAndCEO"}

PKG = "com.github.laxika.magicalvibes"
life.tap.IMPORTS = {
    "TriggerOptions": PKG + ".model.trigger.TriggerOptions",
    "TriggerLimit": PKG + ".model.trigger.TriggerLimit",
    "SourceMatchesPermanentPredicate": PKG + ".model.condition.SourceMatchesPermanentPredicate",
    "PermanentIsEnchantmentPredicate": PKG + ".model.filter.PermanentIsEnchantmentPredicate",
    "List": "java.util.List",
}
life.tap.REMOVABLE = ["OncePerTurnTriggerEffect", "ConditionalEffect", "TriggeringRoomDoorConditionalEffect",
                      "StateTriggerEffect", "SchemeSetInMotionTriggerEffect", "TriggeringCardConditionalEffect",
                      "AnyPlayerControlsPermanent", "PermanentAllOfPredicate", "PermanentIsSourcePermanentPredicate",
                      "PermanentIsEnchantmentPredicate", "List", "CardEffect"]

ENCHANTMENT_IF = ("TriggerOptions.DEFAULT.withInterveningIf(\n"
                  "                        new SourceMatchesPermanentPredicate(new PermanentIsEnchantmentPredicate()))")

life.MANUAL = {
    "MindlinkMech": [("""                .addTrigger(Triggers.becomesCrewed(),
                        new OncePerTurnTriggerEffect(new BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(
                                4, 3, Set.of(CardType.ARTIFACT), Set.of(CardSubtype.VEHICLE),
                                Set.of(Keyword.FLYING))));
""", """                .addTrigger(Triggers.becomesCrewedForFirstTimeEachTurn(),
                        new BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(
                                4, 3, Set.of(CardType.ARTIFACT), Set.of(CardSubtype.VEHICLE),
                                Set.of(Keyword.FLYING)));
""")],
    "StubbornBurrowfiend": [("""        addTrigger(Triggers.becomesSaddled(), new OncePerTurnTriggerEffect(
                SequenceEffect.of(
                        new MillEffect(2, MillRecipient.CONTROLLER),
                        new BoostSelfEffect(creatureCardsInGraveyard, creatureCardsInGraveyard))));
""", """        addTrigger(Triggers.becomesSaddledForFirstTimeEachTurn(), SequenceEffect.of(
                new MillEffect(2, MillRecipient.CONTROLLER),
                new BoostSelfEffect(creatureCardsInGraveyard, creatureCardsInGraveyard)));
""")],
    "InchbladeCompanion": [("""        addTrigger(Triggers.becomesAttachedToCreature(),
                new OncePerTurnTriggerEffect(
                        CreateTokenCopyOfSourceEffect.withoutSourceEffect(OncePerTurnTriggerEffect.class)));
""", """        addTrigger(Triggers.becomesAttachedToCreature(),
                TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN),
                CreateTokenCopyOfSourceEffect.withoutSourceEffect(CreateTokenCopyOfSourceEffect.class));
""")],
    "NicanzilCurrentConductor": [("""        addTrigger(Triggers.creatureYouControlExplores(), new TriggeringCardConditionalEffect(
                new CardTypePredicate(CardType.LAND),
                new MayEffect(
                        new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.LAND), "land", true),
                        "Put a land card from your hand onto the battlefield tapped?")),
                new TriggeringCardConditionalEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                        new PutCountersOnSourceEffect(1, 1, 1)));
""", """        addTrigger(Triggers.creatureYouControlExplores().withFilter(new CardTypePredicate(CardType.LAND)),
                new MayEffect(
                        new PutCardToBattlefieldEffect(new CardTypePredicate(CardType.LAND), "land", true),
                        "Put a land card from your hand onto the battlefield tapped?"));
        addTrigger(Triggers.creatureYouControlExplores()
                        .withFilter(new CardNotPredicate(new CardTypePredicate(CardType.LAND))),
                new PutCountersOnSourceEffect(1, 1, 1));
""")],
    "HenryWuInGenGeneticist": [("""        addTrigger(Triggers.creatureYouControlExploits(),
                new TriggeringCardConditionalEffect(
                        new CardNotPredicate(new CardSubtypePredicate(CardSubtype.HUMAN)),
                        SequenceEffect.of(
                                new DrawCardEffect(1),
                                new ConditionalEffect(new EventValueAtLeast(3),
                                        CreateTokenEffect.ofTreasureToken(1)))));
""", """        addTrigger(Triggers.creatureYouControlExploits()
                        .withFilter(new CardNotPredicate(new CardSubtypePredicate(CardSubtype.HUMAN))),
                SequenceEffect.of(
                        new DrawCardEffect(1),
                        new ConditionalEffect(new EventValueAtLeast(3), CreateTokenEffect.ofTreasureToken(1))));
""")],
    "PlotsThatSpanCenturies": [("""        addTrigger(Triggers.youSetSchemeInMotion(),
                new SchemeSetInMotionTriggerEffect(List.of(
                        new RegisterNextSchemeSetInMotionReplacementEffect())));
""", """        addTrigger(Triggers.youSetSchemeInMotion(), new RegisterNextSchemeSetInMotionReplacementEffect());
""")],
    "MyLaughterEchoes": [("""        addTrigger(Triggers.youSetSchemeInMotion(),
                new SchemeSetInMotionTriggerEffect(NON_ONGOING_SCHEME, List.of(
                        new MayEffect(
                                new AbandonSchemeAndSetTriggeringSchemeInMotionAgainEffect(),
                                "Abandon this scheme and set that scheme in motion again?"))));
""", """        addTrigger(Triggers.youSetSchemeInMotion().withFilter(NON_ONGOING_SCHEME),
                new MayEffect(
                        new AbandonSchemeAndSetTriggeringSchemeInMotionAgainEffect(),
                        "Abandon this scheme and set that scheme in motion again?"));
""")],
    "OpalAvenger": [("""        addTrigger(Triggers.state(), new StateTriggerEffect(
                (gameData, sourcePermanent, controllerId) -> gameData.getLife(controllerId) <= 10,
                new PermanentIsEnchantmentPredicate(), null, 0, null,
                List.of(new ConditionalEffect(
                        new SourceMatchesPermanentPredicate(new PermanentIsEnchantmentPredicate()),
                        new BecomeCreatureEffect(3, 5, CardSubtype.SOLDIER))),
                "Opal Avenger's state-triggered ability"
        ));
""", """        addTrigger(Triggers.state((gameData, sourcePermanent, controllerId) -> gameData.getLife(controllerId) <= 10),
                %s,
                new BecomeCreatureEffect(3, 5, CardSubtype.SOLDIER));
""" % ENCHANTMENT_IF)],
    "LurkingJackals": [("""        addTrigger(Triggers.state(), new StateTriggerEffect(
                (gameData, sourcePermanent, controllerId) -> gameData.orderedPlayerIds.stream()
                        .filter(playerId -> !playerId.equals(controllerId))
                        .anyMatch(playerId -> gameData.getLife(playerId) <= 10),
                new PermanentIsEnchantmentPredicate(), null, 0, 0, null,
                List.of(new ConditionalEffect(new AnyPlayerControlsPermanent(new PermanentAllOfPredicate(List.of(
                        new PermanentIsSourcePermanentPredicate(), new PermanentIsEnchantmentPredicate()))),
                        new BecomeCreatureEffect(3, 2, CardSubtype.JACKAL))),
                "Lurking Jackals's state-triggered ability"
        ));
""", """        addTrigger(Triggers.state((gameData, sourcePermanent, controllerId) -> gameData.orderedPlayerIds.stream()
                        .filter(playerId -> !playerId.equals(controllerId))
                        .anyMatch(playerId -> gameData.getLife(playerId) <= 10)),
                %s,
                new BecomeCreatureEffect(3, 2, CardSubtype.JACKAL));
""" % ENCHANTMENT_IF)],
    "VeiledCrocodile": [("""        addTrigger(Triggers.state(), new StateTriggerEffect(
                (gameData, sourcePermanent, controllerId) -> gameData.orderedPlayerIds.stream()
                        .anyMatch(playerId -> gameData.playerHands.getOrDefault(playerId, List.of()).isEmpty()),
                new PermanentIsEnchantmentPredicate(),
                null, 0, 0, null,
                List.of(new BecomeCreatureEffect(4, 4, CardSubtype.CROCODILE)),
                "Veiled Crocodile's state-triggered ability"
        ));
""", """        addTrigger(Triggers.state((gameData, sourcePermanent, controllerId) -> gameData.orderedPlayerIds.stream()
                        .anyMatch(playerId -> gameData.playerHands.getOrDefault(playerId, List.of()).isEmpty())),
                %s,
                new BecomeCreatureEffect(4, 4, CardSubtype.CROCODILE));
""" % ENCHANTMENT_IF)],
    "HiddenPredators": [("""        addTrigger(Triggers.state(), new StateTriggerEffect(
                null, new PermanentIsEnchantmentPredicate(), null, 0, 0,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentPowerAtLeastPredicate(4),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))),
                List.of(new BecomeCreatureEffect(4, 4, CardSubtype.BEAST)),
                "Hidden Predators's state-triggered ability"
        ));
""", """        addTrigger(Triggers.battlefieldHasAtLeast(1, new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentPowerAtLeastPredicate(4),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())))),
                %s,
                new BecomeCreatureEffect(4, 4, CardSubtype.BEAST));
""" % ENCHANTMENT_IF)],
}

DOOR = re.compile(r"addTrigger\(Triggers\.doorUnlocked\(\),\s*new TriggeringRoomDoorConditionalEffect\(")
STATE = re.compile(r"addTrigger\(Triggers\.state\(\),\s*(?:new StateTriggerEffect|StateTriggerEffect\.(\w+))\(")
ADD = re.compile(r"addTrigger\(")
LIMIT = 120


def layout(src, start, end, parts, indent):
    """addTrigger(parts…) for src[start:end]: one line when it fits, else the first part after "addTrigger(" and the
    others each on their own line at indent + 8."""
    head = "addTrigger("
    one = head + ", ".join(parts) + ")"
    if "\n" not in one and column(src, start) + len(one) + 1 <= LIMIT:
        return one
    return head + (",\n" + " " * (indent + 8)).join(parts) + ")"


def indent_of(src, i):
    line_start = src.rfind("\n", 0, i) + 1
    line = src[line_start:i]
    return len(line) - len(line.lstrip())


def door_edits(src, name, report):
    edits = []
    for m in DOOR.finditer(src):
        end = close_paren(src, src.index("(", m.start()) + 1)
        args = split_args(src, m.end(), close_paren(src, m.end()) - 1)
        if len(args) != 2:
            raise SystemExit("%s: a door wrapper the script doesn't read" % name)
        door = src[args[0][0]:args[0][1]]
        indent = indent_of(src, m.start())
        effect = saga_placed(src, args[1], indent + 8)
        new = layout(src, m.start(), end, ["Triggers.doorUnlocked(%s)" % door, effect], indent)
        report.append("%s\tdoor\t%s" % (name, re.sub(r"\s+", " ", new)))
        edits.append((m.start(), end, new))
    return edits


def state_trigger(src, method, args, name):
    """The trigger expression of a StateTriggerEffect call's arguments, and the span of its effects."""
    vals = [src[a:b] for a, b in args]
    if method == "whenBattlefieldHasAtMost":
        p = s = r = None
        b, mx, mn = args[1], vals[0], "0"
        effects = args[2]
    elif method == "whenBattlefieldHasAtLeast":
        p = s = r = None
        b, mx, mn = args[1], "Integer.MAX_VALUE", vals[0]
        effects = args[2]
    elif method is None and len(args) == 3:
        first = vals[0]
        lam = "->" in first
        p, s, b, mx, mn, r = (args[0] if lam else None), (None if lam else args[0]), None, "0", "0", None
        effects = args[1]
    elif method is None and len(args) == 4:
        p, s, b, mx, mn, r = args[0], None, None, "0", "0", args[1]
        effects = args[2]
    elif method is None and len(args) in (7, 8):
        p, s, b, mx = args[0], args[1], args[2], vals[3]
        mn = "0" if len(args) == 7 else vals[4]
        r = args[4] if len(args) == 7 else args[5]
        effects = args[-2]
    else:
        raise SystemExit("%s: a StateTriggerEffect the script doesn't read" % name)

    def present(span):
        return span is not None and src[span[0]:span[1]] != "null"

    p, s, b, r = [x if present(x) else None for x in (p, s, b, r)]
    return p, s, b, mx, mn, r, effects


def state_edits(src, name, report):
    edits = []
    for m in STATE.finditer(src):
        add_end = close_paren(src, src.index("(", m.start()) + 1)
        args = split_args(src, m.end(), close_paren(src, m.end()) - 1)
        p, s, b, mx, mn, r, effects_span = state_trigger(src, m.group(1), args, name)
        indent = indent_of(src, m.start())
        cont = indent + 16

        calls = []
        if p:
            calls.append(("Triggers.state", [p]))
        elif s:
            calls.append(("Triggers.sourceState", [s]))
        elif b:
            if mn == "0":
                calls.append(("Triggers.battlefieldHasAtMost", [mx, b]))
            elif mx == "Integer.MAX_VALUE":
                calls.append(("Triggers.battlefieldHasAtLeast", [mn, b]))
            else:
                raise SystemExit("%s: a battlefield count with both bounds" % name)
        else:
            raise SystemExit("%s: a state trigger without a game state" % name)
        if p and s:
            calls.append((".withSource", [s]))
        if b and (p or s):
            if mn != "0":
                raise SystemExit("%s: a battlefield minimum next to another condition" % name)
            calls.append((".withBattlefieldAtMost", [mx, b]))
        if r:
            calls.append((".referencing", [r]))

        def render(chained_col):
            out, line_indent, col = "", indent + 8, column(src, m.start()) + len("addTrigger(")
            for i, (fn, fargs) in enumerate(calls):
                if i > 0:
                    out += "\n" + " " * chained_col
                    line_indent, col = chained_col, chained_col
                out += fn + "("
                col += len(fn) + 1
                for j, a in enumerate(fargs):
                    if j > 0:
                        out += ", "
                        col += 2
                    t = a if isinstance(a, str) else src[a[0]:a[1]]
                    if "\n" not in t and col + len(t) + 2 > LIMIT:
                        out += "\n" + " " * (line_indent + 8)
                        line_indent, col = line_indent + 8, line_indent + 8
                    if not isinstance(a, str):
                        t = saga_placed(src, a, line_indent)
                    out += t
                    col = col + len(t) if "\n" not in t else len(t.split("\n")[-1])
                out += ")"
                col += 1
            return out

        trigger = render(cont)
        if len(calls) > 1:
            flat = trigger.replace("\n" + " " * cont, "")
            if "\n" not in flat and column(src, m.start()) + len("addTrigger(") + len(flat) + 1 <= LIMIT:
                trigger = flat
        es, ee = effects_span
        etext = src[es:ee]
        if not etext.startswith("List.of("):
            raise SystemExit("%s: state trigger effects that aren't a List.of: %s" % (name, etext))
        eargs = split_args(src, es + len("List.of("), ee - 1)
        effects = [saga_placed(src, a, indent + 8) for a in eargs]
        new = layout(src, m.start(), add_end, [trigger] + effects, indent)
        report.append("%s\tstate\t%s" % (name, re.sub(r"\s+", " ", new)))
        edits.append((m.start(), add_end, new))
    return edits


_convert = life.convert


def convert(src, name, report):
    if name in life.MANUAL:
        return _convert(src, name, report)
    edits = door_edits(src, name, report) + state_edits(src, name, report)
    for start, end, new in sorted(edits, reverse=True):
        src = src[:start] + new + src[end:]
    return _convert(src, name, report)


life.convert = convert

if __name__ == "__main__":
    life.main()
