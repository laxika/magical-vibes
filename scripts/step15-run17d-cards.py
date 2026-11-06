#!/usr/bin/env python3
"""Step 15 run 17d of effect-slot-refactor.md: card classes whose trigger-event data no collector binds any more.

Idempotent; run per letter module (``--module c``) or with no argument for every module this run touches.

- Curse of Predation: "whenever a creature attacks enchanted player" is the trigger's filter
  (``PermanentIsAttackingEnchantedPlayerPredicate``, as Curse of the Forsaken writes it) and "put a +1/+1 counter on
  it" the existing non-targeting counter effect on the triggering permanent, where a marker effect had the attack
  collector check the attachment and rebuild it into a counter effect.
- The damage triggers whose target is one "that player controls" (the player dealt damage): every
  ``…DamagedPlayerControls…`` effect the collectors rebuilt with the player bound in is the existing targeted effect
  with ``PermanentControlledByTriggeringPlayerPredicate`` in its target restriction (``DestroyTargetPermanentEffect``,
  ``ExileTargetPermanentEffect``, ``ReturnToHandEffect.target``, ``GainControlOfTargetEffect.withTargetPredicate``,
  ``DealDamageToTargetCreatureEffect``, ``SacrificeSelfThenEffect`` of a destroy, ``TransformSelfAndAttachToTargetCreatureEffect``,
  and the tap effects ``TapPermanentsEffect(TARGET, …)`` with ``DoesntUntapEffect`` / ``SkipNextUntapEffect``);
  Ashling's sacrifice keeps its class without the bound player. Deus of Calamity's "6 or more damage" is the
  trigger's event condition (``EventValueAtLeast(6)``), Satyr Firedancer's "deals damage to an opponent" the new
  ``Triggers.instantOrSorceryYouControlDealsDamageToOpponent()``, Bladegriff Prototype's "of that player's choice" the
  card's ``setCombatTriggerTargetChosenByDefendingPlayer(true)`` (Erithizon's flag, renamed from
  ``setAttackTriggerTargetChosenByDefendingPlayer``). Dokuchi Silencer's reflexive destroy keeps
  ``DestroyPermanentDamagedPlayerControlsEffect`` without its minimum damage; Flaming Gambit (a spell) is unchanged.
"""
import argparse
import importlib.util
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
CARDS = ROOT / "magical-vibes-card"
PKG = "src/main/java/com/github/laxika/magicalvibes/cards"
EFFECT = "com.github.laxika.magicalvibes.model.effect"
FILTER = "com.github.laxika.magicalvibes.model.filter"

_spec = importlib.util.spec_from_file_location("strip", pathlib.Path(__file__).with_name("strip-unused-imports.py"))
STRIP = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(STRIP)

EDITS = {
    ("c", "CurseOfPredation"): [
        ("""        addTrigger(Triggers.creatureAttacks(),
                new PutCounterOnAttackingCreatureOnAttacksYouEffect(CounterType.PLUS_ONE_PLUS_ONE));""",
         """        addTrigger(Triggers.creatureAttacks().withFilter(new PermanentIsAttackingEnchantedPlayerPredicate()),
                new PutCounterOnEachMatchingPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1,
                        new PermanentIsTriggeringPermanentPredicate(), EachPermanentScope.ALL_PLAYERS));"""),
        (f"import {EFFECT}.PutCounterOnAttackingCreatureOnAttacksYouEffect;\n",
         f"import {EFFECT}.EachPermanentScope;\nimport {EFFECT}.PutCounterOnEachMatchingPermanentEffect;\n"
         f"import {FILTER}.PermanentIsAttackingEnchantedPlayerPredicate;\n"
         f"import {FILTER}.PermanentIsTriggeringPermanentPredicate;\n"),
    ],
}

# A marker that is present once a card's edits have been applied.
DONE = {
    "CurseOfPredation": "new PermanentIsAttackingEnchantedPlayerPredicate()",
}

TP = "new PermanentControlledByTriggeringPlayerPredicate()"
NOT_SOURCE = "new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())"


# The unstripped arguments of the last split_args call, for the builders that keep the original line breaks.
RAW_ARGS = []


def split_args(text, open_index):
    """The top-level arguments of the call whose '(' is at open_index, and the index after its ')'."""
    depth, args, start, i = 0, [], open_index + 1, open_index
    while True:
        ch = text[i]
        if ch == '"':
            i = text.index('"', i + 1)
            while text[i - 1] == "\\":
                i = text.index('"', i + 1)
        elif ch in "([{":
            depth += 1
        elif ch in ")]}":
            depth -= 1
            if depth == 0:
                args.append(text[start:i])
                RAW_ARGS[:] = [a for a in args if a.strip()]
                return [a.strip() for a in args if a.strip()], i + 1
        elif ch == "," and depth == 1:
            args.append(text[start:i])
            start = i + 1
        i += 1


