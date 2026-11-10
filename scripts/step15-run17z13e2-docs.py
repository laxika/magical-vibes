#!/usr/bin/env python3
"""Step 15, run 17z13e2: agent-docs follow the tap-for-mana and library defaults.

The tap-for-mana watches' triggers carry what the deleted class-keyed handlers judged ("you tap", "enchanted land",
"this land", "an opponent taps an artifact"), the fixed-color "add an additional {C}" effects are one
`AddManaWhenTappedForManaEffect`, the land-tap effects lost their trigger-condition fields, and Rainbow Vale's control
change is part of its mana ability. Rows are replaced by the text they start with (each prefix occurs once); a row
already rewritten is left alone. Fails when a doc still names a removed effect, field or form.

Idempotent. Usage: step15-run17z13e2-docs.py
"""
import pathlib
import re
import sys

DOCS = pathlib.Path(__file__).resolve().parent.parent / "agent-docs"

ROWS = {
    "EFFECTS_INDEX.md": [
        ("| `PerpetuallyGiveLandExtraManaEffect` |",
         "| `PerpetuallyGiveLandExtraManaEffect` | `(ManaColor color)` | on resolution, replace the first land card from "
         "the top of the controller's library with a runtime copy carrying `Triggers.youTapThisLandForMana()` + "
         "`AddManaWhenTappedForManaEffect(color)`. Used by Vigorous Farming (YBLB 19). |"),
        ("| `AddManaOfTypeProducedByTappedPermanentEffect` | `()` |",
         "| `AddManaOfTypeProducedByTappedPermanentEffect` | `()` | `Triggers.playerTapsPermanentForMana()` (Eloren "
         "Wilds), `youTapNonlandPermanentForMana()` (Kinnan), `youTapArtifactTokenForMana()` (Roxanne): the player who "
         "tapped the permanent adds one mana of a type it produced (the entry's produced mana colors; that player "
         "chooses among several); a triggered mana ability the tap-for-mana default resolves at once, after the "
         "permanent's own open any-color choice. `(ManaColor.COLORLESS)` adds {C} only for a tap that produced {C} "
         "(Forsaken Monument, on `youTapLandForMana()` and `youTapNonlandPermanentForMana()`) |"),
        ("`AddManaWhenLandOfSubtypeTappedForManaEffect` also accepts",
         "Which permanents count and who taps them is the trigger: `Triggers.youTapLandForMana()` (\"whenever you tap a "
         "land\", Crypt Ghast with `.withFilter(new PermanentHasSubtypePredicate(SWAMP))`), "
         "`playerTapsLandForMana().withFilter(...)` (\"whenever a Forest is tapped\", Vernal Bloom), "
         "`youTapThisLandForMana()`, `youTapCreatureForMana()`."),
        ("matching lands tapped by the source's controller (Crypt Ghast); the two-argument form remains", None),
        ("symmetric (Vernal Bloom).", None),
        ("| `AddManaWhenLandTappedForManaEffect` |",
         "| `AddManaWhenTappedForManaEffect` | `(ManaColor color)` | a tap-for-mana triggered mana ability's \"add an "
         "additional {C}\" / \"its controller adds an additional {C}\": the player who tapped the permanent (the entry's "
         "triggering player, CR 602.2) adds one mana of `color`, resolved at once by the tap-for-mana default "
         "(CR 605.4a). Groundchuck & Dirtbag (`youTapLandForMana()`), Crypt Ghast, Nissa, Nirkana Revenant "
         "(`youTapLandForMana().withFilter(subtype)`), Leyline of Abundance, Badgermole Cub (`youTapCreatureForMana()`); "
         "`ofItsController(color)` is \"its controller adds\": the tapped permanent's controller adds it (Vernal Bloom, "
         "Gauntlet of Might on `playerTapsLandForMana().withFilter(subtype)`; a player tapping another's land through "
         "Piracy is not its controller) |"),
        ("| `AddManaWhenCreatureTappedForManaEffect` |", None),
        ("| `AddManaOnEnchantedLandTapEffect` |",
         "| `AddManaOnEnchantedLandTapEffect` | `(ManaProducingEffect mana)` | on `Triggers.enchantedLandTappedForMana()`: "
         "the land's controller (as it is on the battlefield, else the player who tapped it) adds the mana described by "
         "`mana` (`AwardManaEffect`, `AwardAnyColorManaEffect`, `AwardManaOfColorsEffect`, `AwardChosenColorManaEffect`), "
         "its amount evaluated for that player and the Aura. Wild Growth, Fertile Ground; Runaway Growth's sequence "
         "with `IntensifySourceCardEffect` |"),
        ("| `AddManaForEachOtherLandWithSameNameEffect` | `()` | Triggers.playerTapsLandForMana() trigger, the source's "
         "controller tapping:",
         "| `AddManaForEachOtherLandWithSameNameEffect` | `()` | `Triggers.youTapLandForMana()` (\"whenever a land you "
         "control is tapped for mana\"): one mana of a type the land produced for every other land with the same name "
         "the tapping player controls, counted as it resolves (Sasaya's Essence); a triggered mana ability (CR 605.1b) "
         "the tap-for-mana default resolves at once (CR 605.4a) through the effect's handler, which reads the stack "
         "entry's produced mana colors (the land's basic-land-type colors as it last existed when the tap reported "
         "none) and adds to the entry's triggering player; while the land's own color choice is open the entry waits "
         "for the produced color |"),
        ("| `AddOneOfEachManaTypeProducedByLandEffect` imprinted-name form |",
         "| `AddOneOfEachManaTypeProducedByLandEffect` imprinted-name form | `()` on "
         "`Triggers.playerTapsLandForMana().withFilter(new PermanentSharesNameWithImprintedCardPredicate())` | \"whenever "
         "a land with the same name as the exiled card is tapped for mana\": the filter judges the land's name against "
         "the source's imprinted card still in exile. Used by Extraplanar Lens |"),
        ("| `AddExtraManaOfChosenColorOnLandTapEffect` |",
         "| `AddExtraManaOfChosenColorOnLandTapEffect` | `()` | a land-tap triggered mana ability: when the tapped land "
         "produced the source's chosen color (or, for a tap that reported none, its basic land types add it), the "
         "ability's controller adds one more of it. Caged Sun on `Triggers.youTapLandForMana()`; Gauntlet of Power's "
         "`ofItsController()` (the land's controller adds it) on `playerTapsLandForMana().withFilter(new "
         "PermanentHasSupertypePredicate(BASIC))` |"),
        ("| `AddOneOfEachManaTypeProducedByLandEffect` | `(boolean controllerOnly)`",
         "| `AddOneOfEachManaTypeProducedByLandEffect` | `()` or `ofAnyColor()` | a land-tap triggered mana ability: the "
         "tapping player adds one additional mana of a type that land produced, choosing one when it produced several; "
         "`ofAnyColor()` is \"one mana of any color\" (CR 106.1a, Regal Behemoth: its own five-color choice, never "
         "waiting for the land's). Who taps and which lands count are the trigger: `Triggers.youTapLandForMana()` "
         "(Vorinclex, Mirari's Wake), `playerTapsLandForMana()` (Mana Flare), its `withFilter` (Lavaleaper's basic, "
         "Winter's Night's snow, Keeper of Progenitus's Mountain, Forest or Plains); Regal Behemoth's \"while you're the "
         "monarch\" is the event condition `ControllerIsMonarch` |"),
        ("| `AddManaWhenLandOfSubtypeTappedForManaEffect` |", None),
        ("| `AddRestrictedManaWhenLandOfSubtypeTappedForManaEffect` |",
         "| `AddRestrictedManaWhenLandTappedForManaEffect` | `(ManaColor color, int amount, int snowAmount, "
         "ManaRestriction restriction)` | a land-tap triggered mana ability: the land's controller adds "
         "`amount` mana of `color` under `restriction`, `snowAmount` when the land is snow; \"may\" always accepted. "
         "Snowfall on `Triggers.playerTapsLandForMana().withFilter(new PermanentHasSubtypePredicate(ISLAND))` "
         "(BLUE, 1/2, `CumulativeUpkeepCosts`) |"),
        ("| `TapLandsThatCouldProduceSameManaAsTappedLandEffect` |",
         "| `TapLandsThatCouldProduceSameManaAsTappedLandEffect` | `()` | on `Triggers.opponentTapsLandForMana()`: the "
         "ability goes on the stack and, as it resolves, taps the lands \"that player\" (the entry's non-targeting "
         "target) controls whose possible mana types overlap those of the tapped land, as it is or as it last existed "
         "(the entry's triggering permanent). The forced taps do not produce mana. Used by Mana Web |"),
        ("| `AddManaWhenLandOfColorTappedForManaEffect` |",
         "| `AddManaWhenLandOfColorTappedForManaEffect` | `(ManaColor color)` | a land-tap triggered mana ability: when "
         "the tap produced `color` (or the land has a tap mana ability producing it) the player who tapped the land "
         "adds one more. Ultima, Origin of Oblivion on `Triggers.youTapLandForMana()` |"),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| \"Whenever an opponent taps an artifact for mana, gain control of that artifact",
         "| \"Whenever an opponent taps an artifact for mana, gain control of that artifact until the end of your next "
         "turn.\" | `Triggers.opponentTapsPermanentForMana().withFilter(new PermanentIsArtifactPredicate())` "
         "`GainControlOfTargetEffect.ofTriggeringPermanent(UNTIL_END_OF_YOUR_NEXT_TURN)` | trigger | Treasure Nabber "
         "(CMM 265). The collector judges the tapping player and the artifact; \"that artifact\" is the entry's "
         "triggering permanent, no target |"),
        ("| \"whenever you tap a land for mana, add an additional [color]\" |",
         "| \"whenever you tap a land for mana, add an additional [color]\" | `AddManaWhenTappedForManaEffect(ManaColor.X)` "
         "| `Triggers.youTapLandForMana()` | Groundchuck & Dirtbag (TMT) |"),
        ("| \"Whenever fortified land is tapped for mana, put a +1/+1 counter",
         "| \"Whenever fortified land is tapped for mana, put a +1/+1 counter on target creature you control. If that "
         "creature shares a color with the mana that land produced, create a Junk token.\" | "
         "`PutCounterOnTargetCreatureAndCreateTokenIfSharesManaColorEffect(CreateTokenEffect.ofArtifactToken(..., JUNK, "
         "...))` | `Triggers.enchantedLandTappedForMana()` | C.A.M.P. (PIP 129). The collector judges the attachment; "
         "the targeted ability waits for any-color mana's choice, then its target is chosen with the produced color |"),
        ("| \"whenever you tap a [land type] for mana, add an additional [color]\" |",
         "| \"whenever you tap a [land type] for mana, add an additional [color]\" | "
         "`AddManaWhenTappedForManaEffect(ManaColor.Y)` | `Triggers.youTapLandForMana().withFilter(new "
         "PermanentHasSubtypePredicate(CardSubtype.X))` | Crypt Ghast (SWAMP -> BLACK), Nissa, Nirkana Revenant |"),
        ("| \"whenever you tap a creature for mana, add an additional [color]\" |",
         "| \"whenever you tap a creature for mana, add an additional [color]\" | "
         "`AddManaWhenTappedForManaEffect(ManaColor.X)` | `Triggers.youTapCreatureForMana()` | Leyline of Abundance, "
         "Badgermole Cub (GREEN) |"),
        ("| \"Whenever you tap a permanent for {C}, add an additional {C}\" |",
         "| \"Whenever you tap a permanent for {C}, add an additional {C}\" | "
         "`AddManaOfTypeProducedByTappedPermanentEffect(ManaColor.COLORLESS)` in `Triggers.youTapLandForMana()` and "
         "`Triggers.youTapNonlandPermanentForMana()` | mana-tap triggers | Forsaken Monument; adds one {C} only when "
         "the tapped permanent produced colorless mana |"),
        ("| \"whenever a [land type] is tapped for mana, its controller adds an additional [color]\" |",
         "| \"whenever a [land type] is tapped for mana, its controller adds an additional [color]\" | "
         "`AddManaWhenTappedForManaEffect.ofItsController(ManaColor.Y)` | `Triggers.playerTapsLandForMana().withFilter(new "
         "PermanentHasSubtypePredicate(CardSubtype.X))` | Vernal Bloom (FOREST → GREEN), Gauntlet of Might. The land's "
         "controller adds it, also when another player tapped it through Piracy |"),
        ("| \"whenever an Island is tapped for mana, its controller may add an additional {U}.",
         "| \"whenever an Island is tapped for mana, its controller may add an additional {U}. If that Island is snow, "
         "… {U}{U} instead. Spend this mana only to pay cumulative upkeep costs.\" | "
         "`AddRestrictedManaWhenLandTappedForManaEffect(BLUE, 1, 2, CumulativeUpkeepCosts())` | "
         "`Triggers.playerTapsLandForMana().withFilter(new PermanentHasSubtypePredicate(ISLAND))` | Snowfall. Snow "
         "lands use `snowAmount`; \"may\" auto-accepted. Routes to `cumulativeUpkeepOnly*` buckets |"),
        ("| \"whenever a player taps a land for mana, that player adds one mana of any type that land produced\" | "
         "`AddOneOfEachManaTypeProducedByLandEffect(false)` |",
         "| \"whenever a player taps a land for mana, that player adds one mana of any type that land produced\" | "
         "`AddOneOfEachManaTypeProducedByLandEffect()` | `Triggers.playerTapsLandForMana()` | Mana Flare — symmetric "
         "mana doubler. \"Whenever you tap a land for mana\" (Vorinclex) is `Triggers.youTapLandForMana()` |"),
        ("| \"Whenever you tap a land for mana while you're the monarch",
         "| \"Whenever you tap a land for mana while you're the monarch, add an additional one mana of any color\" | "
         "`addTrigger(Triggers.enters(ObjectRelation.THIS), new BecomeMonarchEffect())` + "
         "`addTrigger(Triggers.youTapLandForMana(), TriggerOptions.DEFAULT.withEventCondition(new ControllerIsMonarch()), "
         "AddOneOfEachManaTypeProducedByLandEffect.ofAnyColor())` | trigger | Regal Behemoth (CMM 316). \"Any color\" "
         "is a choice of the five colors (CR 106.1a), never colorless, so it never waits for the land's color choice |"),
        ("| \"whenever you tap this land for mana, target opponent creates",
         "| \"whenever you tap this land for mana, target opponent creates a 1/1 colorless Spirit creature token\" | "
         "`CreateTokenForTargetPlayerEffect(CreateTokenEffect(\"Spirit\", 1, 1, null, [SPIRIT], {}, {}), "
         "PlayerRelation.OPPONENT)` | `Triggers.youTapThisLandForMana()` | Forbidden Orchard. The targeted ability is no "
         "mana ability; its target opponent is chosen as it goes on the stack, the choice carrying the tap |"),
        ("| \"whenever a player taps a land for mana, if it's not that player's turn, destroy that land\" |",
         "| \"whenever a player taps a land for mana, if it's not that player's turn, destroy that land\" | "
         "`DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING)` | `Triggers.playerTapsLandForMana()`, "
         "`TriggerOptions.DEFAULT.withInterveningIf(new NotCondition(new TargetPlayerTurn()))` | Price of Glory. \"That "
         "player\" is the entry's non-targeting target, the tapping player; the intervening \"if\" is checked as it "
         "triggers and as it resolves (CR 603.4) |"),
        ("| \"{T}: Add one mana of any color. An opponent gains control of this land at the beginning of the next end "
         "step.\" |",
         "| \"{T}: Add one mana of any color. An opponent gains control of this land at the beginning of the next end "
         "step.\" | `new ActivatedAbility(true, null, List.of(new AwardAnyColorManaEffect(), new "
         "RegisterDelayedChooseOpponentGainsControlOfSourceEffect()), ...)` | mana ability | Rainbow Vale: the control "
         "change is part of the mana ability (no trigger), its delayed trigger controlled by the player who activated it |"),
        ("| \"whenever a land you control is tapped for mana, add an additional one mana of any type",
         "| \"whenever a land you control is tapped for mana, add an additional one mana of any type that land produced "
         "for each other land you control with the same name as it\" | `AddManaForEachOtherLandWithSameNameEffect()` | "
         "`Triggers.youTapLandForMana()` | Sasaya's Essence: the lands are counted as the triggered mana ability "
         "resolves, by the tapped land's name as it is or as it last existed (a land sacrificed for its mana still counts "
         "its name), each mana of the type the land produced (any combination when it produced several) |"),
        ("| \"whenever a land with the same name as the exiled card is tapped for mana",
         "| \"whenever a land with the same name as the exiled card is tapped for mana, its controller adds one mana of "
         "any type that land produced\" | `AddOneOfEachManaTypeProducedByLandEffect()` | "
         "`Triggers.playerTapsLandForMana().withFilter(new PermanentSharesNameWithImprintedCardPredicate())` | Extraplanar "
         "Lens |"),
        ("| \"Whenever you choose to put one or more cards on the bottom of your library while scrying",
         "| \"Whenever you choose to put one or more cards on the bottom of your library while scrying, exile that many "
         "cards from the bottom of your library\" | `Triggers.youScry()`, "
         "`TriggerOptions.DEFAULT.withEventCondition(new EventValueAtLeast(1))` + `new ExileBottomCardsToSourceEffect(new "
         "EventValue())` | trigger | The Temporal Anchor; the scry's bottomed count is the event's value, read by the "
         "event condition and by \"that many\" |"),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `PerpetuallyGiveLandExtraManaEffect(ManaColor)` -",
         "- `PerpetuallyGiveLandExtraManaEffect(ManaColor)` - on resolution, replace the first land card from the top of "
         "the controller's library with a runtime copy carrying `Triggers.youTapThisLandForMana()` + "
         "`AddManaWhenTappedForManaEffect(color)`"),
        ("- `AddManaForEachOtherLandWithSameNameEffect()` —",
         "- `AddManaForEachOtherLandWithSameNameEffect()` — `Triggers.youTapLandForMana()`; adds one mana of a type the "
         "land produced for every other land with the same name the tapping player controls (Sasaya's Essence)"),
        ("- `TapLandsThatCouldProduceSameManaAsTappedLandEffect()` —",
         "- `TapLandsThatCouldProduceSameManaAsTappedLandEffect()` — `Triggers.opponentTapsLandForMana()`; taps that "
         "player's lands that could produce any mana type the tapped land (as it is or last existed) could (Mana Web)"),
        ("- `AddRestrictedManaWhenLandOfSubtypeTappedForManaEffect(",
         "- `AddRestrictedManaWhenLandTappedForManaEffect(color, amount, snowAmount, restriction)` — "
         "`Triggers.playerTapsLandForMana().withFilter(subtype)`; extra restricted mana for the tapping player, "
         "`snowAmount` for a snow land (Snowfall)"),
        ("- `PutCounterOnTargetCreatureAndCreateTokenIfSharesManaColorEffect(CreateTokenEffect)` -",
         "- `PutCounterOnTargetCreatureAndCreateTokenIfSharesManaColorEffect(CreateTokenEffect)` - "
         "`Triggers.enchantedLandTappedForMana()`; target creature you control gets a +1/+1 counter, and a token is "
         "created if it shares a color with the mana the land produced (C.A.M.P.)"),
        ("- `AddManaWhenLandTappedForManaEffect(",
         "- `AddManaWhenTappedForManaEffect(color)` / `.ofItsController(color)` — the ability's controller (\"add\") or "
         "the tapped permanent's controller (\"its controller adds\") adds one mana of `color`; the "
         "trigger says who and which permanents (`youTapLandForMana()` Groundchuck & Dirtbag, "
         "`youTapLandForMana().withFilter(Swamp)` Crypt Ghast, `playerTapsLandForMana().withFilter(Forest)` + "
         "`ofItsController` Vernal Bloom, "
         "`youTapCreatureForMana()` Leyline of Abundance)"),
        ("- `AddManaWhenLandOfSubtypeTappedForManaEffect(", None),
        ("- `AddManaWhenCreatureTappedForManaEffect(", None),
        ("- `AddManaWhenLandOfColorTappedForManaEffect(",
         "- `AddManaWhenLandOfColorTappedForManaEffect(ManaColor)` — `Triggers.youTapLandForMana()`; adds one mana when "
         "the tapped land produced (or could produce) the color (Ultima, Origin of Oblivion)"),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("| Opponent artifact mana tap steals the artifact |",
         "| Opponent artifact mana tap steals the artifact | `t/TreasureNabber.java` | "
         "`Triggers.opponentTapsPermanentForMana().withFilter(new PermanentIsArtifactPredicate())` "
         "`GainControlOfTargetEffect.ofTriggeringPermanent(UNTIL_END_OF_YOUR_NEXT_TURN)` — \"that artifact\" is the "
         "entry's triggering permanent |"),
        ("| Controller land tap adds fixed mana |",
         "| Controller land tap adds fixed mana | `g/GroundchuckDirtbag.java` | `Triggers.youTapLandForMana()` "
         "`AddManaWhenTappedForManaEffect(GREEN)` |"),
        ("| Land tap triggered mana ability with damage |",
         "| Land tap triggered mana ability with damage | `o/Overabundance.java` | `Triggers.playerTapsLandForMana()` "
         "`SequenceEffect.of(AddOneOfEachManaTypeProducedByLandEffect(), DealDamageToPlayersEffect(1, "
         "DamageRecipient.TRIGGERING_PLAYER))` |"),
        ("| Opponent land tap taps all their lands |",
         "| Opponent land tap taps all their lands | `w/WarsToll.java` | `Triggers.opponentTapsLandForMana()` "
         "`TapPermanentsEffect(ALL_PERMANENTS, PermanentAllOf(land, PermanentControlledByTriggeringPlayerPredicate))` "
         "— no target: \"that player\" is the triggering player |"),
        ("| Land tap mana doubling + opponent untap lock |",
         "| Land tap mana doubling + opponent untap lock | `v/VorinclexVoiceOfHunger.java` | "
         "`Triggers.youTapLandForMana()` `AddOneOfEachManaTypeProducedByLandEffect()` + "
         "`Triggers.opponentTapsLandForMana()` `SkipNextUntapEffect(TapUntapScope.TRIGGERING)` |"),
    ],
    "CARD_PATTERNS_PERMANENTS_STATIC.md": [
        ("| Symmetric land-tap mana doubling |",
         "| Symmetric land-tap mana doubling | `m/ManaFlare.java` | `Triggers.playerTapsLandForMana()` "
         "`AddOneOfEachManaTypeProducedByLandEffect()` — every player's land tap adds one extra mana of a type it "
         "produced |"),
        ("| Monarch-gated any-color land-tap mana |",
         "| Monarch-gated any-color land-tap mana | `r/RegalBehemoth.java` | `Triggers.youTapLandForMana()`, "
         "`TriggerOptions.DEFAULT.withEventCondition(new ControllerIsMonarch())`, "
         "`AddOneOfEachManaTypeProducedByLandEffect.ofAnyColor()` — a five-color choice, never waiting for the land's |"),
        ("| Controller's creature-tap mana boost |",
         "| Controller's creature-tap mana boost | `l/LeylineOfAbundance.java` | `Triggers.youTapCreatureForMana()` "
         "`AddManaWhenTappedForManaEffect(ManaColor.GREEN)` |"),
        ("| Deferred opponent land-tap color lock |",
         "| Deferred opponent land-tap color lock | `m/ManaWeb.java` | `Triggers.opponentTapsLandForMana()` "
         "`TapLandsThatCouldProduceSameManaAsTappedLandEffect()` — on the stack, then taps that player's lands that "
         "could produce the tapped land's mana types |"),
        ("| Island-tap CU-restricted mana + snow bonus |",
         "| Island-tap CU-restricted mana + snow bonus | `s/Snowfall.java` | `Triggers.upkeep(ActivePlayer.YOU)` "
         "`CumulativeUpkeepEffect(\"{U}\")` + `Triggers.playerTapsLandForMana().withFilter(Island)` "
         "`AddRestrictedManaWhenLandTappedForManaEffect(BLUE, 1, 2, CumulativeUpkeepCosts())` |"),
        ("| Enchanted land mana |",
         "| Enchanted land mana | `o/Overgrowth.java`, `f/FertileGround.java` | `Triggers.enchantedLandTappedForMana()` "
         "`AddManaOnEnchantedLandTapEffect` + `AwardManaEffect` / `AwardAnyColorManaEffect` |"),
    ],
    "CARD_PATTERNS_PERMANENTS_ARTIFACTS.md": [
        ("| Imprint + matching land-tap mana bonus |",
         "| Imprint + matching land-tap mana bonus | `e/ExtraplanarLens.java` | `Triggers.enters(ObjectRelation.THIS)` "
         "`MayEffect(ExileTargetPermanentAndImprintEffect)` on `TargetFilters.landYouControl()` + "
         "`Triggers.playerTapsLandForMana().withFilter(new PermanentSharesNameWithImprintedCardPredicate())` "
         "`AddOneOfEachManaTypeProducedByLandEffect()` |"),
        ("| Choose color + static boost + mana bonus |",
         "| Choose color + static boost + mana bonus | `c/CagedSun.java` | STATIC ChooseColorOnEnterEffect + STATIC "
         "BoostCreaturesOfChosenColorEffect(1,1) + `Triggers.youTapLandForMana()` "
         "`AddExtraManaOfChosenColorOnLandTapEffect()` |"),
        ("| Choose color + static boost + symmetric basic-land mana bonus |",
         "| Choose color + static boost + symmetric basic-land mana bonus | `g/GauntletOfPower.java` | STATIC "
         "ChooseColorOnEnterEffect + STATIC StaticBoostEffect + `Triggers.playerTapsLandForMana().withFilter(new "
         "PermanentHasSupertypePredicate(BASIC))` `AddExtraManaOfChosenColorOnLandTapEffect.ofItsController()` (\"its "
         "controller adds\") |"),
    ],
    "CARD_PATTERNS_LANDS_SPELLS.md": [
        ("| Any-color land that gives an opponent control at the next end step |",
         "| Any-color land that gives an opponent control at the next end step | `r/RainbowVale.java` | one mana "
         "ability `AwardAnyColorManaEffect` + `RegisterDelayedChooseOpponentGainsControlOfSourceEffect` (part of the "
         "mana ability, not a trigger) |"),
        ("| Any-color land that gives the opponent a token on every tap |",
         "| Any-color land that gives the opponent a token on every tap | `f/ForbiddenOrchard.java` | `{T}` "
         "`AwardAnyColorManaEffect` ability + `Triggers.youTapThisLandForMana()` `CreateTokenForTargetPlayerEffect("
         "CreateTokenEffect(...), PlayerRelation.OPPONENT)` |"),
    ],
    "EFFECT_MERGE_PLAN.md": [
        ("| `AddManaWhenLandOfSubtypeTappedForManaEffect(+ ManaRestriction)` |",
         "| `AddManaWhenLandOfSubtypeTappedForManaEffect(+ ManaRestriction)` | **Done differently** (step 15, run "
         "17z13e2): the subtype is the trigger's filter, the fixed-color form is `AddManaWhenTappedForManaEffect`, the "
         "restricted one `AddRestrictedManaWhenLandTappedForManaEffect` | — |"),
    ],
}

