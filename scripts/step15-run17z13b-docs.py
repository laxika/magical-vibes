#!/usr/bin/env python3
"""Step 15, run 17z13b: agent-docs follow the emblem conversion.

The emblem markers whose triggered ability the engine built from their class are gone, and so are the step wrapper
`EmblemStepTriggerEffect` / `EmblemTriggerStep`: an emblem's triggered ability is a `TriggeredAbility` of the emblem.
Teferi, Who Slows the Sunset's draw is the new static `DrawDuringEachOpponentsDrawStepEffect`. Each doc line naming a
removed form is replaced whole (a line is found by a unique prefix); a "Gone" line keeps the removed name for search.
Fails if a doc or skill still names a removed form outside a "Gone" line. Idempotent.

Usage: step15-run17z13b-docs.py
"""
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"
SKILLS = ROOT / ".claude" / "skills"

REMOVED = ["ReturnCardPutIntoGraveyardToHandEffect", "EmblemArtifactGraveyardReturnTriggerEffect",
           "ExileTargetOpponentPermanentOnDrawEffect", "DrawOnControlledCreatureEntersEffect",
           "EmblemCreatureDeathTriggerEffect", "EmblemStepTriggerEffect", "EmblemTriggerStep", "CollectsEmblemTrigger",
           "EmblemTriggerMatchContext", "opponentControlledOnly",
           "RegisterDelayedReturnDyingArtifactUnderControlEffect"]

RUN = "effect-slot-refactor.md step 15, run 17z13b"

STEP_FORM = ("an emblem's \"at the beginning of [step or phase]\" ability is a `TriggeredAbility` of the emblem with a "
             "`StepTrigger`: `Triggers.upkeep(ActivePlayer.YOU)` / `upkeep(ActivePlayer.OPPONENT)`, "
             "`drawStep(ActivePlayer.YOU)`, `precombatMainPhase(ActivePlayer.YOU)`, "
             "`beginningOfCombat(ActivePlayer.YOU)`, `endStep(ActivePlayer.YOU)` / `endStep(ActivePlayer.ANY)` "
             "(\"each end step\"), and \"at the end of the first combat phase on your turn\" `Triggers.endOfCombat()` "
             "with `TriggerOptions.DEFAULT.withEventCondition(new AllOf(List.of(new ControllerTurn(), "
             "new FirstCombatPhase())))` (Zariel, Archduke of Avernus). `StepTriggerService.collectEmblemStepTriggers` "
             "reads it from the command zone (CR 114.4), the player relation judged against the emblem's controller, "
             "its event condition and intervening \"if\" as it triggers; a graveyard or permanent target is chosen as "
             "it goes on the stack, any other ability records the player whose step it is. "
             "`new CreateEmblemEffect(new TriggeredAbility(Triggers.endStep(ActivePlayer.YOU), new CreateTokenEffect(3, "
             "\"Cat\", 1, 1, WHITE, List.of(CAT), Set.of(LIFELINK), Set.of())), \"…\")` (Ajani, Adversary of Tyrants); "
             "with a recipient, activated abilities or other static effects, the five-component form: "
             "`new CreateEmblemEffect(List.of(), \"…\", EmblemRecipient.EACH_PLAYER_DEALT_DAMAGE_THIS_WAY, List.of(), "
             "List.of(new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU), new DealDamageToPlayersEffect(3, "
             "DamageRecipient.CONTROLLER))))` (Chandra, Roaring Flame)")
DRAW_STATIC = ("\"You draw a card during each opponent's draw step\" is no triggered ability (no player gets priority "
               "between the active player's draw and yours, the card's ruling): the emblem's static "
               "`DrawDuringEachOpponentsDrawStepEffect(int amount)`, read by `StepTriggerService.handleDrawStep` right "
               "after the draw step's draw, in APNAP order, with nothing on the stack; a skipped draw step draws "
               "nothing (Teferi, Who Slows the Sunset −7)")
