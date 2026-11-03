#!/usr/bin/env python3
"""Step 14, run 6a of effect-slot-refactor.md: rewrites the agent-docs rows of the spell, land-play and activation
abilities step14-spell-options.py converted, and the entries of the wrappers those abilities no longer author
(TriggerOptions for the limit and the intervening "if", the trigger's filters for the land played and the permanent
or kind of ability activated). Each replacement is exact and must apply once.

Usage: python3 scripts/step14-spell-docs.py
"""
import sys

ONCE = "TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN)"
FILTER_ROW = ("`.withSource(PermanentPredicate)` / `.withKind(AbilityTrigger.AbilityKind.X)` on the activation trigger "
              "(the trigger's filter; never the wrapper, step 14 of effect-slot-refactor.md)")

EDITS = {
    "agent-docs/ORACLE_TEXT_EFFECT_MAP.md": [
        ("`Triggers.youCastSpell() OncePerTurnTriggerEffect(SpellCastTriggerEffect(CardAllOfPredicate(CREATURE, "
         "CardNotPredicate(CardKeywordPredicate(FLYING))), [ConjureDuplicateOfTriggeringCreatureToHandEffect()]))`",
         "`Triggers.youCastSpell()`, `" + ONCE + "`, `SpellCastTriggerEffect(CardAllOfPredicate(CREATURE, "
         "CardNotPredicate(CardKeywordPredicate(FLYING))), [ConjureDuplicateOfTriggeringCreatureToHandEffect()])`"),
        ("`Triggers.youCastSpell() OncePerTurnTriggerEffect(SpellCastTriggerEffect(CardSubtypePredicate(OMEN), "
         "List.of(ConjureDuplicateOfTriggeringSpellOntoBattlefieldEffect())))`",
         "`Triggers.youCastSpell()`, `" + ONCE + "`, `SpellCastTriggerEffect(CardSubtypePredicate(OMEN), "
         "List.of(ConjureDuplicateOfTriggeringSpellOntoBattlefieldEffect()))`"),
        ("`Triggers.youPlayLand()` / `Triggers.opponentPlaysLand() TriggeringCardConditionalEffect("
         "CardSubtypePredicate(DESERT), draw + GainLifeEffect(3))`",
         "`Triggers.youPlayLand().withFilter(desert)` / `Triggers.opponentPlaysLand().withFilter(desert)` with draw + "
         "`GainLifeEffect(3)` (`desert` = `CardSubtypePredicate(DESERT)`)"),
        ("`Triggers.youActivateAbility()` + `TriggeringNinjutsuAbilityConditionalEffect(OncePerTurnTriggerEffect("
         "LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(3))))`",
         "`Triggers.youActivateAbility().withKind(AbilityTrigger.AbilityKind.NINJUTSU)`, `" + ONCE + "`, "
         "`LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(3))`"),
        ("`Triggers.youActivateAbility() TriggeringPowerUpAbilityConditionalEffect(PutCountersOnSelfEffect("
         "PLUS_ONE_PLUS_ONE))`",
         "`Triggers.youActivateAbility().withKind(AbilityTrigger.AbilityKind.POWER_UP) PutCountersOnSelfEffect("
         "PLUS_ONE_PLUS_ONE)`"),
        ("| `TriggeringPermanentConditionalEffect(new PermanentHasSubtypePredicate(subtype), new BoostSelfEffect(1, 0))` "
         "| `Triggers.youActivateAbility()` |",
         "| `new BoostSelfEffect(1, 0)` | `Triggers.youActivateAbility().withSource(new PermanentHasSubtypePredicate("
         "subtype))` |"),
        ("the conditional filters by the permanent whose ability was activated |",
         "the trigger's source filter is judged on the permanent whose ability was activated |"),
        ("`Triggers.youPlayLand() TriggeringCardConditionalEffect(CardTruePredicate, ConditionalEffect("
         "ControllerPlayedAtLeastLandsThisTurn(2), DealDamageToPlayersEffect(1, CONTROLLER)))`",
         "`Triggers.youPlayLand()`, `TriggerOptions.DEFAULT.withInterveningIf(ControllerPlayedAtLeastLandsThisTurn(2))`, "
         "`DealDamageToPlayersEffect(1, CONTROLLER)`"),
        ("| `TriggeringPermanentConditionalEffect(new PermanentAnyOfPredicate(List.of(new PermanentIsArtifactPredicate(), "
         "new PermanentIsCreaturePredicate(), new PermanentIsLandPredicate())), new DealDamageToPlayersEffect(N, "
         "DamageRecipient.TARGET_PLAYER))` | Triggers.opponentActivatesNonManaAbility() |",
         "| `new DealDamageToPlayersEffect(N, DamageRecipient.TARGET_PLAYER)` | "
         "`Triggers.opponentActivatesNonManaAbility().withSource(new PermanentAnyOfPredicate(List.of("
         "new PermanentIsArtifactPredicate(), new PermanentIsCreaturePredicate(), new PermanentIsLandPredicate())))` |"),
        ("the conditional wrapper filters by the activated permanent's type;",
         "the trigger's source filter is judged on the activated permanent;"),
        ("plus `TriggeringNonTapAbilityConditionalEffect(TriggeringPermanentConditionalEffect(new "
         "PermanentIsHostOfSourceAuraPredicate(), DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PERMANENT_"
         "CONTROLLER)))` in the controller and opponent ability-activation slots | Triggers.enchantedPermanentBecomesTapped(), "
         "Triggers.youActivateAbility(), Triggers.opponentActivatesAbility() |",
         "plus the same effect in `Triggers.youActivateAbility()` and `Triggers.opponentActivatesAbility()`, each "
         "`.withKind(AbilityTrigger.AbilityKind.WITHOUT_TAP_COST).withSource(new PermanentIsHostOfSourceAuraPredicate())` "
         "| Triggers.enchantedPermanentBecomesTapped(), Triggers.youActivateAbility(), "
         "Triggers.opponentActivatesAbility() |"),
        ("`TriggeringLoyaltyAbilityConditionalEffect(TriggeringPermanentConditionalEffect(PermanentAllOfPredicate("
         "PLANESWALKER, PermanentIsHostOfSourceAuraPredicate), SequenceEffect.of(BoostAllOwnCreaturesEffect(2, 2), "
         "GrantKeywordEffect(VIGILANCE, ALL_OWN_CREATURES))))` | STATIC + Triggers.youActivateAbility() |",
         "`Triggers.youActivateAbility().withKind(AbilityTrigger.AbilityKind.LOYALTY).withSource("
         "PermanentAllOfPredicate(PLANESWALKER, PermanentIsHostOfSourceAuraPredicate))` with "
         "`SequenceEffect.of(BoostAllOwnCreaturesEffect(2, 2), GrantKeywordEffect(VIGILANCE, ALL_OWN_CREATURES))` "
         "| STATIC + Triggers.youActivateAbility() |"),
    ],
    "agent-docs/CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("Triggers.youActivateAbility() TriggeringPermanentConditionalEffect(PermanentHasSubtypePredicate(ELEMENTAL), "
         "BoostSelfEffect(1, 0)) — fires once per activated-ability activation (incl. mana abilities) of a permanent "
         "you control; the conditional filters by the activated permanent's subtype",
         "Triggers.youActivateAbility().withSource(PermanentHasSubtypePredicate(ELEMENTAL)) BoostSelfEffect(1, 0) — "
         "fires once per activated-ability activation (incl. mana abilities) of a permanent you control; the "
         "trigger's source filter is judged on the activated permanent"),
        ("Triggers.opponentActivatesNonManaAbility() TriggeringPermanentConditionalEffect(PermanentAnyOfPredicate("
         "artifact/creature/land), DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER)) — fires only on the "
         "non-mana activation path (mana abilities excluded automatically); conditional wrapper filters by the "
         "activated permanent's type;",
         "Triggers.opponentActivatesNonManaAbility().withSource(PermanentAnyOfPredicate(artifact/creature/land)) "
         "DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER) — fires only on the non-mana activation path "
         "(mana abilities excluded automatically); the trigger's source filter is judged on the activated permanent;"),
    ],
    "agent-docs/TRIGGER_SLOT_TARGETING.md": [
        ("wrap in `TriggeringPermanentConditionalEffect` to filter by the permanent whose ability was activated),",
         "`.withSource(filter)` filters by the permanent whose ability was activated),"),
        ("abilities never trigger it; wrap in `TriggeringPermanentConditionalEffect` to filter by the activated\n"
         "permanent's type;",
         "abilities never trigger it; `.withSource(filter)` filters by the activated\n"
         "permanent's type;"),
    ],
    "agent-docs/CARD_PATTERNS_PERMANENTS_STATIC.md": [
        ("Triggers.youActivateNonManaAbility() + Triggers.opponentActivatesNonManaAbility() use a tap-cost trigger "
         "condition,",
         "Triggers.youActivateNonManaAbility() + Triggers.opponentActivatesNonManaAbility(), each `.withKind(AbilityTrigger."
         "AbilityKind.WITH_TAP_COST).withSource(new PermanentIsHostOfSourceAuraPredicate())`,"),
    ],
    "agent-docs/ACTIVATED_ABILITY_GUIDE.md": [
        ("hand. A battlefield trigger watching those grants can use\n"
         "`TriggeringNinjutsuAbilityConditionalEffect` in `Triggers.youActivateAbility()`; the hand-ninjutsu",
         "hand. A battlefield trigger watching those grants is\n"
         "`Triggers.youActivateAbility().withKind(AbilityTrigger.AbilityKind.NINJUTSU)`; the hand-ninjutsu"),
    ],
    "agent-docs/CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md": [
        ("Static `OncePerTurnTriggerEffect(CopyControllerCastSpellOnSpellCastEffect(INSTANT|SORCERY))`;",
         "`Triggers.youCastSpell()`, `" + ONCE + "`, `CopyControllerCastSpellOnSpellCastEffect(INSTANT|SORCERY)`;"),
    ],
    "agent-docs/EFFECTS_INDEX.md": [
        ("| `TriggeringTapAbilityConditionalEffect` | `(CardEffect wrapped)` | Trigger-only wrapper that keeps the wrapped "
         "effect only when the activated ability requires tapping its source; used for “with {T} in its activation "
         "cost” ability triggers (Imprison) |",
         "| `TriggeringTapAbilityConditionalEffect` | `(CardEffect wrapped)` | **Don't author.** The legacy view's form "
         "of `AbilityKind.WITH_TAP_COST`; “with {T} in its activation cost” is `Triggers.youActivateNonManaAbility()"
         ".withKind(AbilityTrigger.AbilityKind.WITH_TAP_COST)` (Imprison) |"),
        ("| `TriggeringPowerUpAbilityConditionalEffect` | `(CardEffect wrapped)` | Trigger-only wrapper that keeps the "
         "wrapped effect only when the activated ability is marked as Power-up; used on `Triggers.youActivateAbility()` "
         "for “whenever you activate a power-up ability” |",
         "| `TriggeringPowerUpAbilityConditionalEffect` | `(CardEffect wrapped)` | **Don't author.** The legacy view's "
         "form of `AbilityKind.POWER_UP`; write `Triggers.youActivateAbility().withKind(AbilityTrigger.AbilityKind."
         "POWER_UP)` for “whenever you activate a power-up ability” (Marvel Boy, Noh-Varr) |"),
        ("| `TriggeringOutlastAbilityConditionalEffect` | `(CardEffect wrapped)` | Trigger-only wrapper that keeps the "
         "wrapped effect only when the activated ability is marked with `ActivatedAbility.withOutlast()`; used on "
         "`Triggers.youActivateAbility()` for “whenever you activate this creature's outlast ability” (wrap the source "
         "check with `TriggeringPermanentConditionalEffect(PermanentIsSourceCardPredicate)`). Used by Herald of Anafenza |",
         "| `TriggeringOutlastAbilityConditionalEffect` | `(CardEffect wrapped)` | **Don't author.** The legacy view's "
         "form of `AbilityKind.OUTLAST` (abilities marked with `ActivatedAbility.withOutlast()`); “whenever you "
         "activate this creature's outlast ability” is `Triggers.youActivateAbility().withKind(AbilityTrigger."
         "AbilityKind.OUTLAST).withSource(new PermanentIsSourceCardPredicate())` (Herald of Anafenza) |"),
        ("| `TriggeringLoyaltyAbilityConditionalEffect` | `(CardEffect wrapped)` | Trigger-only wrapper that keeps the "
         "wrapped effect only when the activated ability has a loyalty cost; used for “whenever you activate a loyalty "
         "ability” triggers |",
         "| `TriggeringLoyaltyAbilityConditionalEffect` | `(CardEffect wrapped)` | **Don't author.** The legacy view's "
         "form of `AbilityKind.LOYALTY`; “whenever you activate a loyalty ability [of a Chandra planeswalker]” is "
         "`Triggers.youActivateAbility().withKind(AbilityTrigger.AbilityKind.LOYALTY)[.withSource(filter)]` |"),
        ("Not for zone changes: a restriction on the object that moved is the trigger's filter",
         "Not for activations (\"an ability of an artifact\" is `Triggers.youActivateAbility().withSource(filter)`) nor "
         "zone changes: a restriction on the object that moved is the trigger's filter"),
        ("| `TriggeringNonTapAbilityConditionalEffect` | `(CardEffect wrapped)` | Trigger-only wrapper that keeps the "
         "wrapped effect only when the activated ability does not require tapping its source; used for “without {T} in "
         "its activation cost” ability triggers (Artifact Possession) |",
         "| `TriggeringNonTapAbilityConditionalEffect` | `(CardEffect wrapped)` | **Don't author.** The legacy view's "
         "form of `AbilityKind.WITHOUT_TAP_COST`; write " + FILTER_ROW + " (Artifact Possession) |"),
    ],
    "agent-docs/EFFECTS_QUICK_REFERENCE.md": [
        ("- `TriggeringTapAbilityConditionalEffect(CardEffect)` — trigger-only wrapper that keeps the wrapped effect only "
         "when the activated ability requires tapping its source; use on ability-activation trigger slots for “with {T} "
         "in its activation cost” conditions (Imprison)",
         "- `TriggeringTapAbilityConditionalEffect(CardEffect)` — **don't author**: “with {T} in its activation cost” is "
         "`.withKind(AbilityTrigger.AbilityKind.WITH_TAP_COST)` on the activation trigger (Imprison); the collectors "
         "get this wrapper from the legacy view"),
        ("- `TriggeringNinjutsuAbilityConditionalEffect` — trigger-only wrapper that exposes its payload when\n"
         "  the activated ability is ninjutsu (Satoru Umezawa)\n"
         "- `TriggeringPowerUpAbilityConditionalEffect` — trigger-only wrapper that exposes its payload when\n"
         "  the activated ability is marked as Power-up (Marvel Boy, Noh-Varr)",
         "- “Whenever you activate a ninjutsu / power-up / boast / outlast / loyalty ability” is\n"
         "  `Triggers.youActivateAbility().withKind(AbilityTrigger.AbilityKind.NINJUTSU)` (`POWER_UP`, `BOAST`,\n"
         "  `OUTLAST`, `LOYALTY`; Satoru Umezawa, Marvel Boy, Noh-Varr), not the `Triggering…AbilityConditionalEffect`\n"
         "  wrappers, which only the legacy view still builds"),
        ("- `TriggeringLoyaltyAbilityConditionalEffect(CardEffect)` — trigger-only wrapper that keeps the wrapped effect "
         "only when the activated ability has a loyalty cost; use for “whenever you activate a loyalty ability” "
         "conditions",
         "- `TriggeringLoyaltyAbilityConditionalEffect(CardEffect)` — **don't author**: “whenever you activate a "
         "loyalty ability” is `Triggers.youActivateAbility().withKind(AbilityTrigger.AbilityKind.LOYALTY)`"),
        ("- `TriggeringNonTapAbilityConditionalEffect(CardEffect)` — trigger-only wrapper that keeps the wrapped effect "
         "only when the activated ability does not require tapping its source; use on ability-activation trigger slots "
         "for “without {T} in its activation cost” conditions (Artifact Possession)",
         "- `TriggeringNonTapAbilityConditionalEffect(CardEffect)` — **don't author**: “without {T} in its activation "
         "cost” is `Triggers.youActivateAbility().withKind(AbilityTrigger.AbilityKind.WITHOUT_TAP_COST)` (Artifact "
         "Possession); the collectors get this wrapper from the legacy view"),
    ],
}


def main():
    for path, edits in EDITS.items():
        text = open(path).read()
        for old, new in edits:
            if old not in text and text.count(new) == 1:
                continue
            if text.count(old) != 1:
                sys.exit("%s: expected one %r, found %d" % (path, old[:80], text.count(old)))
            text = text.replace(old, new)
        open(path, "w").write(text)


if __name__ == "__main__":
    sys.exit(main())
