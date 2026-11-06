#!/usr/bin/env python3
"""EffectSlot refactor step 15, run 17b: the agent-docs stop naming CopyControllerCastSpellOnSpellCastEffect and
GainControlOfTargetCreatureByCastSpellManaValueEffect, and describe the copy trigger as the cast trigger's filter and
options with CopyTriggeringSpellEffect.

Idempotent: every line that names a removed class is replaced (or dropped, for the variants one new line covers), by
the unique text it starts with. Fails if a removed class is still named afterwards.

    python3 scripts/step15-copy-cast-spell-docs.py
"""
import pathlib
import sys

DOCS = pathlib.Path(__file__).resolve().parent.parent / "agent-docs"
REMOVED = ("CopyControllerCastSpellOnSpellCastEffect", "GainControlOfTargetCreatureByCastSpellManaValueEffect")

COPY_INDEX = (
    "| `CopyTriggeringSpellEffect` | `([boolean tokenCopy[, boolean retargetToSource]])` + `with…` modifiers | "
    "spell-cast trigger effect that copies the spell that caused the resolving trigger (the live spell, else the stack "
    "entry's triggering spell as it was when the ability triggered, CR 608.2h), with its targets, under the spell's "
    "controller. Put the trigger condition on the cast trigger (`Triggers.youCastSpell().withFilter(…)`, "
    "`.withStackEntryFilter(…)`, `.castFrom(zone)`, `.notCastFrom(Zone.HAND)`, `.nthThisTurn(1)`, `.anySpell()`), the "
    "\"if\" in `TriggerOptions` (`withInterveningIf`, or `withEventCondition` for \"while …\"), and the cost around the "
    "effect (`MayEffect`, `MayPayManaEffect`, `MayPayTapPermanentsEffect`, `SacrificePermanentThenEffect`). "
    "Modifiers: `withoutNewTargetChoice()` (a plain \"copy it\"), `tokenCopy=true` (the copy is a token), "
    "`permanentSpellCopiesAreTokens()`, `permanentSpellCopiesGainHaste()`, `sacrificedAtEndStep()` (token copies), "
    "`withGrantedKeywords(Set)` (\"those spells gain wither\": the spell and the copy), `withAdditionalTypes(Set)`, "
    "`withoutSupertypes(Set)` (\"except the copy isn't legendary\"), `asAdditionally(List<CardSubtype>, p, t)` "
    "(\"except the copy is a 1/1 Spirit\"), `withCopies(n)` (\"copy it twice\"). `targetingSource()` instead controls "
    "the copy, retargets every copied target occurrence to the trigger source, and creates no retarget choice. Used by "
    "Swarm Intelligence, Aziza, Mirari, Mica, Spinerock Tyrant, Donal, Nalfeshnee and every other cast-copy trigger |"
)
COPY_QUICK = (
    "- `CopyTriggeringSpellEffect([boolean tokenCopy[, boolean retargetToSource]])` — copies the spell that caused the "
    "resolving spell-cast trigger (the stack entry's triggering spell, as it was when the ability triggered, once it "
    "has left the stack), under the spell's controller. The trigger condition is the cast trigger's filter "
    "(`Triggers.youCastSpell().withFilter(INSTANT_OR_SORCERY)`, `.castFrom(Zone.LIBRARY)`, `.notCastFrom(Zone.HAND)`, "
    "`.nthThisTurn(1)`, `.withStackEntryFilter(new StackEntryCastWithAdventurePredicate())`), the cost the effect "
    "around it (`new MayPayManaEffect(\"{2}\", copy, prompt)`, `MayPayTapPermanentsEffect`, `MayEffect`, "
    "`SacrificePermanentThenEffect`). Modifiers: `withoutNewTargetChoice()`, `permanentSpellCopiesAreTokens()`, "
    "`permanentSpellCopiesGainHaste()`, `sacrificedAtEndStep()`, `withGrantedKeywords(Set)`, `withAdditionalTypes(Set)`, "
    "`withoutSupertypes(Set)`, `asAdditionally(subtypes, p, t)`, `withCopies(n)`; `tokenCopy=true` makes every copy a "
    "token, and `targetingSource()` controls the copy and retargets every copied target occurrence to the trigger "
    "source without a choose-new-targets prompt. An emblem's \"whenever you cast …, copy it\" is "
    "`new SpellCastTriggerEffect(filter, List.of(copy))`"
)