DEATH_FORM = ("an emblem's \"whenever a creature dies\" ability is a `TriggeredAbility` of the emblem on "
              "`Triggers.creatureDies(ObjectRelation.ANY)`, a targeted one naming its target filter: "
              "`new CreateEmblemEffect(new TriggeredAbility(Triggers.creatureDies(ObjectRelation.ANY), drain)"
              ".withTargetFilter(opponentFilter), \"…\")` (Sephiroth, One-Winged Angel), read by the emblem "
              "creature-death reader (`TriggerCollectionService.checkCreatureDeathRegisteredTriggers`), the creature "
              "that died recorded as the entry's triggering card and permanent, a target chosen as it goes on the "
              "stack")
GRAVEYARD_FROM_BATTLEFIELD_FORM = (
    "an emblem's \"whenever [a permanent] is put into your graveyard from the battlefield\" ability is a "
    "`TriggeredAbility` of the emblem on `Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, filter, "
    "ObjectRelation.YOU_OWN)`: Daretti, Scrap Savant's `new CreateEmblemEffect(new TriggeredAbility("
    "Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, Triggers.ARTIFACT, ObjectRelation.YOU_OWN), "
    "new RegisterDelayedReturnDyingCreatureUnderControlEffect(false, null, 0, null, null, false, true, false)), \"…\")`, "
    "read by `TriggerCollectionService.checkEmblemPutIntoGraveyardFromBattlefieldTriggers` (the filter judged on the "
    "permanent as it last existed, CR 603.10a; a token triggers it too, CR 111.7), \"that card\" the entry's "
    "triggering card")
GRAVEYARD_FROM_ANYWHERE_FORM = (
    "an emblem's \"whenever a card is put into your graveyard from anywhere\" ability is a `TriggeredAbility` of the "
    "emblem on `Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN, ObjectRelation.YOU_OWN)` (a token isn't a "
    "card, CR 108.2b): Tamiyo, the Moon Sage's `new CreateEmblemEffect(new TriggeredAbility("
    "Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN, ObjectRelation.YOU_OWN), new MayEffect("
    "new ReturnTriggeringCardToOwnerHandEffect(), \"Return that card to your hand?\")), \"…\")`, read by "
    "`GraveyardService` as the card arrives, the card the entry's triggering card for that stay (CR 400.7)")
DRAW_FORM = ("an emblem's targeted \"whenever you draw a card\" ability names its target filter: Teferi, Hero of "
             "Dominaria's `new CreateEmblemEffect(new TriggeredAbility(Triggers.youDrawCard(), "
             "new ExileTargetPermanentEffect()).withTargetFilter(TargetFilters.permanentAnOpponentControls()), \"…\")`, "
             "read by `DrawService`, its target chosen through `PermanentChoiceContext.EmblemTriggerTarget` with the "
             "filter, which the stack entry keeps for the check as it resolves (CR 608.2b)")
ENTERS_FORM = ("Huatli, Radiant Champion's emblem is `new CreateEmblemEffect(new TriggeredAbility(Triggers.enters("
               "Triggers.CREATURE, ObjectRelation.YOU_CONTROL), new MayEffect(new DrawCardEffect(1), \"Draw a card?\")), "
               "\"…\")`, read by the emblem enters reader (`TriggerCollectionService.checkEmblemEntersTriggers`); the "
               "\"you may\" is asked as the ability resolves (CR 603.5)")

INDEX_GONE = [
    f"| `EmblemStepTriggerEffect` / `EmblemTriggerStep` | — | **Gone** ({RUN}): {STEP_FORM} |",
    f"| `DrawDuringEachOpponentsDrawStepEffect` | `(int amount)` | {DRAW_STATIC} |",
    f"| `EmblemCreatureDeathTriggerEffect` | — | **Gone** ({RUN}): {DEATH_FORM} |",
]

