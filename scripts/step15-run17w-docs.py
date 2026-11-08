#!/usr/bin/env python3
"""Step 15, run 17w: agent-docs for the spell-cast mana-value family that reads the triggering spell.

Line rewrites (each line found by a key it contains; a key already rewritten is skipped) and line deletions in
EFFECTS_QUICK_REFERENCE.md, EFFECTS_INDEX.md, ORACLE_TEXT_EFFECT_MAP.md, CARD_PATTERNS_CREATURES_TRIGGERED.md and
CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md. Idempotent. Fails if a doc or skill still names a deleted class.

Usage: step15-run17w-docs.py
"""
import pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"
DELETED = ["RashmiTriggerEffect", "DealDamageEqualToSpellManaValueToAnyTargetEffect",
           "DealDamageEqualToManaSpentToCastToAnyTargetEffect", "BoostSelfByCastSpellManaValueEffect",
           "ReturnTargetPermanentByCastSpellManaValueEffect"]

DAMAGE = ("\"Whenever you cast a [quality] spell, this deals damage equal to that spell's mana value to any target\" is "
          "`addTrigger(Triggers.youCastSpell().withFilter(spellPredicate), new DealDamageToAnyTargetEffect(new "
          "TargetSpellManaValue()))` (Rage Extractor, Thor, God of Thunder; `Triggers.opponentCastsSpell().anySpell()` "
          "Kaervek the Merciless): the generic cast collector gives the target choice the spell and its mana value, and "
          "the amount reads the stack entry's triggering spell as the ability resolves, as it last existed on the stack "
          "once it has left (effect-slot-refactor.md step 15, run 17w)")
BOOST = ("\"Whenever you cast a [quality] spell, this gets +X/+X (or +X/+0) until end of turn, where X is that spell's "
         "mana value\" is `addTrigger(Triggers.youCastSpell().withFilter(spellPredicate) / .anySpell(), new "
         "BoostSelfEffect(new TargetSpellManaValue(), new TargetSpellManaValue() / new Fixed(0))[, new "
         "GrantKeywordEffect(keyword, GrantScope.SELF)])` (Manaplasm, Jamie McCrimmon, Skittering Cicada with trample, "
         "Erratic Cyclops, Renegade Bull; effect-slot-refactor.md step 15, run 17w)")
RETURN = ("\"Whenever you cast a spell, return up to one target nonland permanent an opponent controls with mana value X "
          "or less, where X is that spell's mana value\" is `target(new PermanentPredicateTargetFilter(new "
          "PermanentAllOfPredicate(List.of(nonland, not controlled by you, new PermanentMaxManaValueXPredicate())), "
          "\"…\"), 0, 1).addTrigger(Triggers.youCastSpell().anySpell(), ReturnToHandEffect.target())` (Hammerhead "
          "Tyrant): a cast trigger whose target filter reads X (`PermanentManaValueEqualsXPredicate`, "
          "`PermanentMaxManaValueXPredicate`) is chosen against the spell's mana value as the ability's X, which the "
          "resolution check reads from the entry (effect-slot-refactor.md step 15, run 17w)")
MANA_SPENT = ("\"You get an emblem with 'Whenever you cast a red spell, this emblem deals X damage to any target, where X "
              "is the amount of mana spent to cast that spell'\" is `new CreateEmblemEffect(new TriggeredAbility("
              "Triggers.youCastSpell().withFilter(new CardColorPredicate(CardColor.RED)), new "
              "DealDamageToAnyTargetEffect(new ManaSpentToCast())), reminderText)` (Chandra, Dressed to Kill): the cast "
              "collector gives the ability the mana spent on the triggering spell as its X, which `ManaSpentToCast` reads "
              "on a cast trigger (effect-slot-refactor.md step 15, run 17w)")
RASHMI = ("`RashmiRevealTopCardEffect()` on `Triggers.youCastSpell().nthThisTurn(1)` — reveal the top card; a nonland "
          "card with mana value less than that spell's (the stack entry's triggering spell, as it last existed on the "
          "stack once it has left) may be cast without paying its mana cost, otherwise or on decline it goes to hand "
          "(Rashmi, Eternities Crafter)")
DOOM_OLD = "collector snapshots `CountersOnSource` damage amounts to Fixed at trigger time"
DOOM_NEW = ("\"that much damage\" is `DealDamageToAnyTargetEffect(new TargetSpellManaValue())`, the spell's mana value, "
            "which is the number of counters as the ability triggered (its ruling)")