PREDICATE_ROW = ("| `PermanentSharesNameWithImprintedCardPredicate` | `()` | permanents with the same name as the card imprinted "
                 "on the source while it is still in exile (Extraplanar Lens's \"a land with the same name as the "
                 "exiled card\") | `gameData` + `sourceCardId` |")

GUIDE_ROWS = [
    ("| `Triggers.opponentTapsLandForMana()` |",
     "| `Triggers.opponentTapsLandForMana()` | An opponent taps a land (Sanctimony, Vorinclex's untap lock, War's Toll, "
     "Mana Web); read by the `playerTapsLandForMana()` collector, which judges the tapping player |\n"
     "| `Triggers.youTapLandForMana()` | You tap a land / a land you control is tapped (Crypt Ghast, Savage Firecat, "
     "Sasaya's Essence, Vorinclex); `.withFilter` narrows the land |\n"
     "| `Triggers.enchantedLandTappedForMana()` | The land the source is attached to is tapped (Wild Growth, Fertile "
     "Ground, C.A.M.P.) |\n"
     "| `Triggers.youTapThisLandForMana()` | The source land is tapped for mana (Forbidden Orchard) |\n"
     "| `Triggers.opponentTapsPermanentForMana()` | An opponent taps a permanent for mana; `.withFilter` narrows it "
     "(Treasure Nabber's artifact); read by the `playerTapsPermanentForMana()` collector |"),
]