EDITS = {
    "EFFECTS_INDEX.md": [
        ("| `RegisterDelayedReturnDyingArtifactUnderControlEffect` | `()` |",
         f"| `RegisterDelayedReturnDyingArtifactUnderControlEffect` | — | **Gone** ({RUN}, no card wrote it): "
         f"{GRAVEYARD_FROM_BATTLEFIELD_FORM} |"),
        ("| `EmblemTriggerStep.OPPONENT_DRAW_STEP` | enum value |", None),
        ("| `EmblemTriggerStep.EACH_END_STEP` | enum value |", None),
        ("| `EmblemStepTriggerEffect` | `(EmblemTriggerStep step,", "\n".join(INDEX_GONE)),
        ("| `EmblemCreatureDeathTriggerEffect` | `(List<CardEffect> effects", None),
        ("| `EmblemArtifactGraveyardReturnTriggerEffect` | `()` |",
         f"| `EmblemArtifactGraveyardReturnTriggerEffect` | — | **Gone** ({RUN}): {GRAVEYARD_FROM_BATTLEFIELD_FORM} |"),
        ("| `ReturnCardPutIntoGraveyardToHandEffect` | `()` |",
         f"| `ReturnCardPutIntoGraveyardToHandEffect` | — | **Gone** ({RUN}): {GRAVEYARD_FROM_ANYWHERE_FORM} |"),
        ("| `ExileTargetOpponentPermanentOnDrawEffect` | `()` |",
         f"| `ExileTargetOpponentPermanentOnDrawEffect` | — | **Gone** ({RUN}): {DRAW_FORM} |\n"
         f"| `DrawOnControlledCreatureEntersEffect` | — | **Gone** ({RUN}): {ENTERS_FORM} |"),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `RegisterDelayedReturnDyingArtifactUnderControlEffect()` — emblem marker",
         f"- `RegisterDelayedReturnDyingArtifactUnderControlEffect` — **Gone** ({RUN}, no card wrote it): "
         f"{GRAVEYARD_FROM_BATTLEFIELD_FORM}"),
        ("- `EmblemStepTriggerEffect(EmblemTriggerStep.OPPONENT_DRAW_STEP, effects, reminderText)`",
         f"- `DrawDuringEachOpponentsDrawStepEffect(int amount)` — {DRAW_STATIC}"),
        ("- `EmblemStepTriggerEffect(EmblemTriggerStep.EACH_END_STEP, effects, reminderText)`", None),
        ("- `EmblemStepTriggerEffect(EmblemTriggerStep step, List<CardEffect> effects, String reminderText)` — "
         "marker payload for an emblem reading \"At the beginning of your upkeep/draw step/end step, …\" or",
         f"- `EmblemStepTriggerEffect` / `EmblemTriggerStep` — **Gone** ({RUN}): {STEP_FORM}"),
        ("- `EmblemCreatureDeathTriggerEffect(List<CardEffect> effects, TargetFilter targetFilter)`",
         f"- `EmblemCreatureDeathTriggerEffect` — **Gone** ({RUN}): {DEATH_FORM}"),
        ("- `EmblemArtifactGraveyardReturnTriggerEffect()`",
         f"- `EmblemArtifactGraveyardReturnTriggerEffect` — **Gone** ({RUN}): {GRAVEYARD_FROM_BATTLEFIELD_FORM}\n"
         f"- `ReturnCardPutIntoGraveyardToHandEffect` — **Gone** ({RUN}): {GRAVEYARD_FROM_ANYWHERE_FORM}\n"
         f"- `ExileTargetOpponentPermanentOnDrawEffect` — **Gone** ({RUN}): {DRAW_FORM}\n"
         f"- `DrawOnControlledCreatureEntersEffect` — **Gone** ({RUN}): {ENTERS_FORM}"),
        ("- `EmblemStepTriggerEffect(EmblemTriggerStep step, List<CardEffect> effects, String reminderText)` — "
         "marker payload for an emblem reading \"At the beginning of your upkeep/draw step/end step, …\". ", None),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| \"you get an emblem with \\\"whenever an artifact is put into your graveyard from the battlefield, "
         "return that card to the battlefield at the beginning of the next end step\\\"\" | "
         "`CreateEmblemEffect(List.of(new RegisterDelayedReturnDyingArtifactUnderControlEffect())", None),
        ("| \"If your life total is greater than 10, it becomes 10. For the rest of the game, your maximum life total "
         "is 10. You get an emblem with",
         "| \"If your life total is greater than 10, it becomes 10. For the rest of the game, your maximum life total "
         "is 10. You get an emblem with \\\"Pay 2 life: Add one mana of any color\\\" and \\\"At the beginning of your "
         "upkeep, you draw a card and you lose 1 life.\\\"\" | `SPELL ConditionalEffect(ControllerLifeAtLeast(11), "
         "SetLifeTotalEffect(10))` + `SPELL SetMaximumLifeTotalEffect(10)` + `SPELL new CreateEmblemEffect(List.of(), "
         "\"…\", EmblemRecipient.CONTROLLER, List.of(activated ability PayLifeCost(2) + AwardAnyColorManaEffect()), "
         "List.of(new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU), SequenceEffect.of(new DrawCardEffect(1), "
         "new LoseLifeEffect(1)))))` | SPELL | You Compleat Me (MB2 320/556). |"),
        ("| \"You get an emblem with \\\"At the beginning of each opponent's upkeep, that player sacrifices a "
         "creature of their choice.\\\"\" |",
         "| \"You get an emblem with \\\"At the beginning of each opponent's upkeep, that player sacrifices a creature "
         "of their choice.\\\"\" | `new CreateEmblemEffect(new TriggeredAbility(Triggers.upkeep(ActivePlayer.OPPONENT), "
         "new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), SacrificeRecipient.ACTIVE_PLAYER)), "
         "\"…\")` | loyalty ability | Sorin, Solemn Visitor −6. The emblem step reader records the active opponent "
         "as \"that player\", the non-targeting effect recipient |"),
        ("| \"you get an emblem with \\\"whenever a creature dies, target opponent loses 1 life and you gain 1 "
         "life\\\"\" |",
         "| \"you get an emblem with \\\"whenever a creature dies, target opponent loses 1 life and you gain 1 "
         "life\\\"\" | `new CreateEmblemEffect(new TriggeredAbility(Triggers.creatureDies(ObjectRelation.ANY), drain)"
         ".withTargetFilter(opponentFilter), \"Whenever a creature dies, target opponent loses 1 life and you gain 1 "
         "life.\")` wrapped in `AsTransformsIntoThisEffect` as a static ability of the back face (\"as this creature "
         "transforms into …\") | STATIC | Sephiroth, One-Winged Angel. The emblem exists as the permanent "
         "transforms, without using the stack; the emblem creature-death reader chooses the ability's target with "
         "its own filter as it goes on the stack |"),
        ("| \"you get an emblem with \\\"whenever an artifact is put into your graveyard from the battlefield, "
         "return that card to the battlefield at the beginning of the next end step\\\"\" | "
         "`CreateEmblemEffect(List.of(new EmblemArtifactGraveyardReturnTriggerEffect())",
         "| \"you get an emblem with \\\"whenever an artifact is put into your graveyard from the battlefield, return "
         "that card to the battlefield at the beginning of the next end step\\\"\" | `new CreateEmblemEffect(new "
         "TriggeredAbility(Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, Triggers.ARTIFACT, ObjectRelation.YOU_OWN), "
         "new RegisterDelayedReturnDyingCreatureUnderControlEffect(false, null, 0, null, null, false, true, false)), "
         "\"…\")` | loyalty ability | Daretti, Scrap Savant −10. Read by "
         "`TriggerCollectionService.checkEmblemPutIntoGraveyardFromBattlefieldTriggers`; an artifact token triggers it "
         "too and returns nothing |\n"
         "| \"you get an emblem with \\\"whenever a card is put into your graveyard from anywhere, you may return it to "
         "your hand\\\"\" | `new CreateEmblemEffect(new TriggeredAbility(Triggers.putIntoGraveyardFromAnywhere("
         "Triggers.NONTOKEN, ObjectRelation.YOU_OWN), new MayEffect(new ReturnTriggeringCardToOwnerHandEffect(), "
         "\"Return that card to your hand?\")), \"…\")` | loyalty ability | Tamiyo, the Moon Sage −8 (with "
         "`GrantNoMaximumHandSizeEffect(REST_OF_GAME)` for the emblem's other ability). A token isn't a card |\n"
         "| \"you get an emblem with \\\"whenever you draw a card, exile target permanent an opponent controls\\\"\" | "
         "`new CreateEmblemEffect(new TriggeredAbility(Triggers.youDrawCard(), new ExileTargetPermanentEffect())"
         ".withTargetFilter(TargetFilters.permanentAnOpponentControls()), \"…\")` | loyalty ability | Teferi, Hero of "
         "Dominaria −8 |\n"
         "| \"you get an emblem with \\\"whenever a creature you control enters, you may draw a card\\\"\" | "
         "`new CreateEmblemEffect(new TriggeredAbility(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL), "
         "new MayEffect(new DrawCardEffect(1), \"Draw a card?\")), \"…\")` | loyalty ability | Huatli, Radiant "
         "Champion −8 |\n"
         "| \"you get an emblem with \\\"... you draw a card during each opponent's draw step\\\"\" | "
         "`DrawDuringEachOpponentsDrawStepEffect(1)` in the emblem's static effects (no triggered ability) | "
         "loyalty ability | Teferi, Who Slows the Sunset −7, beside "
         "`UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect(TurnStep.UNTAP)` |"),
        ("| \"You get an emblem with \\\"At the beginning of your end step, create three 1/1 white Cat creature "
         "tokens with lifelink.\\\"\" |",
         "| \"You get an emblem with \\\"At the beginning of your end step, create three 1/1 white Cat creature tokens "
         "with lifelink.\\\"\" | `new CreateEmblemEffect(new TriggeredAbility(Triggers.endStep(ActivePlayer.YOU), "
         "new CreateTokenEffect(3, \"Cat\", 1, 1, WHITE, List.of(CAT), Set.of(LIFELINK), Set.of())), \"…\")` | "
         "loyalty ability | Ajani, Adversary of Tyrants −7. Every emblem step ability is a `TriggeredAbility` with a "
         "`StepTrigger` (`upkeep`, `drawStep`, `precombatMainPhase`, `beginningOfCombat`, `endStep`, with "
         "`ActivePlayer.YOU`, `OPPONENT` or `ANY`), read by `StepTriggerService.collectEmblemStepTriggers` |"),
    ],
    "TRIGGER_SLOT_TARGETING.md": [
        ("| Emblem (`EmblemTriggerTarget`)      |",
         "| Emblem (`EmblemTriggerTarget`)      | `Options.UPKEEP`   | ❌            | ✅ any permanent  | ❌ | ✅ the "
         "ability's own (`TriggeredAbility.withTargetFilter`, kept on the stack entry; Teferi, Hero of Dominaria) | "
         "✅ the ability's own | ❌ |"),
    ],
    "CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md": [
        ("| Planeswalker with until-next-turn mass pump, token, and opponent-upkeep sacrifice emblem |",
         "| Planeswalker with until-next-turn mass pump, token, and opponent-upkeep sacrifice emblem | "
         "`s/SorinSolemnVisitor.java` | +1 `BoostAllOwnCreaturesEffect(1, 0, GrantDuration.UNTIL_YOUR_NEXT_TURN)` + "
         "`GrantKeywordEffect(LIFELINK, OWN_CREATURES, UNTIL_YOUR_NEXT_TURN)`; -2 `CreateTokenEffect(\"Vampire\", 2, 2, "
         "BLACK, VAMPIRE, FLYING)`; -6 `CreateEmblemEffect(new TriggeredAbility(Triggers.upkeep(ActivePlayer.OPPONENT), "
         "SacrificePermanentsEffect(1, PermanentIsCreaturePredicate, ACTIVE_PLAYER)), …)`. The emblem step reader puts "
         "the non-targeting ability on the stack with the active opponent as the effect recipient |"),
        ("| Planeswalker with draw + delayed untap + tuck + emblem | `t/TeferiHeroOfDominaria.java` |",
         "| Planeswalker with draw + delayed untap + tuck + emblem | `t/TeferiHeroOfDominaria.java` | +1 DrawCardEffect + "
         "RegisterDelayedUntapPermanentsEffect(2), -3 PutTargetPermanentIntoLibraryNFromTopEffect(2) with nonland "
         "filter, -8 `CreateEmblemEffect(new TriggeredAbility(Triggers.youDrawCard(), new ExileTargetPermanentEffect())"
         ".withTargetFilter(TargetFilters.permanentAnOpponentControls()), \"…\")` — the emblem draw reader chooses the "
         "target with the ability's filter on each draw |"),
        ("| Planeswalker with creature-count loyalty, creature-count pump, and persistent creature-entry draw emblem |",
         "| Planeswalker with creature-count loyalty, creature-count pump, and persistent creature-entry draw emblem | "
         "`h/HuatliRadiantChampion.java` | +1 `PutCountersOnSelfEffect(LOYALTY, PermanentCount(IsCreature, "
         "CONTROLLER))`; −1 `BoostTargetCreatureEffect(PermanentCount(IsCreature, CONTROLLER), same)` with "
         "`TargetFilters.creature()`; −8 `CreateEmblemEffect(new TriggeredAbility(Triggers.enters(Triggers.CREATURE, "
         "ObjectRelation.YOU_CONTROL), new MayEffect(new DrawCardEffect(1), \"Draw a card?\")), \"…\")` |"),
        ("| Planeswalker with tap-lock + tapped-creature draw + graveyard-recursion emblem |",
         "| Planeswalker with tap-lock + tapped-creature draw + graveyard-recursion emblem | `t/TamiyoTheMoonSage.java` "
         "| +1 `TapPermanentsEffect(TARGET)` + `SkipNextUntapEffect(TARGET)` with **no** target filter (any "
         "permanent). -2 `DrawCardEffect(PermanentCount(AllOf(IsCreature, IsTapped), TARGET_PLAYER))` — the "
         "TARGET_PLAYER scope is what makes `DrawCardEffect.targetSpec()` advertise a player target, so no card-level "
         "filter is needed for \"target player\". -8 `GrantNoMaximumHandSizeEffect(REST_OF_GAME)` + "
         "`CreateEmblemEffect(new TriggeredAbility(Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN, "
         "ObjectRelation.YOU_OWN), new MayEffect(new ReturnTriggeringCardToOwnerHandEffect(), \"…\")), \"…\")` (the "
         "emblem carries only the graveyard-return ability) |"),
        ("| Pinging creature transforming into a planeswalker on cumulative damage, with a damage-emblem back face |",
         "| Pinging creature transforming into a planeswalker on cumulative damage, with a damage-emblem back face | "
         "`c/ChandraFireOfKaladesh.java` / `c/ChandraRoaringFlame.java` | Front: `Triggers.youCastSpell()` "
         "`.withFilter(CardColorPredicate(RED))` on the cast trigger with `UntapPermanentsEffect(SELF)` (mandatory, "
         "unlike Cinder Pyromancer's `MayEffect`); `{T}` `[DealDamageToTargetPlayerOrPlaneswalkerEffect(1), "
         "ConditionalEffect(SelfDealtDamageThisTurn(3), ExileSelfAndReturnTransformedEffect())]` — the ping resolves "
         "first so it counts toward the three. Back: +1 `DealDamageToTargetPlayerOrPlaneswalkerEffect(2)`; −2 "
         "`DealDamageToTargetCreatureEffect(2)`; −7 `DealDamageToPlayersEffect(6, EACH_OPPONENT)` + "
         "`CreateEmblemEffect(List.of(), …, EmblemRecipient.EACH_PLAYER_DEALT_DAMAGE_THIS_WAY, List.of(), "
         "List.of(new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU), DealDamageToPlayersEffect(3, CONTROLLER))))` "
         "— read by the emblem step reader (see also Ajani, Adversary of Tyrants for `Triggers.endStep(ActivePlayer.YOU)`) |"),
        ("| Transforming creature with a death-trigger emblem |",
         "| Transforming creature with a death-trigger emblem | `s/SephirothFabledSoldier.java` / "
         "`s/SephirothOneWingedAngel.java` | Back face uses `addStaticEffect(new AsTransformsIntoThisEffect(new "
         "CreateEmblemEffect(new TriggeredAbility(Triggers.creatureDies(ObjectRelation.ANY), payload)"
         ".withTargetFilter(opponentFilter), \"Whenever a creature dies, …\")))` (\"as this transforms into\", not a "
         "trigger); the emblem creature-death reader chooses the target with the ability's own filter |"),
        ("| Artifact graveyard return emblem | `d/DarettiScrapSavant.java` |",
         "| Artifact graveyard return emblem | `d/DarettiScrapSavant.java` | −10 `CreateEmblemEffect(new TriggeredAbility("
         "Triggers.putIntoGraveyardFrom(Zone.BATTLEFIELD, Triggers.ARTIFACT, ObjectRelation.YOU_OWN), new "
         "RegisterDelayedReturnDyingCreatureUnderControlEffect(false, null, 0, null, null, false, true, false)), \"…\")`; "
         "the emblem's put-into-your-graveyard-from-the-battlefield reader records \"that card\" and the Lifeline-form "
         "delayed return brings it back under its owner's control |"),
    ],
}