# file -> list of (key, new line or None to delete, fragment replacements in place of a whole line)
REWRITES = {
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `DealDamageEqualToSpellManaValueToAnyTargetEffect(CardPredicate)`", "- " + DAMAGE),
        ("- `BoostSelfByCastSpellManaValueEffect(", "- " + BOOST),
        ("- `ReturnTargetPermanentByCastSpellManaValueEffect(", "- " + RETURN),
        ("- `DealDamageEqualToManaSpentToCastToAnyTargetEffect(CardPredicate)`", "- " + MANA_SPENT),
        ("- `RashmiTriggerEffect()` / `RashmiRevealTopCardEffect(int spellManaValue)`", "- " + RASHMI),
    ],
    "EFFECTS_INDEX.md": [
        ("| `DealDamageEqualToSpellManaValueToAnyTargetEffect` |", None),
        ("| `ReturnTargetPermanentByCastSpellManaValueEffect` |", None),
        ("| `BoostSelfByCastSpellManaValueEffect` |", None),
        ("| `DealDamageEqualToManaSpentToCastToAnyTargetEffect` |", None),
        ("| `RashmiTriggerEffect` / `RashmiRevealTopCardEffect` |",
         "| `RashmiRevealTopCardEffect` | `()` | Rashmi, Eternities Crafter's resolution on `Triggers.youCastSpell()"
         ".nthThisTurn(1)`: reveal the top card; a nonland card with mana value less than the triggering spell's (read "
         "from the stack entry as it resolves) may be cast for free, otherwise or on decline it goes to hand |"),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| `RashmiTriggerEffect()` in `Triggers.youCastSpell()`",
         "| \"Whenever you cast your first spell each turn, reveal the top card of your library. You may cast it without "
         "paying its mana cost if it's a spell with lesser mana value. If you don't cast it, put it into your hand.\" | "
         "`RashmiRevealTopCardEffect()` | `Triggers.youCastSpell().nthThisTurn(1)` | Rashmi, Eternities Crafter. The "
         "triggering spell's mana value is read from the stack entry as the ability resolves (its triggering spell, as "
         "it last existed on the stack if it was countered, per the ruling); lands and cards that are not strictly lower "
         "go directly to hand, while an eligible card reuses the shared top-card free-cast choice with "
         "`LookDestination.HAND` |"),
        ("`CreateEmblemEffect(List.of(new DealDamageEqualToManaSpentToCastToAnyTargetEffect(CardColorPredicate(RED))), \"…\")` | loyalty ability",
         "| \"You get an emblem with \\\"Whenever you cast a red spell, this emblem deals X damage to any target, where X "
         "is the amount of mana spent to cast that spell.\\\"\" | `new CreateEmblemEffect(new TriggeredAbility("
         "Triggers.youCastSpell().withFilter(new CardColorPredicate(CardColor.RED)), new DealDamageToAnyTargetEffect("
         "new ManaSpentToCast())), \"…\")` | loyalty ability | Chandra, Dressed to Kill. The emblem's cast trigger goes "
         "through the generic cast collector, which gives it the mana spent on the triggering spell as its X |"),
        ("Collector snapshots `CountersOnSource` damage to Fixed at trigger time",
         "| \"Whenever you cast a spell with mana value equal to the number of [type] counters on this, this deals that "
         "much damage to any target. Then put a [type] counter on this\" | `.withStackEntryFilter(new "
         "StackEntryManaValueEqualsSourceCountersPredicate(TYPE))` on the cast trigger with "
         "`DealDamageToAnyTargetEffect(new TargetSpellManaValue())` + `PutCountersOnSelfEffect(TYPE)` | "
         "`Triggers.youCastSpell()` | Imminent Doom (DOOM). Pair with `EnterWithCountersEffect(TYPE, Fixed(1))`. "
         "\"That much\" is the spell's mana value, the number of counters as the ability triggered (its ruling), read "
         "from the stack entry's triggering spell |"),
        ("+ `ReturnTargetPermanentByCastSpellManaValueEffect()`",
         "| \"Whenever you cast a spell, return up to one target nonland permanent an opponent controls with mana value "
         "less than or equal to that spell's mana value to its owner's hand\" | `target(new PermanentPredicateTargetFilter("
         "new PermanentAllOfPredicate(List.of(nonland, not controlled by you, new PermanentMaxManaValueXPredicate())), "
         "\"…\"), 0, 1)` + `ReturnToHandEffect.target()` | `Triggers.youCastSpell().anySpell()` | Hammerhead Tyrant. "
         "The target is chosen against the triggering spell's mana value as the ability's X, and the resolution check "
         "reads that X from the entry, so the target stays legal only while it still has mana value X or less |"),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("| Spell cast trigger — self-boost by mana value | `m/Manaplasm.java` |",
         "| Spell cast trigger — self-boost by mana value | `m/Manaplasm.java` | Triggers.youCastSpell().anySpell() "
         "`BoostSelfEffect(new TargetSpellManaValue(), new TargetSpellManaValue())` — \"whenever you cast a spell, this "
         "gets +X/+X until end of turn where X is that spell's mana value\" (`.withFilter(p)` for a quality; `new "
         "Fixed(0)` toughness for +X/+0, Erratic Cyclops) |"),
    ],
    "CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md": [
        ("`CreateEmblemEffect(List.of(new DealDamageEqualToManaSpentToCastToAnyTargetEffect(CardColorPredicate(RED))), \"…\")` — the emblem marker deals any-target damage equal to the mana spent",
         None, ("`CreateEmblemEffect(List.of(new DealDamageEqualToManaSpentToCastToAnyTargetEffect(CardColorPredicate(RED))), \"…\")` — the emblem marker deals any-target damage equal to the mana spent",
                "`CreateEmblemEffect(new TriggeredAbility(Triggers.youCastSpell().withFilter(CardColorPredicate(RED)), new DealDamageToAnyTargetEffect(new ManaSpentToCast())), \"…\")` — the emblem's cast trigger deals any-target damage equal to the mana spent on the triggering spell")),
    ],
}

