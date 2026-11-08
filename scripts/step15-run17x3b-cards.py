#!/usr/bin/env python3
"""Step 15, run 17x3b: the enter family's "it / that creature" reads the stack entry's triggering permanent.

Rewrites the card classes whose enter trigger held a marker the enter collector rewrote into an effect aimed at the
entering permanent as the entry's target. Each card now writes the effect that goes on the stack, reading the entry's
triggering permanent (the creature that entered) as it resolves:

- "put N counters on it / that creature": `PutCounterOnTargetPermanentEffect.onTriggeringPermanent(type, amount)`
  (Securitron Squadron, Serra Redeemer, Good Fortune Unicorn, Case of the Pilfered Proof, Geralf, Railway Brawler's
  "where X is its power" `TriggeringPermanentPower`), with "it gains haste" `GrantKeywordEffect(HASTE,
  TRIGGERING_PERMANENT)` in one `SequenceEffect` (Wartime Protestors, Surrak and Goreclaw), Mighty Emergence's "you
  may" a `MayEffect`, Ichorplate Golem's "if it has one or more oil counters on it" the intervening "if"
  `TriggeringPermanentMatches(PermanentHasCountersPredicate(OIL))` (replacing `PutCountersOnEnteringCreatureEffect`).
- "that creature gets +X/+Y [and gains haste] until end of turn": `BoostReferencedPermanentEffect(TRIGGERING, X, Y)`,
  with `GrantKeywordEffect(HASTE, TRIGGERING_PERMANENT[, duration])` in one `SequenceEffect`, or the grant alone
  (replacing `BoostEnteringCreatureEffect`).
- Amulet of Vigor: "a permanent you control enters tapped" the trigger's filter `PermanentIsTappedPredicate` with
  `YOU_CONTROL`, "untap it" `UntapPermanentsEffect(TapUntapScope.TRIGGERING)` (replacing
  `UntapEnteringPermanentEffect`).
- Vildin-Pack Alpha: `MayEffect(TransformTargetPermanentEffect.ofTriggeringPermanent())` (replacing
  `TransformEnteringCreatureEffect`).
- Renegade Doppelganger, Sarkhan, Soul Aflame: `MayEffect(BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(…)
  .ofTriggeringPermanent())` (replacing `BecomeCopyOfEnteringCreatureUntilEndOfTurnEffect`).
- The Equipment's "[you may] attach this Equipment to it": `[MayEffect(]AttachSourceEquipmentToTargetCreatureEffect
  .forTriggeringCreature()[)]`; the Auras' "[you may] attach this Aura to that creature": `[MayEffect(]
  AttachSourceAuraToTargetCreatureEffect.ofTriggeringPermanent()[)]` (replacing the two
  `AttachSource…ToEnteringCreatureEffect`s).

An import the rewritten class no longer uses is removed. Idempotent: a file already converted is left alone. Per
letter module ("shared" for the shared module).

Usage: step15-run17x3b-cards.py <letter> [<letter> ...]   (or "all")
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
PKG = "com.github.laxika.magicalvibes"

P1 = "CounterType.PLUS_ONE_PLUS_ONE"
ON_IT = "PutCounterOnTargetPermanentEffect.onTriggeringPermanent"
COUNTER_IMPORTS = ["model.CounterType", "model.effect.PutCounterOnTargetPermanentEffect"]
HASTE_IT = "new GrantKeywordEffect(Keyword.HASTE, GrantScope.TRIGGERING_PERMANENT)"
HASTE_IMPORTS = ["model.Keyword", "model.effect.GrantKeywordEffect", "model.effect.GrantScope"]
BOOST_IMPORTS = ["model.effect.BoostReferencedPermanentEffect", "model.effect.PermanentReference"]


def counters(count):
    return [(f"new PutCountersOnEnteringCreatureEffect({count}, false)", f"{ON_IT}({P1}, {count})")], COUNTER_IMPORTS


def boost_and_haste():
    return [("new BoostEnteringCreatureEffect(2, 0, Set.of(Keyword.HASTE))",
             "SequenceEffect.of(\n"
             "                        new BoostReferencedPermanentEffect(PermanentReference.TRIGGERING, 2, 0),\n"
             f"                        {HASTE_IT})")], \
        BOOST_IMPORTS + HASTE_IMPORTS + ["model.effect.SequenceEffect"]


def haste_only():
    return [("new BoostEnteringCreatureEffect(0, 0, Set.of(Keyword.HASTE))", HASTE_IT)], HASTE_IMPORTS


def equipment_may():
    return [("new AttachSourceEquipmentToEnteringCreatureEffect()",
             "new MayEffect(AttachSourceEquipmentToTargetCreatureEffect.forTriggeringCreature(),\n"
             "                        \"Attach this Equipment to it?\")")], \
        ["model.effect.AttachSourceEquipmentToTargetCreatureEffect", "model.effect.MayEffect"]


def aura_may():
    return [("new AttachSourceAuraToEnteringCreatureEffect()",
             "new MayEffect(AttachSourceAuraToTargetCreatureEffect.ofTriggeringPermanent(),\n"
             "                        \"Attach this Aura to that creature?\")")], \
        ["model.effect.AttachSourceAuraToTargetCreatureEffect", "model.effect.MayEffect"]


# card -> (module, package letter, edits, imports to add)
CARDS = {
    "SecuritronSquadron": ("s", "s", [
        ("new PutCountersOnEnteringCreatureEffect(CounterType.PLUS_ONE_PLUS_ONE, 1, false)", f"{ON_IT}({P1}, 1)"),
    ], COUNTER_IMPORTS),
    "SerraRedeemer": ("s", "s") + counters(2),
    "GoodFortuneUnicorn": ("g", "g") + counters(1),
    "CaseOfThePilferedProof": ("c", "c") + counters(1),
    "GeralfTheFleshwright": ("g", "g", [
        ("new PutCountersOnEnteringCreatureEffect(otherZombiesEnteredThisTurn, false)",
         f"{ON_IT}({P1}, otherZombiesEnteredThisTurn)"),
    ], COUNTER_IMPORTS),
    "RailwayBrawler": ("r", "r", [
        ("new PutCountersOnEnteringCreatureEffect(new TargetPower(), false)",
         f"{ON_IT}({P1}, new TriggeringPermanentPower())"),
    ], COUNTER_IMPORTS + ["model.amount.TriggeringPermanentPower"]),
    "WartimeProtestors": ("w", "w", [
        ("""new PutCountersOnEnteringCreatureEffect(1, false,
                        List.of(new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET,
                                GrantDuration.END_OF_TURN))))""",
         f"""SequenceEffect.of(
                        {ON_IT}({P1}, 1),
                        {HASTE_IT}))"""),
    ], COUNTER_IMPORTS + HASTE_IMPORTS + ["model.effect.SequenceEffect"]),
    "SurrakAndGoreclaw": ("s", "s", [
        ("""new PutCountersOnEnteringCreatureEffect(1, false,
                        List.of(new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET))))""",
         f"""SequenceEffect.of(
                        {ON_IT}({P1}, 1),
                        {HASTE_IT}))"""),
    ], COUNTER_IMPORTS + HASTE_IMPORTS + ["model.effect.SequenceEffect"]),
    "MightyEmergence": ("m", "m", [
        ("new PutCountersOnEnteringCreatureEffect(2)",
         f"""new MayEffect({ON_IT}({P1}, 2),
                        "Put two +1/+1 counters on it?")"""),
    ], COUNTER_IMPORTS + ["model.effect.MayEffect"]),
    "IchorplateGolem": ("i", "i", [
        ("""        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasCountersPredicate(CounterType.OIL))),
                        ObjectRelation.YOU_CONTROL),
                new PutCountersOnEnteringCreatureEffect(CounterType.OIL, 1, false, CounterType.OIL));""",
         f"""        // Whenever a creature you control enters, if it has one or more oil counters on it, put an oil counter on
        // it. The intervening "if" is judged as the creature enters and again as the ability resolves (CR 603.4).
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                TriggerOptions.DEFAULT.withInterveningIf(new TriggeringPermanentMatches(
                        new PermanentHasCountersPredicate(CounterType.OIL))),
                {ON_IT}(CounterType.OIL, 1));"""),
    ], ["model.condition.TriggeringPermanentMatches", "model.effect.PutCounterOnTargetPermanentEffect",
        "model.trigger.TriggerOptions"]),
    "PrimalForcemage": ("p", "p", [
        ("new BoostEnteringCreatureEffect(3, 3)",
         "new BoostReferencedPermanentEffect(PermanentReference.TRIGGERING, 3, 3)"),
    ], BOOST_IMPORTS),
    "OgreBattledriver": ("o", "o") + boost_and_haste(),
    "InTheWebOfWar": ("i", "i") + boost_and_haste(),
    "EnduringCourage": ("e", "e") + boost_and_haste(),
    "AlexWilderRunaway": ("a", "a") + boost_and_haste(),
    "RapidAugmenter": ("r", "r") + haste_only(),
    "DragonTempest": ("d", "d") + haste_only(),
    "ChainerNightmareAdept": ("c", "c", [
        ("new BoostEnteringCreatureEffect(0, 0, Set.of(Keyword.HASTE), GrantDuration.UNTIL_YOUR_NEXT_TURN)",
         "new GrantKeywordEffect(Keyword.HASTE, GrantScope.TRIGGERING_PERMANENT, GrantDuration.UNTIL_YOUR_NEXT_TURN)"),
    ], HASTE_IMPORTS),
    "AmuletOfVigor": ("a", "a", [
        ("        addTrigger(Triggers.enters(null, ObjectRelation.ANY), new UntapEnteringPermanentEffect());",
         """        // Whenever a permanent you control enters tapped, untap it: "enters tapped" is the permanent as it entered,
        // the trigger's filter; "it" is the stack entry's triggering permanent.
        addTrigger(Triggers.enters(new PermanentIsTappedPredicate(), ObjectRelation.YOU_CONTROL),
                new UntapPermanentsEffect(TapUntapScope.TRIGGERING));"""),
    ], ["model.effect.TapUntapScope", "model.effect.UntapPermanentsEffect", "model.filter.PermanentIsTappedPredicate"]),
    "VildinPackAlpha": ("shared", "v", [
        ("new TransformEnteringCreatureEffect()",
         """new MayEffect(TransformTargetPermanentEffect.ofTriggeringPermanent(), "Transform it?")"""),
    ], ["model.effect.MayEffect", "model.effect.TransformTargetPermanentEffect"]),
    "RenegadeDoppelganger": ("r", "r", [
        ("new BecomeCopyOfEnteringCreatureUntilEndOfTurnEffect()",
         """new MayEffect(new BecomeCopyOfTargetCreatureUntilEndOfTurnEffect().ofTriggeringPermanent(),
                        "Have this creature become a copy of that creature until end of turn?")"""),
    ], ["model.effect.BecomeCopyOfTargetCreatureUntilEndOfTurnEffect", "model.effect.MayEffect"]),
    "SarkhanSoulAflame": ("s", "s", [
        ("""new BecomeCopyOfEnteringCreatureUntilEndOfTurnEffect(
                        "Sarkhan, Soul Aflame", Set.of(CardSupertype.LEGENDARY)))""",
         """new MayEffect(new BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(
                        "Sarkhan, Soul Aflame", Set.of(CardSupertype.LEGENDARY)).ofTriggeringPermanent(),
                        "Have Sarkhan become a copy of it until end of turn?"))"""),
    ], ["model.effect.BecomeCopyOfTargetCreatureUntilEndOfTurnEffect", "model.effect.MayEffect"]),
    "ShieldedByFaith": ("s", "s") + aura_may(),
    "PrisonTerm": ("p", "p") + aura_may(),
    "IllusoryGains": ("i", "i", [
        ("new AttachSourceAuraToEnteringCreatureEffect(false)",
         "AttachSourceAuraToTargetCreatureEffect.ofTriggeringPermanent()"),
    ], ["model.effect.AttachSourceAuraToTargetCreatureEffect"]),
    "RoninWarclub": ("r", "r", [
        ("new AttachSourceEquipmentToEnteringCreatureEffect(false)",
         "AttachSourceEquipmentToTargetCreatureEffect.forTriggeringCreature()"),
    ], ["model.effect.AttachSourceEquipmentToTargetCreatureEffect"]),
}
for _name, _letter in [("VeteransArmaments", "v"), ("ThornbiteStaff", "t"), ("SwordOfTheSqueak", "s"),
                       ("StormriderRig", "s"), ("SaiOfTheShinobi", "s"), ("QuintessentialKatana", "q"),
                       ("ObsidianBattleAxe", "o"), ("InventorsGoggles", "i"), ("HerosBlade", "h"),
                       ("DredgingClaw", "d"), ("DocOcksTentacles", "d"), ("DivinersWand", "d"),
                       ("CloakAndDagger", "c")]:
    CARDS[_name] = (_letter, _letter) + equipment_may()


def imp(cls):
    return f"import {PKG}.{cls};"


def remove_unused_imports(text):
    lines = text.split("\n")
    body = "\n".join(l for l in lines if not l.startswith("import "))
    kept = []
    for line in lines:
        match = re.match(r"import ([\w.]+)\.(\w+);$", line)
        if match and not re.search(r"\b" + match.group(2) + r"\b", body):
            continue
        kept.append(line)
    text = "\n".join(kept)
    # A java.util import removed last leaves the blank line that separated it.
    return re.sub(r"\n\n\n+", "\n\n", text)


def add_imports(text, add):
    lines = text.split("\n")
    for cls in add:
        line = imp(cls)
        if line in lines:
            continue
        imports = [i for i, l in enumerate(lines) if l.startswith("import ") and not l.startswith("import java.")]
        position = next((i for i in imports if lines[i] > line), imports[-1] + 1)
        lines.insert(position, line)
    return "\n".join(lines)


def convert(name, module, letter, edits, add):
    path = ROOT / f"magical-vibes-card/{module}/src/main/java/com/github/laxika/magicalvibes/cards/{letter}/{name}.java"
    text = path.read_text()
    original = text
    for old, new in edits:
        if new in text:
            continue
        if old not in text:
            raise SystemExit(f"{name}: expected text not found:\n{old}")
        text = text.replace(old, new)
    if text == original:
        return
    text = add_imports(text, add)
    text = remove_unused_imports(text)
    path.write_text(text)
    print(f"converted {name}")


def main():
    modules = set("abcdefghijklmnopqrstuvwxyz") | {"shared"} if sys.argv[1:] == ["all"] else set(sys.argv[1:])
    if not modules:
        raise SystemExit(__doc__)
    for name, (module, letter, edits, add) in CARDS.items():
        if module in modules:
            convert(name, module, letter, edits, add)


if __name__ == "__main__":
    main()