FRAGMENTS = {
    "EFFECTS_INDEX.md": [
        ("`TriggerCollectionService` (with the emblem attack reader for \"whenever a creature attacks you\") / "
         "`DrawService` / `GraveyardService` for the trigger markers, ",
         ""),
        ("(Ajani, Sleeper Agent: `PlayerPredicateTargetFilter(OPPONENT)`) — `new CreateEmblemEffect(new "
         "TriggeredAbility(Triggers.youCastSpell().withFilter(spellPredicate), effects…)[.withTargetFilter(filter)], "
         "reminderText)`. ",
         "(Ajani, Sleeper Agent: `PlayerPredicateTargetFilter(OPPONENT)`) — `new CreateEmblemEffect(new "
         "TriggeredAbility(Triggers.youCastSpell().withFilter(spellPredicate), effects…)[.withTargetFilter(filter)], "
         "reminderText)`. Every emblem triggered ability is a `TriggeredAbility` of the emblem, never a marker in "
         "`staticEffects`: the emblem readers read cast, enters, tap, attack, draw, life-gain, combat-damage, "
         "creature-death, \"put into your graveyard\" (from the battlefield, from anywhere) and step abilities. "),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("Emblems are never removed\n- \"Whenever a creature attacks enchanted player",
         "An emblem's triggered ability is a `TriggeredAbility` of the emblem (`CreateEmblemEffect(TriggeredAbility, "
         "reminderText)`, or the five-component form's `triggeredAbilities`), never a marker in `staticEffects`. "
         "Emblems are never removed\n- \"Whenever a creature attacks enchanted player"),
    ],
}