FORBIDDEN = re.compile(r"AddManaWhenLandOfSubtypeTappedForManaEffect\(|AddManaWhenLandTappedForManaEffect|"
                       r"AddManaWhenCreatureTappedForManaEffect|AddRestrictedManaWhenLandOfSubtypeTappedForManaEffect|"
                       r"AddOneOfEachManaTypeProducedByLandEffect\((?:true|false)|anyColor\(true|"
                       r"AddExtraManaOfChosenColorOnLandTapEffect\(false")


def rewrite(path, rows):
    lines = path.read_text().split("\n")
    out = []
    used = set()
    for line in lines:
        for i, (prefix, replacement) in enumerate(rows):
            if line.startswith(prefix):
                if i in used:
                    raise SystemExit(f"{path.name}: two rows start with {prefix!r}")
                used.add(i)
                if replacement is not None:
                    out.append(replacement)
                break
        else:
            out.append(line)
    path.write_text("\n".join(out))


# Substring rewrites in rows written elsewhere: the effect's trigger-condition fields are the trigger's now.
SUBS = [
    ("GrantStaticEffectToPlayerUntilEndOfTurnEffect(AddOneOfEachManaTypeProducedByLandEffect(true))",
     "GrantStaticEffectToPlayerUntilEndOfTurnEffect(AddOneOfEachManaTypeProducedByLandEffect())"),
    ("AddOneOfEachManaTypeProducedByLandEffect(false)", "AddOneOfEachManaTypeProducedByLandEffect()"),
]


