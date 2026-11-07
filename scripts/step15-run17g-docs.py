#!/usr/bin/env python3
"""Step 15 run 17g of effect-slot-refactor.md: agent-docs and skills stop naming AllyCombatDamageTriggerEffect.

Idempotent: each edit is an exact replacement that is skipped once applied. Fails if a removed form is still named
in agent-docs or the skills afterwards.
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"

IT_FORMS = ("`PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING, counterType[, amount])`, "
            "`DoublePlusOneCountersOnSourceEffect.ofTriggeringPermanent()`, "
            "`TurnSourceFaceUpEffect.ofTriggeringPermanent()`, `FlickerEffect.flickerTriggeringPermanent()`, "
            "`CreateTokenCopyOfEnteringPermanentEffect[.nonlegendary()]`, "
            "`GainLifeEffect(new TriggeringPermanentToughness())`, "
            "`ConditionalEffect(new TriggeringPermanentMatches(p), effect)`")

EDITS = {
    "EFFECTS_QUICK_REFERENCE.md": [
        ("; use `AllyCombatDamageTriggerEffect(..., true)` to keep the creature that dealt the combat damage as the "
         "damage source (Saskia the Unyielding).",
         "; Saskia the Unyielding's \"it deals that much damage to the chosen player\" is "
         "`DealDamageToChosenPlayerEffect(new EventValue())`, whose damage source is the creature that dealt the "
         "combat damage."),
        (re.compile(r"^- `AllyCombatDamageTriggerEffect` with `opponentDealtDamage = true` — .*$", re.M),
         "- \"Whenever a creature you control deals combat damage to a player, put a counter on it / that "
         "creature\" (`Triggers.creatureYouControlDealsCombatDamageToPlayer()`, the creature's filter "
         "`.withFilter(p)`): the collector records the creature as the stack entry's triggering permanent, and the "
         "effect reads it as the ability resolves: " + IT_FORMS + ". \"One or more\" (`TriggerOptions.ONE_OR_MORE`) "
         "has no single triggering permanent: its creatures are the entry's triggering permanents. \"Whenever your "
         "opponents are dealt combat damage, if any of that damage was dealt by a [filter]\" is "
         "`Triggers.creatureDealsCombatDamageToOpponent().withFilter(p)` with `TriggerOptions.ONE_OR_MORE`: any "
         "player's matching creature, once for the combat damage step (Mindblade Render).",
         "has no single triggering permanent: its creatures are the entry's triggering permanents"),
    ],
    "EFFECTS_INDEX.md": [
        ("Contrast `AllyCombatDamageTriggerEffect` (players only, wraps a generic effect on the stack)",
         "Contrast a `Triggers.creatureYouControlDealsCombatDamageToPlayer()` trigger (players only, an ability on "
         "the stack)"),
        (re.compile(r"^\| `AllyCombatDamageTriggerEffect` \|.*\n", re.M), ""),
        ("| `CreateTokenCopyOfEnteringPermanentEffect` | `()` | non-targeting enter-trigger effect that creates a token "
         "copy of the permanent that caused the trigger; the enter-trigger collector binds that permanent's "
         "identity. Used by Ondu Spiritdancer |",
         "| `CreateTokenCopyOfEnteringPermanentEffect` | `()` / `(grantHaste, exileAtEndStep[, sacrificeAtEndStep"
         "[, trackWithSource, createForEnteringController[, removeLegendary]]])` / `nonlegendary()` | non-targeting "
         "effect that creates a token copy of the stack entry's triggering permanent (the permanent that entered, "
         "the creature that dealt combat damage), as it is on the battlefield as the ability resolves, else as it "
         "last existed there. Used by Ondu Spiritdancer; Impostor Syndrome's `nonlegendary()` |"),
        ("| `TurnSourceFaceUpEffect` | `()` | source-only effect that turns a face-down source permanent face up "
         "without paying its turn-face-up cost; used by Staff Room (DSC 33) |",
         "| `TurnSourceFaceUpEffect` | `()` / `ofTriggeringPermanent()` | turns a face-down source permanent (or, "
         "`ofTriggeringPermanent()`, the stack entry's triggering permanent: \"turn that creature face up\") face up "
         "without paying its turn-face-up cost; Staff Room (DSC 33) uses `ofTriggeringPermanent()` |\n"
         "| `DoublePlusOneCountersOnSourceEffect` | `()` / `ofTriggeringPermanent()` | doubles the +1/+1 counters on "
         "the source (or, `ofTriggeringPermanent()`, on the stack entry's triggering permanent: \"double the number "
         "of +1/+1 counters on it\", Aragorn, Hornburg Hero) as the ability resolves (CR 701.10e) |"),
        ("/ `flickerTarget()` / ",
         "/ `flickerTarget()` / `flickerTriggeringPermanent()` (scope `TRIGGERING`: \"exile it, then return it\", "
         "the stack entry's triggering permanent; Gossip's Talent) / "),
        (re.compile(r"^(\| `TriggeringCardMatches` \(condition\) \|.*\n)", re.M),
         "\\1| `TriggeringPermanentMatches` (condition) | `(PermanentPredicate filter)` | "
         "`ConditionalEffect(new TriggeringPermanentMatches(p), effect)`: an \"if\" of an effect on the stack "
         "entry's triggering permanent, checked as the effect resolves (as it last existed once it left the "
         "battlefield): \"draw a card if that creature has a +1/+1 counter on it\" (Marcus, Mutant Mayor). A filter "
         "on what triggers the ability is the trigger's own |" "\n",
         "| `TriggeringPermanentMatches` (condition) |"),
    ],
    "CARD_PATTERNS_PERMANENTS_ARTIFACTS.md": [
        ("plus `AllyCombatDamageTriggerEffect` filtered to your creature tokens",
         "plus `Triggers.creatureYouControlDealsCombatDamageToPlayer()` filtered to your creature tokens with "
         "`TriggerOptions.ONE_OR_MORE`"),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("`ChooseOpponentOnEnterEffect()` + `Triggers.creatureYouControlDealsCombatDamageToPlayer().withFilter("
         "PermanentIsCreaturePredicate), AllyCombatDamageTriggerEffect.boundToDealer(DealDamageToPlayersEffect("
         "EventValue, CHOSEN_PLAYER))`",
         "`ChoosePlayerOnEnterEffect()` + `Triggers.creatureYouControlDealsCombatDamageToPlayer().withFilter("
         "PermanentIsCreaturePredicate), DealDamageToChosenPlayerEffect(new EventValue())`"),
        ("with `AllyCombatDamageTriggerEffect(..., oncePerDamageStep=true)` and",
         "with `.withFilter(PermanentHasAnySubtypePredicate(...))`, `TriggerOptions.ONE_OR_MORE` and"),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("`Triggers.creatureYouControlDealsCombatDamageToPlayer() AllyCombatDamageTriggerEffect("
         "PermanentHasSubtypePredicate(SQUIRREL), MayEffect(SacrificePermanentThenEffect(PermanentIsTokenPredicate, "
         "PutCounterOnEachControlledPermanentEffect(ACORN, 1, PermanentNamedPredicate(\"Chitterspitter\"))), false, "
         "true)`",
         "`Triggers.creatureYouControlDealsCombatDamageToPlayer().withFilter(PermanentHasSubtypePredicate(SQUIRREL)), "
         "TriggerOptions.ONE_OR_MORE, MayEffect(SacrificePermanentThenEffect(PermanentIsTokenPredicate, "
         "PutCounterOnEachControlledPermanentEffect(ACORN, 1, PermanentNamedPredicate(\"Chitterspitter\"))))`"),
        ("`ChooseOpponentOnEnterEffect()` + `trigger.withFilter(PermanentIsCreaturePredicate), "
         "AllyCombatDamageTriggerEffect.boundToDealer(DealDamageToPlayersEffect(new EventValue(), CHOSEN_PLAYER))`",
         "`ChoosePlayerOnEnterEffect()` + `trigger.withFilter(PermanentIsCreaturePredicate), "
         "DealDamageToChosenPlayerEffect(new EventValue())`"),
        ("| `AllyCombatDamageTriggerEffect.boundToDealer(ChooseOneEffect(TurnSourceFaceUpEffect(), "
         "PutCountersOnSelfEffect(PLUS_ONE_PLUS_ONE)))` | `Triggers.creatureYouControlDealsCombatDamageToPlayer()` | "
         "Experimental Lab // Staff Room (DSC 33), Staff Room door; binding the combat trigger source to the damage "
         "dealer makes both modes act on that creature |",
         "| `ChooseOneEffect(TurnSourceFaceUpEffect.ofTriggeringPermanent(), "
         "PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING, PLUS_ONE_PLUS_ONE))` | "
         "`Triggers.creatureYouControlDealsCombatDamageToPlayer()` | Experimental Lab // Staff Room (DSC 33), Staff "
         "Room door; both modes act on the stack entry's triggering permanent, the creature that dealt the damage |"),
        ("+ `AllyCombatDamageTriggerEffect(PermanentHasSubtypePredicate(VILLAIN), SequenceEffect.of("
         "GainControlOfTargetEffect(UNTIL_END_OF_YOUR_NEXT_TURN), GrantStaticEffectToTargetUntilEndOfYourNextTurnEffect("
         "CantBeSacrificedEffect())), oncePerDamageStep=true)`",
         "+ `.withFilter(PermanentHasSubtypePredicate(VILLAIN)), TriggerOptions.ONE_OR_MORE, SequenceEffect.of("
         "GainControlOfTargetEffect(UNTIL_END_OF_YOUR_NEXT_TURN), GrantStaticEffectToTargetUntilEndOfYourNextTurnEffect("
         "CantBeSacrificedEffect()))`"),
        ("Broader than `AllyCombatDamageTriggerEffect` (which is player-damage only)",
         "Broader than a `Triggers.creatureYouControlDealsCombatDamageToPlayer()` trigger (player damage only)"),
        ("List.of(LegacySlots.triggered(Triggers.creatureWithSameNameDealsCombatDamageToPlayer(), List.of(new "
         "AllyCombatDamageTriggerEffect(new PermanentHasSameNameAsSourcePredicate(), new DrawCardEffect(1))))))`",
         "List.of(new TriggeredAbility(Triggers.creatureWithSameNameDealsCombatDamageToPlayer(), new "
         "DrawCardEffect(1))))`"),
    ],
    "CARD_PATTERN_INDEX.md": [
        ("`p/PepRaucousRaider.java` + `AllyCombatDamageTriggerEffect` + ",
         "`p/PepRaucousRaider.java` + `Triggers.creatureYouControlDealsCombatDamageToPlayer()` + "),
    ],
    "TRIGGER_SLOT_TARGETING.md": [
        ("(read through `CombatAbilityReader.allyCombatDamage`, which takes the trigger's filter and \"one or more\" "
         "from the ability)",
         "(read through `CombatAbilityReader.read`, which takes the trigger's filter and \"one or more\" from the "
         "ability)"),
    ],
    "ACTIVATED_ABILITY_GUIDE.md": [
        ("Holds an `AllyCombatDamageTriggerEffect`; the stack entry's source",
         "The creature's filter is the trigger's (`.withFilter(p)`); the stack entry's source"),
    ],
    "CARD_PATTERNS_PERMANENTS_STATIC.md": [
        ("Triggers.creatureYouControlDealsCombatDamageToPlayer() AllyCombatDamageTriggerEffect("
         "PermanentHasSubtypePredicate(VAMPIRE), PutCountersOnSourceEffect(1,1,1), bindSourceToDealer=true)",
         "Triggers.creatureYouControlDealsCombatDamageToPlayer().withFilter(PermanentHasSubtypePredicate(VAMPIRE)) "
         "PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING, PLUS_ONE_PLUS_ONE)"),
        ("Triggers.creatureYouControlDealsCombatDamageToPlayer() AllyCombatDamageTriggerEffect("
         "PermanentIsCreaturePredicate(), PutCountersOnSelfEffect(PLUS_ONE_PLUS_ONE, EventValue()), "
         "bindSourceToDealer=true)",
         "Triggers.creatureYouControlDealsCombatDamageToPlayer().withFilter(PermanentIsCreaturePredicate()) "
         "PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING, PLUS_ONE_PLUS_ONE, EventValue())"),
    ],
    "ARCHITECTURE.md": [
        ("`CombatAbilityReader.allyCombatDamage` reads the ally combat damage abilities' filter and \"one or more\" "
         "from the ability and the event bindings from an authored `AllyCombatDamageTriggerEffect`, and",
         "the ally combat damage collectors read the abilities' filter and \"one or more\" from the ability and "
         "record the creature that dealt the damage as the stack entry's triggering permanent (its creatures as the "
         "triggering permanents for \"one or more\"), which \"it\" / \"that creature\" reads, and"),
    ],
}

SKILL_EDITS = {
    ROOT / ".claude/skills/implement-card/SKILL.md": [
        ("and on the ally combat damage triggers author `AllyCombatDamageTriggerEffect` only as "
         "`AllyCombatDamageTriggerEffect.boundToDealer(effect)` (\"put a counter on it / that creature\") or "
         "`.opponentsDealtDamage(effect)` (\"whenever your opponents are dealt combat damage\"); a plain effect is the "
         "usual form.",
         "and on the ally combat damage triggers \"it\" / \"that creature\" is the stack entry's triggering "
         "permanent (`PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING, …)` and the other "
         "triggering-permanent forms in `EFFECTS_QUICK_REFERENCE.md`), never an effect on the source."),
    ],
}

REMOVED = re.compile(r"AllyCombatDamageTriggerEffect|boundToDealer|opponentsDealtDamage|allyCombatDamage\b")


def apply(path, edits):
    text = path.read_text()
    for old, new, *marker in edits:
        if isinstance(old, re.Pattern):
            # A pattern edit is skipped once its marker (the inserted text) is there; a deletion just finds nothing.
            if marker and marker[0] in text:
                continue
            text = old.sub(new, text)
            continue
        if old in text:
            if text.count(old) != 1:
                sys.exit(f"{path}: more than one occurrence of {old[:80]}")
            text = text.replace(old, new)
        elif new not in text:
            sys.exit(f"{path}: neither the old nor the new form of {old[:80]}")
    path.write_text(text)


def main():
    for name, edits in EDITS.items():
        apply(DOCS / name, edits)
    for path, edits in SKILL_EDITS.items():
        apply(path, edits)
    left = []
    for path in list(DOCS.glob("*.md")) + list((ROOT / ".claude/skills").rglob("*.md")):
        if path.name == "EFFECT_MERGE_PLAN.md":
            continue
        for number, line in enumerate(path.read_text().splitlines(), 1):
            if REMOVED.search(line):
                left.append(f"{path.relative_to(ROOT)}:{number}")
    if left:
        sys.exit("still named: " + ", ".join(left))
    print("docs updated")


if __name__ == "__main__":
    main()