def all_of(predicate):
    return f"new PermanentAllOfPredicate(List.of({predicate}, {TP}))"


def rewrite_calls(text, call, build):
    """Replaces every ``call(...)`` in text with build(args) (or build(args, indent), the indentation of its line)."""
    out, pos = [], 0
    while True:
        i = text.find(call + "(", pos)
        if i < 0:
            out.append(text[pos:])
            return "".join(out)
        if i > 0 and (text[i - 1].isalnum() or text[i - 1] == "."):
            out.append(text[pos:i + len(call)])
            pos = i + len(call)
            continue
        args, end = split_args(text, i + len(call))
        line_start = text.rfind("\n", 0, i) + 1
        indent = len(text[line_start:]) - len(text[line_start:].lstrip(" "))
        out.append(text[pos:i])
        built = build(args, indent) if build.__code__.co_argcount == 2 else build(args)
        out.append(text[i:end] if built is None else built)
        pos = end


def reindent(element, indent):
    """``element`` with its first line at ``indent`` and its continuation lines one level (8) deeper, kept relative."""
    lines = element.split("\n")
    rest = [line for line in lines[1:] if line.strip()]
    if not rest:
        return " " * indent + lines[0].strip()
    # Measured on the lines that aren't only closing brackets, which sit a level shallower than the lines they close.
    opening = [line for line in rest if not re.fullmatch(r"\s*[)\]}]+,?", line)] or rest
    base = min(len(line) - len(line.lstrip(" ")) for line in opening)
    shifted = [" " * indent + lines[0].strip()]
    for line in lines[1:]:
        shifted.append(" " * (indent + 8 + len(line) - len(line.lstrip(" ")) - base) + line.strip() if line.strip() else "")
    return "\n".join(shifted)


def format_tap_sequence(args, indent):
    """Lays a "tap target permanent that player controls" sequence out one step per line."""
    if not args or not args[0].startswith("new TapPermanentsEffect(TapUntapScope.TARGET") or TP not in args[0]:
        return None
    steps = ",\n".join(" " * (indent + 8) + " ".join(line.strip() for line in step.split("\n"))
                       .replace("List.of( ", "List.of(") for step in args)
    return "SequenceEffect.of(\n" + steps + ")"


def format_on_next_line(call, keep):
    """A builder that puts the last argument of ``call`` on a line of its own when it says "that player controls"."""
    def build(args, indent):
        if len(args) <= keep or TP not in args[-1] or args[-1].startswith("\n"):
            return None
        head = ", ".join(args[:-1])
        return call + "(" + head + ("," if head else "") + "\n" + reindent(args[-1], indent + 8) + ")"
    return build


def format_may(args, indent):
    """A "you may" whose effect says "that player controls" has its prompt on a line of its own."""
    if len(args) != 2 or TP not in args[0] or "\n" not in args[0]:
        return None
    first = RAW_ARGS[0].rstrip()
    return "new MayEffect(" + first + ",\n" + " " * (indent + 8) + args[1] + ")"


def fixed_damage(args):
    """``new DealDamageToTargetCreatureEffect(new Fixed(N), false, <that player>)`` written with its int constructor."""
    match = re.fullmatch(r"new Fixed\((\d+)\)", args[0]) if len(args) == 3 else None
    if match and args[1] == "false" and args[2] == TP:
        return f"new DealDamageToTargetCreatureEffect({match.group(1)}, {TP})"
    return None


LAYOUT = [
    ("new DealDamageToTargetCreatureEffect", fixed_damage),
    ("new DealDamageToTargetCreatureEffect", format_on_next_line("new DealDamageToTargetCreatureEffect", 1)),
    ("GainControlOfTargetEffect.withTargetPredicate",
     format_on_next_line("GainControlOfTargetEffect.withTargetPredicate", 1)),
    ("new SacrificeSelfThenEffect", format_on_next_line("new SacrificeSelfThenEffect", 0)),
    ("new MayEffect", format_may),
]


def format_all_of(args, indent):
    """Lays a ``new PermanentAllOfPredicate(List.of(…, <"that player controls">))`` out one element per line."""
    inner = args[0] if args else ""
    if not inner.startswith("List.of(") or (TP not in inner and NOT_SOURCE not in inner):
        return None
    elements, _ = split_args(inner, len("List.of"))
    if elements[-1] not in (TP, NOT_SOURCE):
        return None
    body = ",\n".join(reindent(element, indent + 8) for element in elements)
    return "new PermanentAllOfPredicate(List.of(\n" + body + "))"