# (file, unique start of the old line, new line or None to drop it)
EDITS = [
    ("EFFECTS_INDEX.md", "| `CopyTriggeringSpellEffect` | `([boolean tokenCopy[, boolean retargetToSource]])` |",
     COPY_INDEX),
    ("EFFECTS_INDEX.md", "- `CopyControllerCastSpellOnSpellCastEffect.tokenCopy(", None),
    ("EFFECTS_INDEX.md", "`CopyControllerCastSpellOnSpellCastEffect.withSacrificeFilter(", None),
    ("EFFECTS_INDEX.md", "| `MayPayTapPermanentsEffect` | `(TapMultiplePermanentsCost tapCost,", "TAP_INDEX"),
    ("EFFECTS_INDEX.md", "| `GainControlOfTargetCreatureByCastSpellManaValueEffect` |", None),
    ("EFFECTS_INDEX.md", "| `CopyControllerCastSpellOnSpellCastEffect` | `fromOutsideHand", None),
    ("EFFECTS_INDEX.md", "| `CopyControllerCastSpellOnSpellCastEffect` | `fromExileWith", None),
    ("EFFECTS_INDEX.md", "| `CopyControllerCastSpellOnSpellCastEffect.asArtifactToken`", None),
    ("EFFECTS_INDEX.md", "| `CopyControllerCastSpellOnSpellCastEffect.permanentSpellToken`", None),
    ("EFFECTS_INDEX.md", "| `CopyControllerCastSpellOnSpellCastEffect.withTriggerCondition`", None),
    ("EFFECTS_QUICK_REFERENCE.md", "- `CopyTriggeringSpellEffect([boolean tokenCopy[, boolean retargetToSource]])` —",
     COPY_QUICK),
    ("EFFECTS_QUICK_REFERENCE.md", "- `CopyControllerCastSpellOnSpellCastEffect.tokenCopy(", None),
    ("EFFECTS_QUICK_REFERENCE.md", "- `CopyControllerCastSpellOnSpellCastEffect.permanentSpellToken(", None),
    ("EFFECTS_QUICK_REFERENCE.md", "- `GainControlOfTargetCreatureByCastSpellManaValueEffect(", None),
    ("EFFECTS_QUICK_REFERENCE.md", "- `CopyControllerCastSpellOnSpellCastEffect(CardPredicate, TapMultiplePermanentsCost)` — Triggers.youCastSpell(): copy cast instant/sorcery; optional tap cost wraps `MayPayTapPermanentsEffect` + `CopyControllerCastSpellEffect` (Aziza, Mage Tower Captain). Pass `null` tap cost and wrap in `MayEffect` for a free \"you may copy that spell\" (Swarm Intelligence)\n", None),
    ("EFFECTS_QUICK_REFERENCE.md", "- `CopyControllerCastSpellOnSpellCastEffect.withIntervening(", None),
    ("EFFECTS_QUICK_REFERENCE.md", "- `CopyControllerCastSpellOnSpellCastEffect.withTriggerCondition(", None),
    ("EFFECTS_QUICK_REFERENCE.md", "- `CopyControllerCastSpellOnSpellCastEffect.fromOutsideHandWithPermanentSpellHaste(", None),
    ("EFFECTS_QUICK_REFERENCE.md", "- `CopyControllerCastSpellOnSpellCastEffect.fromExileWithPermanentSpellHasteAndSacrifice(", None),
    ("EFFECTS_QUICK_REFERENCE.md", "- `CopyControllerCastSpellOnSpellCastEffect(CardPredicate, TapMultiplePermanentsCost)` — Triggers.youCastSpell(): copy cast instant/sorcery; optional tap cost wraps `MayPayTapPermanentsEffect` + `CopyControllerCastSpellEffect` (Aziza, Mage Tower Captain). Pass `null` tap cost and wrap in `MayEffect` for a free \"you may copy that spell\" (Swarm Intelligence). `CopyControllerCastSpellOnSpellCastEffect(CardPredicate, Zone)`", None),
    ("EFFECTS_QUICK_REFERENCE.md", "- `CopyControllerCastSpellOnSpellCastEffect.withSacrificeFilter(", None),
    ("EFFECTS_QUICK_REFERENCE.md", "- `CopyControllerCastSpellOnSpellCastEffect.asArtifactToken(", None),
    ("ORACLE_TEXT_EFFECT_MAP.md", "| \"Whenever you cast an instant or sorcery spell from your graveyard, copy that spell. You may choose new targets for the copy.\" |",
     "| \"Whenever you cast an instant or sorcery spell from your graveyard, copy that spell. You may choose new targets for the copy.\" | `Triggers.youCastSpell().withFilter(INSTANT_OR_SORCERY).castFrom(Zone.GRAVEYARD)` + `new CopyTriggeringSpellEffect()` | Triggers.youCastSpell() | Geist of Regret (YMID 16). The copy effect reads the triggering spell from the stack entry and offers new-target choices for targeted copies. |"),
    ("ORACLE_TEXT_EFFECT_MAP.md", "| \"Whenever you cast an artifact spell, you may pay {1}. If you do, copy it. (The copy becomes a token.)\" |",
     "| \"Whenever you cast an artifact spell, you may pay {1}. If you do, copy it. (The copy becomes a token.)\" | `Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.ARTIFACT))`, `TriggerOptions.DEFAULT.withInterveningIf(new MaxSpeed())`, `new MayPayManaEffect(\"{1}\", new CopyTriggeringSpellEffect(true).withoutNewTargetChoice(), prompt)` | Triggers.youCastSpell() | Mendicant Core, Guidelight — the copy is a token and keeps the original spell's targets |"),
    ("ORACLE_TEXT_EFFECT_MAP.md", "| \"Whenever you cast a nonlegendary creature spell with flying, you may copy it, except the copy is a 1/1 Spirit",
     "| \"Whenever you cast a nonlegendary creature spell with flying, you may copy it, except the copy is a 1/1 Spirit in addition to its other types. Do this only once each turn.\" | `Triggers.youCastSpell().withFilter(creatureAndFlyingAndNonlegendary)` with `TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN)`, `MayEffect(new CopyTriggeringSpellEffect().permanentSpellCopiesAreTokens().asAdditionally(List.of(CardSubtype.SPIRIT), 1, 1), prompt)` | Triggers.youCastSpell() | Donal, Herald of Wings (VOC 3); the copy is a token and keeps the copied spell's other characteristics |"),
    ("ORACLE_TEXT_EFFECT_MAP.md", "| \"Whenever you cast a spell with hybrid mana in its mana cost, copy it. You may choose new targets for the copy.\" |",
     "| \"Whenever you cast a spell with hybrid mana in its mana cost, copy it. You may choose new targets for the copy.\" | `Triggers.youCastSpell().withFilter(new CardHasHybridManaPredicate())` + `new CopyTriggeringSpellEffect()` | Triggers.youCastSpell() | Providence of Night (YECL 25) |"),
    ("ORACLE_TEXT_EFFECT_MAP.md", "| \"Whenever you cast a Spirit or Arcane spell, you may gain control of target creature with that spell's mana value until end of turn\" |",
     "| \"Whenever you cast a Spirit or Arcane spell, you may gain control of target creature with that spell's mana value until end of turn\" | `target(new PermanentPredicateTargetFilter(new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(), new PermanentManaValueEqualsXPredicate())), msg))` + `Triggers.youCastSpell().withFilter(SPIRIT_OR_ARCANE)`, `MayEffect(new GainControlOfTargetEffect(ControlDuration.END_OF_TURN), prompt)` | `Triggers.youCastSpell()` | Skyfire Kirin. A cast trigger whose target filter reads the mana value as X is chosen as the ability goes on the stack (CR 603.3d) with the spell's mana value as the ability's X, and checked against it again as it resolves |"),
    ("ORACLE_TEXT_EFFECT_MAP.md", "| \"whenever you cast an instant or sorcery spell, you may tap three untapped creatures you control. If you do, copy that spell\" |",
     "| \"whenever you cast an instant or sorcery spell, you may tap three untapped creatures you control. If you do, copy that spell\" | `Triggers.youCastSpell().withFilter(INSTANT_OR_SORCERY)` + `new MayPayTapPermanentsEffect(new TapMultiplePermanentsCost(3, new PermanentIsCreaturePredicate()), new CopyTriggeringSpellEffect(), prompt)` | Triggers.youCastSpell() | Aziza, Mage Tower Captain — the tap is chosen and paid as the ability resolves |"),
    ("ORACLE_TEXT_EFFECT_MAP.md", "| \"whenever you cast a multicolored instant or sorcery spell, you may pay {1}. If you do, copy that spell\" |",
     "| \"whenever you cast a multicolored instant or sorcery spell, you may pay {1}. If you do, copy that spell\" | `Triggers.youCastSpell().withFilter(CardAllOfPredicate(CardIsMulticoloredPredicate, INSTANT_OR_SORCERY))` + `new MayPayManaEffect(\"{1}\", new CopyTriggeringSpellEffect(), prompt)` | Triggers.youCastSpell() | Cloven Casting — the payment is asked as the ability resolves (CR 603.5) |"),
    ("ORACLE_TEXT_EFFECT_MAP.md", "| \"Whenever you cast an instant or sorcery spell, you may sacrifice an artifact. If you do, copy that spell\" |",
     "| \"Whenever you cast an instant or sorcery spell, you may sacrifice an artifact. If you do, copy that spell\" | `Triggers.youCastSpell().withFilter(INSTANT_OR_SORCERY)` + `MayEffect(new SacrificePermanentThenEffect(new PermanentIsArtifactPredicate(), new CopyTriggeringSpellEffect(), \"an artifact\", false, false), \"Sacrifice an artifact to copy that spell?\")` | Triggers.youCastSpell() | Mica, Reader of Ruins — the sacrifice is chosen as the ability resolves, then the copy is made in the same resolution |"),
    ("ORACLE_TEXT_EFFECT_MAP.md", "| \"whenever you cast an instant or sorcery spell, you may copy that spell. You may choose new targets for the copy\" |",
     "| \"whenever you cast an instant or sorcery spell, you may copy that spell. You may choose new targets for the copy\" | `Triggers.youCastSpell().withFilter(INSTANT_OR_SORCERY)` + `new MayEffect(new CopyTriggeringSpellEffect(), \"Copy that spell?\")` | Triggers.youCastSpell() | Swarm Intelligence — the \"you may\" is asked as the ability resolves (CR 603.5); the copy is still made if the spell was countered in response |"),
    ("ORACLE_TEXT_EFFECT_MAP.md", "| \"Whenever you cast a creature spell that doesn't share a creature type with a creature you control or a creature card in your graveyard, copy that spell.",
     "| \"Whenever you cast a creature spell that doesn't share a creature type with a creature you control or a creature card in your graveyard, copy that spell. (A copy of a creature spell becomes a token.)\" | `Triggers.youCastSpell().withFilter(CardAllOfPredicate(CardTypePredicate(CREATURE), CardNotPredicate(CardSharesCreatureTypeWithControlledCreatureOrGraveyardPredicate)))` + `new CopyTriggeringSpellEffect(true).withoutNewTargetChoice()` | Triggers.youCastSpell() | Volo, Guide to Monsters (AFR) |"),
    ("ORACLE_TEXT_EFFECT_MAP.md", "| \"Whenever you cast an instant or sorcery spell that has the same name as a card in your graveyard, you may put a quest counter on this enchantment.",
     "| \"Whenever you cast an instant or sorcery spell that has the same name as a card in your graveyard, you may put a quest counter on this enchantment. Whenever you cast an instant or sorcery spell while this enchantment has two or more quest counters on it, you may copy that spell\" | `.withFilter(CardAllOfPredicate(INSTANT_OR_SORCERY, CardNameInControllerGraveyardPredicate))` on the cast trigger with `MayEffect(new PutCountersOnSelfEffect(QUEST), \"Put a quest counter …?\")` + `Triggers.youCastSpell().withFilter(INSTANT_OR_SORCERY)`, `TriggerOptions.DEFAULT.withEventCondition(new SourceCounterThreshold(2, QUEST))`, `new MayEffect(new CopyTriggeringSpellEffect(), \"Copy that spell?\")` | Triggers.youCastSpell() | Pyromancer Ascension — \"while\" is an event condition, checked only as the spell is cast: removing the counters or the enchantment in response doesn't stop the copy (the card's rulings) |"),
    ("ORACLE_TEXT_EFFECT_MAP.md", "| \"whenever you cast an instant or sorcery spell from your library, copy it. You may choose new targets for the copy\" |",
     "| \"whenever you cast an instant or sorcery spell from your library, copy it. You may choose new targets for the copy\" | `Triggers.youCastSpell().withFilter(INSTANT_OR_SORCERY).castFrom(Zone.LIBRARY)` + `new CopyTriggeringSpellEffect()` | Triggers.youCastSpell() | Melek, Izzet Paragon — mandatory (no cost, no `MayEffect`). Pair with `PlayWithTopCardRevealedEffect` + `AllowCastFromTopOfLibraryEffect(INSTANT, SORCERY)` on STATIC |"),
    ("ORACLE_TEXT_EFFECT_MAP.md", "| \"Whenever you cast a spell from anywhere other than your hand, you may copy it.",
     "| \"Whenever you cast a spell from anywhere other than your hand, you may copy it. If you do, you may choose new targets for the copy. If the copy is a permanent spell, it gains haste. Do this only once each turn.\" | `Triggers.youCastSpell().anySpell().notCastFrom(Zone.HAND)` with `TriggerOptions.DEFAULT.withLimit(TriggerLimit.DO_ONLY_ONCE_EACH_TURN)`, `new MayEffect(new CopyTriggeringSpellEffect().permanentSpellCopiesGainHaste(), \"Copy that spell?\")` | STATIC + Triggers.youCastSpell() | Spider-Verse (SPM). Accepting the copy uses up the once-per-turn limit |"),
    ("ORACLE_TEXT_EFFECT_MAP.md", "| \"Whenever you cast a permanent spell with a mana cost that contains {X}, double the value of X.",
     "| \"Whenever you cast a permanent spell with a mana cost that contains {X}, double the value of X. Whenever you cast an instant or sorcery spell or activate an ability, if that spell's mana cost or that ability's activation cost contains {X}, copy that spell or ability\" | `DoubleXValueOfTriggeringSpellEffect()` on a filtered cast trigger + `Triggers.youCastSpell().withFilter(CardAllOfPredicate(CardHasXInManaCostPredicate, INSTANT_OR_SORCERY))`, `new CopyTriggeringSpellEffect()` + `CopyControllerActivatedAbilityTriggerEffect(null, null, false, false, null, true)` | Triggers.youCastSpell() + Triggers.youActivateAbility() | Unbound Flourishing — spell copies are mandatory, and activated-ability copies include mana abilities while remaining limited to activations whose mana cost contains `{X}` |"),
    ("ORACLE_TEXT_EFFECT_MAP.md", "| \"Whenever you cast an instant or sorcery spell or activate an ability, if that spell's mana cost or that ability's activation cost contains {X}, copy that spell or ability\" |", None),
    ("ORACLE_TEXT_EFFECT_MAP.md", "| \"Whenever you cast a spell while this creature is attacking, copy that spell. You may choose new targets for the copy.\" |",
     "| \"Whenever you cast a spell while this creature is attacking, copy that spell. You may choose new targets for the copy.\" | `AwardManaUntilEndOfCombatEffect(RED, 2)` on `Triggers.attacks()` + `Triggers.youCastSpell().anySpell()`, `TriggerOptions.DEFAULT.withEventCondition(new SourceIsAttacking())`, `new CopyTriggeringSpellEffect()` | Triggers.attacks() + Triggers.youCastSpell() | Fire Lord Azula (TLA 220) — the attack condition is an event condition, checked when the spell is cast; the copy resolves even if Azula is no longer attacking afterward |"),
    ("CARD_PATTERNS_PERMANENTS_STATIC.md", "| Choose subtype + copy matching spells as tokens | `r/ReflectionsOfLittjara.java` |",
     "| Choose subtype + copy matching spells as tokens | `r/ReflectionsOfLittjara.java` | STATIC ChooseSubtypeOnEnterEffect + `Triggers.youCastSpell().withFilter(CardHasSourceChosenSubtypePredicate(false))` `new CopyTriggeringSpellEffect(true).withoutNewTargetChoice()` — copies any spell carrying the chosen creature subtype; permanent-spell copies resolve as tokens |"),
    ("CARD_PATTERNS_PERMANENTS_STATIC.md", "| Cast-time X doubling + X spell/ability copying | `u/UnboundFlourishing.java` |",
     "| Cast-time X doubling + X spell/ability copying | `u/UnboundFlourishing.java` | `Triggers.youCastSpell().withFilter(permanent X spell)` `DoubleXValueOfTriggeringSpellEffect` + `Triggers.youCastSpell().withFilter(CardAllOfPredicate(CardHasXInManaCostPredicate, CardAnyOfPredicate(INSTANT, SORCERY)))` `new CopyTriggeringSpellEffect()` + Triggers.youActivateAbility() `CopyControllerActivatedAbilityTriggerEffect(..., activationCostContainsX=true)` — doubles X for permanent spells and copies your X instant/sorcery spells and activated abilities, including mana abilities |"),
    ("CARD_PATTERNS_PERMANENTS_STATIC.md", "| Top-of-library instant/sorcery casting + copy | `m/MelekIzzetParagon.java` |",
     "| Top-of-library instant/sorcery casting + copy | `m/MelekIzzetParagon.java` | STATIC PlayWithTopCardRevealedEffect + AllowCastFromTopOfLibraryEffect(INSTANT, SORCERY) + `Triggers.youCastSpell().withFilter(instant/sorcery).castFrom(Zone.LIBRARY)` `new CopyTriggeringSpellEffect()` — the zone filter makes the mandatory copy fire only on library-top casts |"),
    ("CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md", "| Planeswalker with token, top-card play permission, attack-count damage, and two-copy emblem | `j/JayaFieryNegotiator.java` |",
     "| Planeswalker with token, top-card play permission, attack-count damage, and two-copy emblem | `j/JayaFieryNegotiator.java` | +1 `CreateTokenEffect` for a red Monk with `PROWESS`; −1 `ExileTopCardsChooseOneMayPlayThisTurnEffect(2)`; −2 `RegisterDelayedAttackDamageEffect()` with `TargetFilters.creatureAnOpponentControls()`; −8 `CreateEmblemEffect` carrying `new SpellCastTriggerEffect(redInstantOrSorcery, List.of(new CopyTriggeringSpellEffect().withCopies(2)))` |"),
    ("CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md", "| Once-per-turn spell copying + any-color mana + filtered next-turn impulse cast + X multi-target damage | `c/ChandraHopesBeacon.java` |",
     "| Once-per-turn spell copying + any-color mana + filtered next-turn impulse cast + X multi-target damage | `c/ChandraHopesBeacon.java` | `Triggers.youCastSpell().withFilter(INSTANT|SORCERY)`, `TriggerOptions.DEFAULT.withLimit(TriggerLimit.ONCE_EACH_TURN)`, `new CopyTriggeringSpellEffect()`; +2 `AwardAnyColorManaEffect(2, true)`; +1 `ExileTopCardsMayCastMatchingUntilNextTurnEffect(5, INSTANT|SORCERY)`; −X `DealDamageToEachTargetEffect(new XValue())` on the variable-loyalty multi-target constructor with min 0 / max 2 |"),
    ("PREDICATES_REFERENCE.md", "| `StackEntryIsNthSpellCastThisTurnPredicate` |", "FIRST_OF_KIND"),
]