def main():
    for name, rows in ROWS.items():
        rewrite(DOCS / name, rows)
    for path in sorted(DOCS.glob("*.md")):
        text = path.read_text()
        new = text
        for old, replacement in SUBS:
            new = new.replace(old, replacement)
        if new != text:
            path.write_text(new)
    predicates = DOCS / "PREDICATES_REFERENCE.md"
    text = predicates.read_text()
    anchor = "| `PermanentHasSameNameAsSourcePredicate` |"
    if "PermanentSharesNameWithImprintedCardPredicate" not in text:
        lines = text.split("\n")
        at = next(i for i, line in enumerate(lines) if line.startswith(anchor))
        lines.insert(at + 1, PREDICATE_ROW)
        predicates.write_text("\n".join(lines))
    guide = DOCS / "ACTIVATED_ABILITY_GUIDE.md"
    text = guide.read_text()
    if "`Triggers.youTapLandForMana()`" not in text:
        rewrite(guide, GUIDE_ROWS)
    bad = []
    for path in sorted(DOCS.glob("*.md")):
        for n, line in enumerate(path.read_text().split("\n"), 1):
            if FORBIDDEN.search(line) and "**Gone**" not in line and "Done differently" not in line:
                bad.append(f"{path.name}:{n}: {line[:160]}")
    if bad:
        print("\n".join(bad))
        sys.exit(1)
    print("agent-docs updated")


if __name__ == "__main__":
    main()