FRAGMENTS = {
    "EFFECTS_QUICK_REFERENCE.md": [(DOOM_OLD, DOOM_NEW)],
    "ARCHITECTURE.md": [(
        "A permanent target restricted by \"that spell's mana value\" (`PermanentManaValueEqualsXPredicate` in the ability's "
        "target filter, Skyfire Kirin) is chosen against the spell's mana value as the ability's X, which "
        "`TriggeredAbilityQueueService` puts in the target choice's filter context and the resolution check reads from the "
        "entry.",
        "A permanent target restricted by \"that spell's mana value\" (`PermanentManaValueEqualsXPredicate` or "
        "`PermanentMaxManaValueXPredicate` in the ability's target filter: Skyfire Kirin, Hammerhead Tyrant's \"up to one\" "
        "target) is chosen against the spell's mana value as the ability's X, which `TriggeredAbilityQueueService` (or the "
        "multi-target choice) puts in the target choice's filter context and the resolution check reads from the entry. "
        "\"Damage equal to the amount of mana spent to cast that spell\" (`ManaSpentToCast`) reads the X the cast "
        "collector gives the ability, the mana spent on the triggering spell."),
    ],
}


def read(path):
    return path.read_text(encoding="utf-8", errors="surrogateescape")


def write(path, text):
    path.write_text(text, encoding="utf-8", errors="surrogateescape")


def main():
    for name, rewrites in REWRITES.items():
        path = DOCS / name
        lines = read(path).split("\n")
        for rewrite in rewrites:
            key, new = rewrite[0], rewrite[1]
            fragment = rewrite[2] if len(rewrite) > 2 else None
            hits = [i for i, line in enumerate(lines) if key in line]
            if fragment is not None:
                for i in hits:
                    lines[i] = lines[i].replace(fragment[0], fragment[1])
            elif new is None:
                lines = [line for line in lines if key not in line]
            else:
                for i in hits:
                    lines[i] = new
        text = "\n".join(lines)
        for old, new in FRAGMENTS.get(name, []):
            text = text.replace(old, new)
        write(path, text)
    for name, fragments in FRAGMENTS.items():
        if name not in REWRITES:
            path = DOCS / name
            text = read(path)
            for old, new in fragments:
                text = text.replace(old, new)
            write(path, text)
    leftovers = []
    for path in list(DOCS.glob("*.md")) + list((ROOT / ".claude/skills").rglob("*.md")):
        text = read(path)
        leftovers += [f"{path.relative_to(ROOT)}: {cls}" for cls in DELETED if cls in text]
    if leftovers:
        raise SystemExit("still named:\n" + "\n".join(leftovers))


if __name__ == "__main__":
    main()