def apply_edits(text, edits, name):
    lines = text.split("\n")
    for prefix, replacement in edits:
        matches = [i for i, line in enumerate(lines) if line.startswith(prefix)]
        if replacement is not None and replacement.split("\n")[0] in lines:
            continue
        if not matches:
            if replacement is None:
                continue
            raise SystemExit(f"{name}: no line starts with {prefix[:80]!r}")
        if len(matches) > 1:
            raise SystemExit(f"{name}: {len(matches)} lines start with {prefix[:80]!r}")
        index = matches[0]
        if replacement is None:
            del lines[index]
        else:
            lines[index:index + 1] = replacement.split("\n")
    return "\n".join(lines)


def main():
    for name, edits in EDITS.items():
        path = DOCS / name
        text = path.read_text()
        new = apply_edits(text, edits, name)
        for old, frag in FRAGMENTS.get(name, []):
            if old in new and (not frag or frag not in new):
                new = new.replace(old, frag, 1)
        if new != text:
            path.write_text(new)
            print(f"changed {name}")
    stale = []
    for path in list(DOCS.glob("*.md")) + list(SKILLS.rglob("*.md")):
        for number, line in enumerate(path.read_text().split("\n"), 1):
            if "**Gone**" in line or line.startswith("- Gone") or line.startswith("| Gone"):
                continue
            for removed in REMOVED:
                if re.search(r"\b" + removed + r"\b", line):
                    stale.append(f"{path.relative_to(ROOT)}:{number}: {removed}")
    if stale:
        raise SystemExit("still named:\n" + "\n".join(stale))


if __name__ == "__main__":
    sys.exit(main())
