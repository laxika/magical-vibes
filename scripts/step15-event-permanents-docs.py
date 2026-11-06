#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 9: the agent-docs lines for "they", "them" and "each of them".

Idempotent; run once from the repository root:

    python3 scripts/step15-event-permanents-docs.py

Exact line edits. Each edit names the start of one line (unique in its file); the line, and for a multi-line entry the
continuation lines that follow it, become the new text, or go when the new text is None. A file whose old line is
gone and whose new text is present is left alone.
"""
import pathlib

DOCS = pathlib.Path("agent-docs")

YOU_ATTACK_FILTER = (
    "\"Whenever you attack with one or more [filter] creatures\" / \"whenever one or more [filter] creatures you "
    "control attack\" is `Triggers.youAttack().withFilter(filter)`: the collector judges the filter against the "
    "declared attackers, the ability doesn't trigger when none matches, and \"they\", \"those creatures\" and "
    "\"their total power\" are the matching attackers as they were declared (the entry's attacking snapshots, its "
    "declared `GrantScope.TARGETS` recipients and its triggering permanents). \"They gain [keyword]\" = "
    "`GrantKeywordEffect(keyword, GrantScope.TARGETS)` (Tyvar the Bellicose), \"those creatures get +2/+2\" = "
    "`BoostAllOwnCreaturesEffect(2, 2).forDeclaredAttackers()` (Lulu, Wild Hollyphant), \"X is their total power\" = "
    "`TotalPowerOfControlledCreatures.declaredAttackers(null)` (Witch-king, Sky Scourge), \"put a +1/+1 counter on "
    "each of them\" = `PutCounterOnEachMatchingPermanentEffect(PLUS_ONE_PLUS_ONE, 1, new PermanentTruePredicate(), "
    "EachPermanentScope.TRIGGERING_PERMANENTS)` (Vrestin, Menoptra Leader).")

DEALERS = ("the creatures of a `TriggerOptions.ONE_OR_MORE` \"one or more creatures you control deal combat damage\" "
           "batch that match the trigger's filter, which the combat collector sets as the stack entry's triggering "
           "permanents")

EDITS = {
    "EFFECTS_INDEX.md": [
        ("| `BoostTriggeringAttackersEffect` |", None),
        ("| `GrantKeywordToTriggeringAttackersEffect` |", None),
        ("| `PutCounterOnTriggeringAttackersEffect` |", None),
        ("| `SacrificeOneOfCombatDamageDealersThenRevealMatchingCreatureEffect` |", None),
        ("| `PutCountersOnCombatDamageDealersEffect` |", None),
        ("| `ExileTopCardsForAttackingCreaturesMayPlayThisTurnEffect` |", None),
        ("| `ConjureDuplicateOfChosenCombatDamageDealerIntoHandEffect` |",
         "| `ConjureDuplicateOfChosenCombatDamageDealerIntoHandEffect` | `()` | lets the controller choose one of "
         + DEALERS + ", that is still on the battlefield, and conjures its duplicate into hand | Vodalian Tide Mage "
         "(YDMU 29) |"),
        ("| `ReturnOneOfCombatDamageDealersToHandThenEffect` |",
         "| `ReturnOneOfCombatDamageDealersToHandThenEffect` | `(CardEffect thenEffect, String permanentDescription)` "
         "| non-targeting resolution-time choice among " + DEALERS + "; returns the chosen permanent to its owner's "
         "hand and resolves `thenEffect` only after a successful return. Used by Kaito, Dancing Shadow and Zurgo and "
         "Ojutai |"),
        ("| `SacrificeOneOfCombatDamageDealersThenRevealUntilSharedCreatureTypeEffect` |",
         "| `SacrificeOneOfCombatDamageDealersThenRevealUntilSharedCreatureTypeEffect` | `()` | choose one of "
         + DEALERS + ", sacrifice it, then reveal the controller's library until a creature card sharing a creature "
         "type with the sacrificed creature is found; put it onto the battlefield and put the rest on the bottom in a "
         "random order. Used by Descendants' Fury |"),
        ("| `PutCounterOnEachMatchingPermanentEffect` | `(CounterType, DynamicAmount amount, PermanentPredicate "
         "predicate, EachPermanentScope scope)` / int sugar `(CounterType, int count, PermanentPredicate, "
         "EachPermanentScope)` | put `amount` counters on each permanent matching the predicate across the "
         "battlefields chosen by `scope` (`ALL_PLAYERS` / `TARGET_PLAYER`).",
         "| `PutCounterOnEachMatchingPermanentEffect` | `(CounterType, DynamicAmount amount, PermanentPredicate "
         "predicate, EachPermanentScope scope)` / int sugar `(CounterType, int count, PermanentPredicate, "
         "EachPermanentScope)` | put `amount` counters on each permanent matching the predicate across the "
         "battlefields chosen by `scope` (`ALL_PLAYERS` / `TARGET_PLAYER`), or on the stack entry's triggering "
         "permanents (`TRIGGERING_PERMANENTS`: \"put a +1/+1 counter on each of them\", with `PermanentTruePredicate`, "
         "for a filtered `Triggers.youAttack()` (Vrestin, Menoptra Leader) or a `ONE_OR_MORE` combat damage batch "
         "(Heroes in a Half Shell, Vulture, Feathered Fiend)).", "prefix"),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `BoostTriggeringAttackersEffect(PermanentPredicate, int power, int toughness)`", "- " + YOU_ATTACK_FILTER),
        ("- `GrantKeywordToTriggeringAttackersEffect(PermanentPredicate, Keyword)`", None),
        ("- `PutCounterOnTriggeringAttackersEffect(CounterType, PermanentPredicate)`", None),
        ("`SacrificeOneOfCombatDamageDealersThenRevealMatchingCreatureEffect()` — optional batched combat", None, 4),
        ("- `PutCountersOnCombatDamageDealersEffect(CounterType, int)`",
         "- \"Put a +1/+1 counter on each of those creatures\" of a `ONE_OR_MORE` combat damage batch: "
         "`PutCounterOnEachMatchingPermanentEffect(PLUS_ONE_PLUS_ONE, 1, new PermanentTruePredicate(), "
         "EachPermanentScope.TRIGGERING_PERMANENTS)`; the combat collector sets the matching dealers as the stack "
         "entry's triggering permanents (Heroes in a Half Shell, Vulture, Feathered Fiend)."),
        ("- `ConjureDuplicateOfChosenCombatDamageDealerIntoHandEffect()`",
         "- `ConjureDuplicateOfChosenCombatDamageDealerIntoHandEffect()` - lets the controller choose one of "
         + DEALERS + ", that is still on the battlefield, and conjures its duplicate into hand (Vodalian Tide Mage, "
         "YDMU 29)"),
        ("- `SacrificeOneOfCombatDamageDealersThenRevealUntilSharedCreatureTypeEffect()` — on a batched",
         "- `SacrificeOneOfCombatDamageDealersThenRevealUntilSharedCreatureTypeEffect()` — choose one of " + DEALERS
         + ", sacrifice it, then\n  reveal until a creature card sharing a creature type with it is found; put that "
         "card onto the\n  battlefield and the rest on the bottom in a random order (Descendants' Fury)", 3),
        ("- `ReturnOneOfCombatDamageDealersToHandThenEffect(CardEffect, String[, List<UUID>])`",
         "- `ReturnOneOfCombatDamageDealersToHandThenEffect(CardEffect, String)` — non-targeting resolution-time "
         "choice among " + DEALERS + "; returns the chosen permanent, then resolves the follow-up only after a "
         "successful return (Kaito, Dancing Shadow)"),
        ("- `ExileTopCardsForAttackingCreaturesMayPlayThisTurnEffect(PermanentPredicate)`", None),
        ("- `PutCounterOnEachMatchingPermanentEffect(CounterType, int|DynamicAmount, PermanentPredicate, "
         "EachPermanentScope[, evaluateAmountPerPermanent])` — counters on each matching permanent across "
         "`ALL_PLAYERS`/`TARGET_PLAYER`;",
         "- `PutCounterOnEachMatchingPermanentEffect(CounterType, int|DynamicAmount, PermanentPredicate, "
         "EachPermanentScope[, evaluateAmountPerPermanent])` — counters on each matching permanent across "
         "`ALL_PLAYERS`/`TARGET_PLAYER`, or on the stack entry's triggering permanents (`TRIGGERING_PERMANENTS`: "
         "\"each of them\");", "prefix"),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| \"Specialize {2}\" plus \"Whenever you attack with one or more other creatures with flying,",
         "| \"Specialize {2}\" plus \"Whenever you attack with one or more other creatures with flying, [gain twice "
         "that much life / draw that many cards, then discard a card / each opponent loses that much life and you gain "
         "that much life / create that many 1/1 white Soldier tokens tapped and attacking / those creatures get +2/+2 "
         "until end of turn]\" (five specialized faces) | `Triggers.youAttack() ConditionalEffect(MinimumMatchingAttackers"
         "(1, PermanentAllOfPredicate(PermanentHasKeywordPredicate(FLYING), PermanentNotPredicate("
         "PermanentIsSourcePermanentPredicate))), effect using EventValue)`; the +2/+2 face is "
         "`Triggers.youAttack().withFilter(that predicate)` with `BoostAllOwnCreaturesEffect(2, 2).forDeclaredAttackers()` "
         "| Triggers.youAttack() | Lulu, Forgetful Hollyphant (HBG 3) and its five faces. |"),
        ("| \"Whenever you attack with one or more Wraiths, exile the top X cards of your library,",
         "| \"Whenever you attack with one or more Wraiths, exile the top X cards of your library, where X is their "
         "total power. You may play those cards this turn.\" | `Triggers.youAttack().withFilter(PermanentHasSubtypePredicate"
         "(WRAITH))`, `ExileTopCardsMayPlayThisTurnEffect(TotalPowerOfControlledCreatures.declaredAttackers(null))` | "
         "Triggers.youAttack() | Witch-king, Sky Scourge (LTC 511); the filter picks the attacking Wraiths as the "
         "ability triggers, their power is read as it resolves, and a departed Wraith counts with its last-known "
         "power. |"),
        ("| \"Whenever one or more creatures you control deal combat damage to a player, you may sacrifice one of them.",
         "| \"Whenever one or more creatures you control deal combat damage to a player, you may sacrifice one of them. "
         "If you do, reveal cards from the top of your library until you reveal a creature card that shares a creature "
         "type with the sacrificed creature. Put that card onto the battlefield and the rest on the bottom of your "
         "library in a random order.\" | `TriggerOptions.ONE_OR_MORE, MayEffect(new "
         "SacrificeOneOfCombatDamageDealersThenRevealUntilSharedCreatureTypeEffect(), prompt)` | "
         "Triggers.creatureYouControlDealsCombatDamageToPlayer() | Descendants' Fury (ECC 93); the stack entry's "
         "triggering permanents are the creatures of the damage event, and the sacrificed creature's types drive the "
         "reveal. |"),
        ("| \"Whenever one or more Mutants, Ninjas, and/or Turtles you control deal combat damage to a player,",
         "| \"Whenever one or more Mutants, Ninjas, and/or Turtles you control deal combat damage to a player, put a "
         "+1/+1 counter on each of those creatures and draw a card.\" | `trigger.withFilter(PermanentHasAnySubtypePredicate"
         "({MUTANT, NINJA, TURTLE})), TriggerOptions.ONE_OR_MORE, SequenceEffect.of(PutCounterOnEachMatchingPermanentEffect"
         "(PLUS_ONE_PLUS_ONE, 1, PermanentTruePredicate, EachPermanentScope.TRIGGERING_PERMANENTS), DrawCardEffect(1))` | "
         "`Triggers.creatureYouControlDealsCombatDamageToPlayer()` | Heroes in a Half Shell (TMC 6). `CombatDamageService` "
         "sets the matching dealers as the stack entry's triggering permanents, so each creature that dealt the damage "
         "gets a counter while the trigger draws only one card |"),
        ("| \"Whenever one or more other nontoken creatures you control deal combat damage to a player, choose one of "
         "those creatures.",
         "| \"Whenever one or more other nontoken creatures you control deal combat damage to a player, choose one of "
         "those creatures. Conjure a duplicate of the chosen creature into your hand.\" | "
         "`Triggers.creatureYouControlDealsCombatDamageToPlayer().withFilter(source-excluding nontoken predicate), "
         "TriggerOptions.ONE_OR_MORE, ConjureDuplicateOfChosenCombatDamageDealerIntoHandEffect()` | combat-damage trigger "
         "| Vodalian Tide Mage (YDMU 29); the stack entry's triggering permanents are the matching dealers of the damage "
         "step, and resolution chooses one still on the battlefield before conjuring its duplicate. |"),
        ("| \"Whenever one or more creatures you control deal combat damage to a player, you may return one of them",
         "| \"Whenever one or more creatures you control deal combat damage to a player, you may return one of them to "
         "its owner's hand. If you do, you may activate loyalty abilities of Kaito twice this turn rather than only "
         "once.\" | `trigger.withFilter(PermanentIsCreaturePredicate), TriggerOptions.ONE_OR_MORE, "
         "MayEffect(ReturnOneOfCombatDamageDealersToHandThenEffect(MayEffect(GrantExtraLoyaltyActivationToSourceEffect()), "
         "\"a creature that dealt combat damage\"), ...)` | triggered ability | The stack entry's triggering permanents "
         "are the matching damage dealers, so the non-targeting choice is restricted to the creatures from that damage "
         "event. The follow-up extra activation is source-specific to Kaito |"),
    ],
    "CARD_PATTERN_INDEX.md": [
        ("| one-time spell-cast boon plus five specialize faces",
         "| one-time spell-cast boon plus five specialize faces with \"attack with one or more other creatures with "
         "flying\" triggers (`EventValue` count; the +2/+2 face a `Triggers.youAttack().withFilter(...)`) | "
         "`l/LuluForgetfulHollyphant.java` + `SpecializeLuluEffect` + `BoostAllOwnCreaturesEffect.forDeclaredAttackers()` |"),
        ("| one or more other nontoken creatures deal combat damage, then choose one and conjure its duplicate into hand |",
         "| one or more other nontoken creatures deal combat damage, then choose one and conjure its duplicate into hand "
         "| `Triggers.creatureYouControlDealsCombatDamageToPlayer().withFilter(...)` + `TriggerOptions.ONE_OR_MORE` + "
         "`ConjureDuplicateOfChosenCombatDamageDealerIntoHandEffect` |"),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("| One or more creatures deal combat damage → may sacrifice one dealer and reveal a shared-type creature |",
         "| One or more creatures deal combat damage → may sacrifice one dealer and reveal a shared-type creature | "
         "`d/DescendantsFury.java` | `Triggers.creatureYouControlDealsCombatDamageToPlayer().withFilter("
         "PermanentIsCreaturePredicate), TriggerOptions.ONE_OR_MORE, MayEffect("
         "SacrificeOneOfCombatDamageDealersThenRevealUntilSharedCreatureTypeEffect)`; the stack entry's triggering "
         "permanents are the dealers, and the effect chooses among those the controller still controls |"),
        ("| One or more other nontoken creatures deal combat damage to a player → choose one and conjure a duplicate",
         "| One or more other nontoken creatures deal combat damage to a player → choose one and conjure a duplicate "
         "into hand | `v/VodalianTideMage.java` | `Triggers.creatureYouControlDealsCombatDamageToPlayer().withFilter("
         "source-excluding nontoken predicate)` and `TriggerOptions.ONE_OR_MORE`; "
         "`ConjureDuplicateOfChosenCombatDamageDealerIntoHandEffect` reads the dealers from the stack entry's "
         "triggering permanents and prompts at resolution |"),
    ],
    "ACTIVATED_ABILITY_GUIDE.md": [
        ("| `Triggers.creatureYouControlDealsCombatDamageToPlayerOrBattle()` |",
         "| `Triggers.creatureYouControlDealsCombatDamageToPlayerOrBattle()` | One or more matching creatures you "
         "control deal combat damage to a player or battle; with `TriggerOptions.ONE_OR_MORE` it batches separately "
         "for each damaged player or battle and gives the stack entry the matching dealers as its triggering "
         "permanents (\"one of them\", \"each of those creatures\") |"),
        ("| `Triggers.youAttack()` | One or more creatures the controller controls attack",
         "| `Triggers.youAttack()` | One or more creatures the controller controls attack (fires once per combat, not "
         "per creature). Scans all controller's permanents after attackers are declared. Targeted effects use the "
         "attack-trigger target-selection pipeline; non-targeting effects are pushed directly to the stack. "
         "`.withFilter(predicate)` is \"whenever you attack with one or more [predicate] creatures\": the ability "
         "triggers only if a declared attacker matches, is its own stack object, and its entry's attacking snapshots, "
         "declared `GrantScope.TARGETS` recipients and triggering permanents are the matching attackers |"),
    ],
}


def apply(path, edits):
    lines = path.read_text(errors="surrogateescape").split("\n")
    changed = False
    for edit in edits:
        prefix, new = edit[0], edit[1]
        mode = edit[2] if len(edit) > 2 else 1
        if mode == "prefix":
            # Only the start of the line changes; the rest of the row stays.
            if any(line.startswith(new) for line in lines):
                continue
            hits = [i for i, line in enumerate(lines) if line.startswith(prefix)]
            if len(hits) != 1:
                raise SystemExit(f"{path}: {len(hits)} lines start with {prefix!r}")
            lines[hits[0]] = new + lines[hits[0]][len(prefix):]
            changed = True
            continue
        span = mode
        hits = [i for i, line in enumerate(lines) if line.startswith(prefix)]
        if new is not None and new.split("\n")[0] in lines and (not hits or lines[hits[0]] == new.split("\n")[0]):
            continue
        if not hits:
            if new is None:
                continue
            raise SystemExit(f"{path}: no line starts with {prefix!r}")
        if len(hits) > 1:
            raise SystemExit(f"{path}: {len(hits)} lines start with {prefix!r}")
        at = hits[0]
        lines[at:at + span] = [] if new is None else new.split("\n")
        changed = True
    if changed:
        path.write_text("\n".join(lines), errors="surrogateescape")
        print(f"edited {path}")


def main():
    for name, edits in EDITS.items():
        apply(DOCS / name, edits)


if __name__ == "__main__":
    main()