def damage_amount(arg):
    return f"new Fixed({arg})" if arg.isdigit() else arg


REWRITES = [
    ("new DestroyPermanentDamagedPlayerControlsEffect",
     lambda a: f"new DestroyTargetPermanentEffect({all_of(a[0])})"),
    ("new ExilePermanentDamagedPlayerControlsEffect",
     lambda a: f"new ExileTargetPermanentEffect({all_of(a[0])})"),
    ("new ReturnPermanentDamagedPlayerControlsToHandEffect",
     lambda a: f"ReturnToHandEffect.target({all_of(a[0])})"),
    ("new GainControlOfPermanentDamagedPlayerControlsEffect",
     lambda a: f"GainControlOfTargetEffect.withTargetPredicate(ControlDuration.PERMANENT, {all_of(a[0])})"),
    ("new SacrificePermanentDamagedPlayerControlsEffect",
     lambda a: f"new SacrificePermanentDamagedPlayerControlsEffect({a[0]})"),
    ("new SacrificeSelfToDestroyCreatureDamagedPlayerControlsEffect",
     lambda a: f"new SacrificeSelfThenEffect(new DestroyTargetPermanentEffect({all_of('new PermanentIsCreaturePredicate()')}))"),
    ("new TransformSelfAndAttachToCreatureDamagedPlayerControlsEffect",
     lambda a: f"new TransformSelfAndAttachToTargetCreatureEffect({all_of('new PermanentIsCreaturePredicate()')})"),
    ("new DestroyPermanentChosenByDamagedPlayerAmongOpponentsEffect",
     lambda a: "new DestroyTargetPermanentEffect(new PermanentAllOfPredicate(List.of(" + a[0]
               + ", " + NOT_SOURCE + ")))"),
    ("TapChosenPermanentEffect.damagedPlayerControlsAndSkipsNextUntap",
     lambda a: f"SequenceEffect.of(new TapPermanentsEffect(TapUntapScope.TARGET, {all_of(a[0])}), "
               "new SkipNextUntapEffect(TapUntapScope.TARGET))"),
    ("TapChosenPermanentEffect.damagedPlayerControls",
     lambda a: f"SequenceEffect.of(new TapPermanentsEffect(TapUntapScope.TARGET, {all_of(a[0])}), "
               "DoesntUntapEffect.targetWhileSourceOnBattlefield())"),
]

# Dokuchi Silencer's reflexive destroy keeps its class (DiscardCardThenEffectHandler binds the player); only the
# minimum damage goes.
KEEP_DESTROY_CLASS = {"DokuchiSilencer"}

# "Destroy up to one target …" (Feline Sovereign): the old effect's handler offered an up-to-one choice as the ability
# resolved; the target is now chosen as it goes on the stack, from a target group of zero to one (Acolyte Hybrid's
# form). Applied to the rewritten text (an earlier version of this script wrapped it in a one-step sequence).
_FELINE_PREDICATE = """new PermanentAllOfPredicate(List.of(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsEnchantmentPredicate())),
                        new PermanentControlledByTriggeringPlayerPredicate()))"""
_FELINE_TRIGGER = """        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer().withFilter(cats),
                TriggerOptions.ONE_OR_MORE,
"""
_FELINE_TARGET = """        target(new PermanentPredicateTargetFilter(new PermanentAllOfPredicate(List.of(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsEnchantmentPredicate())),
                new PermanentControlledByTriggeringPlayerPredicate())),
                "Target must be an artifact or enchantment that player controls"), 0, 1)
                .addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer().withFilter(cats),
                        TriggerOptions.ONE_OR_MORE,
                        new DestroyTargetPermanentEffect());"""
UP_TO_ONE = {
    "FelineSovereign": [
        (_FELINE_TRIGGER + "                SequenceEffect.upToOneTarget(new DestroyTargetPermanentEffect("
         + _FELINE_PREDICATE + ")));", _FELINE_TARGET),
        (_FELINE_TRIGGER + "                new DestroyTargetPermanentEffect(" + _FELINE_PREDICATE + "));",
         _FELINE_TARGET),
    ],
}