FIRST_OF_KIND = (
    "| `StackEntryIsFirstSpellOfKindThisTurnPredicate` | `(List<CardPredicate> kinds)` | a spell that is the first spell "
    "of one of `kinds` its controller cast this turn (each kind judged on its own, with the predicate's source as "
    "\"this\"): \"if it's the first instant spell, the first sorcery spell, or the first Otter spell other than Alania "
    "you've cast this turn\" as a cast trigger's `withStackEntryFilter` (Alania, Divergent Storm). For one kind, use "
    "the cast trigger's `withFilter(kind).nthThisTurn(1)` |"
)


def main():
    for name, start, new in EDITS:
        path = DOCS / name
        lines = path.read_text().split("\n")
        matches = [i for i, line in enumerate(lines) if line.startswith(start) or (start.endswith("\n") and line + "\n" == start)]
        if new == "TAP_INDEX":
            tap = [i for i in matches if "CopyControllerCastSpellOnSpellCastEffect" in lines[i]]
            for i in tap:
                lines[i] = lines[i].replace(
                    "Used by Aziza, Mage Tower Captain via `CopyControllerCastSpellOnSpellCastEffect`",
                    "Used by Aziza, Mage Tower Captain around `CopyTriggeringSpellEffect`")
        elif new == "FIRST_OF_KIND":
            if not any(line.startswith("| `StackEntryIsFirstSpellOfKindThisTurnPredicate` |") for line in lines):
                if len(matches) != 1:
                    raise SystemExit(f"{name}: expected one line starting {start!r}, found {len(matches)}")
                lines.insert(matches[0], FIRST_OF_KIND)
        elif new is None:
            lines = [line for i, line in enumerate(lines) if i not in matches]
        else:
            if new in lines:
                continue
            if len(matches) != 1:
                raise SystemExit(f"{name}: expected one line starting {start!r}, found {len(matches)}")
            lines[matches[0]] = new
        path.write_text("\n".join(lines))
    left = [f"{path.name}:{n}" for path in DOCS.glob("*.md")
            for n, line in enumerate(path.read_text().split("\n"), 1)
            if path.name != "EFFECT_MERGE_PLAN.md" and any(old in line for old in REMOVED)]
    if left:
        raise SystemExit("still named: " + ", ".join(left))


if __name__ == "__main__":
    main()