IMPORTS = {
    "DestroyTargetPermanentEffect": EFFECT, "ExileTargetPermanentEffect": EFFECT, "ReturnToHandEffect": EFFECT,
    "GainControlOfTargetEffect": EFFECT, "ControlDuration": EFFECT, "SacrificeSelfThenEffect": EFFECT,
    "TransformSelfAndAttachToTargetCreatureEffect": EFFECT, "SequenceEffect": EFFECT, "TapPermanentsEffect": EFFECT,
    "TapUntapScope": EFFECT, "SkipNextUntapEffect": EFFECT, "DoesntUntapEffect": EFFECT,
    "DealDamageToTargetCreatureEffect": EFFECT,
    "PermanentAllOfPredicate": FILTER, "PermanentControlledByTriggeringPlayerPredicate": FILTER,
    "PermanentIsCreaturePredicate": FILTER, "PermanentNotPredicate": FILTER,
    "PermanentControlledBySourceControllerPredicate": FILTER, "PermanentPredicateTargetFilter": FILTER,
    "Fixed": "com.github.laxika.magicalvibes.model.amount",
    "EventValueAtLeast": "com.github.laxika.magicalvibes.model.condition",
    "TriggerOptions": "com.github.laxika.magicalvibes.model.trigger",
}

DAMAGED_PLAYER_CARDS = [
    ("a", "Aberrant"), ("a", "AshlingTheExtinguisher"), ("a", "ArmWithAether"), ("b", "BlindZealot"),
    ("b", "BladegriffPrototype"), ("c", "CommandoRaid"), ("c", "CausticWasps"), ("d", "DokuchiSilencer"),
    ("d", "DeusOfCalamity"), ("d", "DawningPurist"), ("f", "FearOfBurningAlive"), ("f", "FelineSovereign"),
    ("h", "HammerOfRuin"), ("l", "LightwielderPaladin"), ("l", "LatullasOrders"), ("m", "MordantDragon"),
    ("m", "ManaSkimmer"), ("o", "OKagachiVengefulKami"), ("p", "PolisCrusher"), ("r", "RustmouthOgre"),
    ("s", "SunderShaman"), ("s", "SparkMage"), ("s", "SoulSeizer"), ("s", "Somnophore"), ("s", "SoltariVisionary"),
    ("s", "SigilOfSleep"), ("s", "ScionOfCalamity"), ("s", "SatyrFiredancer"), ("t", "TrygonPredator"),
    ("t", "ThroatSlitter"), ("t", "TheSeaDevils"), ("w", "WakkaDevotedGuardian"), ("w", "WakeTheDragon"),
]

# Edits on top of the rewrites, applied first.
EXTRA = {
    "DeusOfCalamity": [
        ("""        addTrigger(Triggers.dealsDamageToOpponent(),
                new DestroyPermanentDamagedPlayerControlsEffect(new PermanentIsLandPredicate(), 6));""",
         """        addTrigger(Triggers.dealsDamageToOpponent(),
                TriggerOptions.DEFAULT.withEventCondition(new EventValueAtLeast(6)),
                new DestroyPermanentDamagedPlayerControlsEffect(new PermanentIsLandPredicate(), 0));"""),
    ],
    "SatyrFiredancer": [
        ("Triggers.instantOrSorceryYouControlDealsDamage()", "Triggers.instantOrSorceryYouControlDealsDamageToOpponent()"),
    ],
    "BladegriffPrototype": [
        ("""                        new PermanentNotPredicate(new PermanentIsLandPredicate())));""",
         """                        new PermanentNotPredicate(new PermanentIsLandPredicate())));
        setCombatTriggerTargetChosenByDefendingPlayer(true);"""),
    ],
}


# A marker that is present once a card's extra edits have been applied.
EXTRA_DONE = {
    "DeusOfCalamity": "new EventValueAtLeast(6)",
    "SatyrFiredancer": "instantOrSorceryYouControlDealsDamageToOpponent()",
    "BladegriffPrototype": "setCombatTriggerTargetChosenByDefendingPlayer(true)",
}

# Layout repairs of an earlier version of this script's output, applied only where that output is present.
REPAIRS = {
    "DokuchiSilencer": [
        ('                        "a creature card"), "Discard a creature card to destroy target creature or '
         'planeswalker that player controls?"));',
         '                        "a creature card"),\n'
         '                "Discard a creature card to destroy target creature or planeswalker that player '
         'controls?"));'),
        ("        addTrigger(Triggers.dealsCombatDamageToPlayer(), new MayEffect(new DiscardCardThenEffect(\n",
         "        addTrigger(Triggers.dealsCombatDamageToPlayer(), new MayEffect(\n"
         "                new DiscardCardThenEffect(\n"),
    ],
    "LatullasOrders": [
        ("        addTrigger(Triggers.dealsCombatDamageToPlayer(), new MayEffect(new DestroyTargetPermanentEffect(",
         "        addTrigger(Triggers.dealsCombatDamageToPlayer(), new MayEffect(\n"
         "                new DestroyTargetPermanentEffect("),
    ],
}


def add_imports(text, names):
    """Adds the imports of ``names`` the text uses, each in sorted position among the imports (moving it there)."""
    for name in sorted(names):
        line = f"import {IMPORTS[name]}.{name};\n"
        text = text.replace(line, "")
        if not re.search(r"\b" + name + r"\b", text.split("\npublic class", 1)[-1]):
            continue
        imports = [m for m in re.finditer(r"^import [\w.]+;\n", text, re.M)]
        after = next((m for m in imports if m.group(0) > line and not m.group(0).startswith("import java.")), None)
        if after is None:
            project = [m for m in imports if not m.group(0).startswith("import java.")]
            position = project[-1].end() if project else imports[0].start()
        else:
            position = after.start()
        text = text[:position] + line + text[position:]
    return text


def convert_damaged_player(letter, name):
    path = CARDS / letter / PKG / letter / f"{name}.java"
    text = path.read_text()
    original = text
    for old, new in EXTRA.get(name, []):
        if new in text or EXTRA_DONE.get(name, "\0") in text:
            continue
        if text.count(old) != 1:
            sys.exit(f"{path}: expected one occurrence of {old!r}")
        text = text.replace(old, new)
    for old, new in REPAIRS.get(name, []):
        text = text.replace(old, new)
    for call, build in REWRITES:
        if name in KEEP_DESTROY_CLASS and call == "new DestroyPermanentDamagedPlayerControlsEffect":
            text = rewrite_calls(text, call, lambda a, indent: "new DestroyPermanentDamagedPlayerControlsEffect(\n"
                                 + reindent(a[0], indent + 8) + ")")
            continue
        text = rewrite_calls(text, call, build)
    if name != "FlamingGambit":
        text = rewrite_calls(text, "new DealDamageToTargetCreatureDamagedPlayerControlsEffect",
                             lambda a: f"new DealDamageToTargetCreatureEffect({a[0]}, {TP})" if a[0].isdigit()
                             else f"new DealDamageToTargetCreatureEffect({a[0]}, false, {TP})"
                             if len(a) == 1 else "new DealDamageToTargetCreatureDamagedPlayerControlsEffect("
                             + ", ".join(a) + ")")
    text = rewrite_calls(text, "SequenceEffect.of", format_tap_sequence)
    for call, build in LAYOUT:
        text = rewrite_calls(text, call, build)
    text = rewrite_calls(text, "new PermanentAllOfPredicate", format_all_of)
    for old, new in UP_TO_ONE.get(name, []):
        text = text.replace(old, new)
    text = add_imports(text, IMPORTS.keys())
    if "List.of(" in text:
        text = ensure_list_import(text)
    if text == original:
        print(f"unchanged {path.relative_to(ROOT)}")
        return
    path.write_text(text)
    STRIP.process(path, True)
    print(f"converted {path.relative_to(ROOT)}")


def convert_erithizon():
    path = CARDS / "e" / PKG / "e" / "Erithizon.java"
    text = path.read_text()
    new = text.replace("setAttackTriggerTargetChosenByDefendingPlayer(", "setCombatTriggerTargetChosenByDefendingPlayer(")
    print(("converted " if new != text else "unchanged ") + str(path.relative_to(ROOT)))
    path.write_text(new)



def ensure_list_import(text):
    if "import java.util.List;" in text:
        return text
    return text.replace("\n@CardRegistration", "\nimport java.util.List;\n\n@CardRegistration", 1)


def convert(letter, name, edits):
    path = CARDS / letter / PKG / letter / f"{name}.java"
    text = path.read_text()
    if DONE[name] in text:
        print(f"unchanged {path.relative_to(ROOT)}")
        return
    for old, new in edits:
        if text.count(old) != 1:
            sys.exit(f"{path}: expected one occurrence of {old!r}")
        text = text.replace(old, new)
    if "List.of(" in text:
        text = ensure_list_import(text)
    path.write_text(text)
    print(f"converted {path.relative_to(ROOT)}")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--module", help="letter module to convert (default: all this run touches)")
    args = parser.parse_args()
    for (letter, name), edits in EDITS.items():
        if args.module is None or args.module == letter:
            convert(letter, name, edits)
    for letter, name in DAMAGED_PLAYER_CARDS:
        if args.module is None or args.module == letter:
            convert_damaged_player(letter, name)
    if args.module is None or args.module == "e":
        convert_erithizon()


if __name__ == "__main__":
    main()
