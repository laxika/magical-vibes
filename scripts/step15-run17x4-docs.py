#!/usr/bin/env python3
"""Step 15, run 17x4: agent-docs edits for the life, emblem, step and vote families.

Exact line edits (a line containing the key fragment is replaced whole) in EFFECTS_QUICK_REFERENCE.md,
EFFECTS_INDEX.md, ORACLE_TEXT_EFFECT_MAP.md, the CARD_PATTERN* files and ACTIVATED_ABILITY_GUIDE.md, fragment edits
in those files, and a sentence in ARCHITECTURE.md's triggering-card paragraph. Fails if a doc or skill still names a removed form. Idempotent.

Usage: step15-run17x4-docs.py
"""
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DOCS = ROOT / "agent-docs"
GONE = "(effect-slot-refactor.md step 15, run 17x4)"

LINES = {
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `ErestorOfTheCouncilEffect(VotingResult)`",
         "- `ErestorOfTheCouncilEffect()` - on `Triggers.playersFinishVoting()`: reads the vote from the stack entry's "
         "triggering vote (`StackEntry.getTriggeringVotingResult()`, set by the voting collector); creates one Treasure "
         "for each opponent sharing one of the controller's choices, scries for opponents with a different choice, then "
         "draws a card. Used by Erestor of the Council (LTC 53)."),
        ("- `ModelOfUnityEffect(VotingResult)`",
         "- `ModelOfUnityEffect()` - on `Triggers.playersFinishVoting()`: reads the vote from the stack entry's "
         "triggering vote; the controller and each opponent sharing one of the controller's choices may each scry 2. "
         "Used by Model of Unity (LTC 78)."),
        ("- `DealDamageToAnyTargetOnControllerSpellCastEffect(damage)`",
         "- An emblem's \"whenever you cast a [quality] spell, this emblem deals N damage to any target [and …]\" is "
         "`new CreateEmblemEffect(new TriggeredAbility(Triggers.youCastSpell()[.withFilter(spellPredicate)], new "
         "DealDamageToAnyTargetEffect(N)[, more effects…]), reminderText)` (Chandra, Torch of Defiance; Narset of the "
         "Ancient Way; Ral, Izzet Viceroy). `DealDamageToAnyTargetOnControllerSpellCastEffect` is gone " + GONE + "."),
        ("- `EachTargetPlayerDrawsCardsEqualToAttachedCountEffect(PermanentPredicate)`",
         "- `EachTargetPlayerDrawsCardsEqualToAttachedCountEffect(PermanentPredicate)` — each target player draws cards "
         "equal to the number of matching permanents attached to \"that player\", the stack entry's triggering player "
         "(the enchanted player whose upkeep it is), counted as it resolves; use `PermanentHasSubtypePredicate(CURSE)` "
         "for Curse-counting upkeep abilities"),
        ("- `EmblemControllerLosesLifeOnAnyPlayerDrawEffect(int amount)`",
         "- An emblem's \"whenever [a player / you / an opponent] draws a card, …\" is a `TriggeredAbility` of the emblem "
         "with `Triggers.playerDrawsCard()` / `youDrawCard()` / `opponentDrawsCard()` (no filter, count or options), "
         "read by `DrawService` from the command zone, the drawing player the entry's triggering player: Ob Nixilis "
         "Reignited's `new CreateEmblemEffect(List.of(), text, EmblemRecipient.TARGET_PLAYER, List.of(), List.of(new "
         "TriggeredAbility(Triggers.playerDrawsCard(), new LoseLifeEffect(2, LoseLifeRecipient.CONTROLLER))))`. "
         "`EmblemControllerLosesLifeOnAnyPlayerDrawEffect` is gone " + GONE + "."),
        ("- `GivePoisonCountersEffect(int, PoisonRecipient)` — give poison;",
         "- `GivePoisonCountersEffect(int, PoisonRecipient)` — give poison; recipient routes CONTROLLER (self) / "
         "TARGET_PLAYER / EACH_PLAYER / EACH_OPPONENT / TARGET_PERMANENT_CONTROLLER / TRIGGERING_PERMANENT_CONTROLLER. "
         "`TARGET_PERMANENT_CONTROLLER` reads the controller of a non-targeting permanent baked onto a trigger (Noxious "
         "Assault); `TRIGGERING_PERMANENT_CONTROLLER` the controller of the entry's triggering permanent as it resolves "
         "(Relic Putrescence's \"whenever enchanted artifact becomes tapped, its controller gets a poison counter\"). On "
         "an `Triggers.dealsCombatDamageToPlayer()` trigger, `TARGET_PLAYER` binds to the damaged player (Pit Scorpion: "
         "\"whenever this deals damage to a player, that player gets a poison counter\")"),
        ("- `NykthosParagonLifeGainEffect()`",
         "- \"Whenever you gain life, you may put that many +1/+1 counters on each creature you control. Do this only "
         "once each turn.\" is `new MayEffect(new PutCounterOnEachControlledPermanentEffect(PLUS_ONE_PLUS_ONE, new "
         "EventValue(), new PermanentIsCreaturePredicate()), prompt)` with `TriggerOptions.DEFAULT.withLimit("
         "TriggerLimit.DO_ONLY_ONCE_EACH_TURN)` on `Triggers.youGainLife()` (Nykthos Paragon): the ability goes on the "
         "stack with the life gained as its event value and asks as it resolves. `NykthosParagonLifeGainEffect` is "
         "gone " + GONE + "."),
        ("- `ConjureDuplicateOfEnlistedNontokenCreatureEffect()`",
         "- `ConjureDuplicateOfEnlistedNontokenCreatureEffect()` — trigger-condition record for \"whenever this enlists a "
         "nontoken creature\" (on `Triggers.attackingCreatureYouControlAbilityTriggers()`): the collector puts its "
         "`triggeredEffect()`, a `MayEffect(ConjureDuplicateOfEnlistedCreatureIntoTopFiveEffect())`, on the stack with "
         "the enlisted creature as the entry's triggering permanent (Goblin Morale Sergeant)"),
        ("- `ConjureDuplicateOfEnlistedCreatureIntoTopFiveEffect(Card creatureCard)`",
         "- `ConjureDuplicateOfEnlistedCreatureIntoTopFiveEffect()` — conjures a duplicate of the entry's triggering "
         "permanent (where it is, else as it last existed) with haste into a random insertion position among the top "
         "five cards of the controller's library and stores a perpetual +1/+0 modifier (Goblin Morale Sergeant)"),
    ],
    "EFFECTS_INDEX.md": [
        ("| `ConjureDuplicateOfEnlistedNontokenCreatureEffect` |",
         "| `ConjureDuplicateOfEnlistedNontokenCreatureEffect` | `()` | trigger-condition record for \"whenever this "
         "enlists a nontoken creature\"; the collector puts its `triggeredEffect()` (an optional duplicate conjure) on "
         "the stack with the enlisted creature as the entry's triggering permanent | Goblin Morale Sergeant (YDMU 14) |"),
        ("| `ConjureDuplicateOfEnlistedCreatureIntoTopFiveEffect` |",
         "| `ConjureDuplicateOfEnlistedCreatureIntoTopFiveEffect` | `()` | conjures a fresh haste duplicate of the entry's "
         "triggering permanent (where it is, else as it last existed) into a random position among the top five cards "
         "of the controller's library and stores a perpetual +1/+0 modifier keyed to the duplicate card | Goblin Morale "
         "Sergeant (YDMU 14) |"),
        ("| `ErestorOfTheCouncilEffect` |",
         "| `ErestorOfTheCouncilEffect` | `()` | On `Triggers.playersFinishVoting()`; reads the vote from the stack "
         "entry's triggering vote (`StackEntry.getTriggeringVotingResult()`). On resolution, each opponent who chose one "
         "of the controller's choices creates a Treasure, each opponent with a different choice contributes one to "
         "scry, and the controller draws a card. Used by Erestor of the Council |"),
        ("| `ModelOfUnityEffect` |",
         "| `ModelOfUnityEffect` | `()` | On `Triggers.playersFinishVoting()`; reads the vote from the stack entry's "
         "triggering vote. On resolution, the controller and each opponent who chose one of the controller's choices "
         "are offered an independent may-scry-2 choice in APNAP order. Used by Model of Unity |"),
        ("| `DrawCardsEqualToLifeGainedEffect` |",
         "| `DrawCardsEqualToLifeGainedEffect` | (gone) | **Gone** " + GONE + ": \"whenever you gain life, draw that "
         "many cards\" is `DrawCardEffect(new EventValue())` on `Triggers.youGainLife()` (the life gained is the "
         "entry's event value) |"),
        ("| `MillOpponentOnLifeLossEffect` |",
         "| `MillOpponentOnLifeLossEffect` | (gone) | **Gone** " + GONE + ": \"whenever an opponent / a player loses "
         "life, that player mills that many cards\" is `MillEffect(new EventValue(), MillRecipient.TRIGGERING_PLAYER)` "
         "on `Triggers.opponentLosesLife()` (and `Triggers.youLoseLife()` in one ability for \"a player\"): the player "
         "who lost life is the entry's non-targeting \"that player\", the life lost its event value |"),
        ("| `EachTargetPlayerDrawsCardsEqualToAttachedCountEffect` |",
         "| `EachTargetPlayerDrawsCardsEqualToAttachedCountEffect` | `(PermanentPredicate filter)` | each target player "
         "draws cards equal to the number of matching permanents attached to \"that player\", the stack entry's "
         "triggering player (the enchanted player whose upkeep it is); the count is evaluated at resolution. Used by "
         "Curse of Surveillance |"),
        ("| `DealDamageOnSpellLifeGainEffect` |",
         "| `DealDamageOnSpellLifeGainEffect` | `(int damage, CardColor triggeringColor)` | trigger-condition record on "
         "`Triggers.youGainLife()`: when an instant or sorcery spell of the color causes the controller to gain life, "
         "the collector queues its `triggeredEffect()` (`DealDamageToAnyTargetEffect(damage)`) through "
         "`LifeGainTriggerAnyTarget`. Used by Firesong and Sunspeaker |"),
        ("| `CradleOfVitalityLifeGainEffect` |",
         "| `CradleOfVitalityLifeGainEffect` | (gone) | **Gone** " + GONE + ": \"whenever you gain life, you may pay "
         "{cost}. If you do, put a +1/+1 counter on target creature for each 1 life you gained\" is `MayPayManaEffect("
         "cost, PutCounterOnTargetPermanentEffect.withTargetRestriction(PLUS_ONE_PLUS_ONE, new EventValue(), new "
         "PermanentIsCreaturePredicate()), prompt)` on `Triggers.youGainLife()`: the target is chosen as it triggers "
         "(a \"you may pay\" whose effect targets no player offers creatures), the life gained is the event value |"),
        ("| `NykthosParagonLifeGainEffect` |",
         "| `NykthosParagonLifeGainEffect` | (gone) | **Gone** " + GONE + ": `MayEffect(PutCounterOnEachControlled"
         "PermanentEffect(PLUS_ONE_PLUS_ONE, new EventValue(), new PermanentIsCreaturePredicate()), prompt)` with "
         "`TriggerLimit.DO_ONLY_ONCE_EACH_TURN` on `Triggers.youGainLife()` (Nykthos Paragon) |"),
        ("| `GivePoisonCountersEffect` |",
         "| `GivePoisonCountersEffect` | `(int amount, PoisonRecipient recipient)`, `(DynamicAmount, recipient)` or "
         "`(int amount, PoisonRecipient recipient, CardPredicate spellFilter)` | give N poison counters, routed by "
         "`PoisonRecipient` {CONTROLLER (self-poison, e.g. Phyrexian Vatmother upkeep), TARGET_PLAYER (targets a "
         "player; with `spellFilter` doubles as an `Triggers.youCastSpell()` trigger descriptor firing when the "
         "controller casts a matching spell, resolving into a `spellFilter == null` copy — Hand of the Praetors), "
         "EACH_PLAYER (every player incl. controller, e.g. Ichor Rats ETB), EACH_OPPONENT, TARGET_PERMANENT_CONTROLLER, "
         "TRIGGERING_PERMANENT_CONTROLLER (the controller of the entry's triggering permanent as it resolves: Relic "
         "Putrescence's tapped artifact, Reaper of Sheoldred's damage source)}. All placement routes through the "
         "central `LifeSupport.applyPoisonCounters` path (respects `PlayerCantGetPoisonCountersEffect` suppression) |"),
        ("| `EmblemControllerLosesLifeOnAnyPlayerDrawEffect` |",
         "| `EmblemControllerLosesLifeOnAnyPlayerDrawEffect` | (gone) | **Gone** " + GONE + ": an emblem's \"whenever a "
         "player draws a card, you lose N life\" is a `TriggeredAbility(Triggers.playerDrawsCard(), new LoseLifeEffect("
         "N, LoseLifeRecipient.CONTROLLER))` of the emblem, read by `DrawService` (Ob Nixilis Reignited) |"),
    ],
    "ORACLE_TEXT_EFFECT_MAP.md": [
        ("| \"Whenever Goblin Morale Sergeant enlists a nontoken creature,",
         "| \"Whenever Goblin Morale Sergeant enlists a nontoken creature, you may conjure a duplicate of that creature "
         "into the top five cards of your library at random. The duplicate perpetually gets +1/+0 and gains haste.\" | "
         "`ConjureDuplicateOfEnlistedNontokenCreatureEffect()` (its `triggeredEffect()`: `MayEffect("
         "ConjureDuplicateOfEnlistedCreatureIntoTopFiveEffect())`) | enlist trigger | Goblin Morale Sergeant (YDMU 14); "
         "the enlisted creature is the entry's triggering permanent, read as the ability resolves (else as it last "
         "existed), and the duplicate is inserted at a random position among the top five while its perpetual +1/+0 is "
         "stored by card identity. |"),
        ("| \"Whenever players finish voting, each opponent who voted",
         "| \"Whenever players finish voting, each opponent who voted for a choice you voted for creates a Treasure "
         "token. You scry X, where X is the number of opponents who voted for a choice you didn't vote for. Draw a "
         "card.\" | `ErestorOfTheCouncilEffect()` on `Triggers.playersFinishVoting()` | Triggers.playersFinishVoting() | "
         "Erestor of the Council (LTC 53). The completed vote is the entry's triggering vote (`VotingResult`); an "
         "opponent may contribute to both Treasure and scry counts when their votes include both shared and different "
         "choices |"),
        ("| \"Whenever players finish voting, you and each opponent",
         "| \"Whenever players finish voting, you and each opponent who voted for a choice you voted for may scry 2.\" | "
         "`ModelOfUnityEffect()` on `Triggers.playersFinishVoting()` | Triggers.playersFinishVoting() | Model of Unity "
         "(LTC 78). The completed vote is the entry's triggering vote (`VotingResult`); the controller is always "
         "eligible and matching opponents are offered independent may-scry choices in APNAP order |"),
        ("| \"At the beginning of enchanted player's upkeep, any number of target players other than that player",
         "| \"At the beginning of enchanted player's upkeep, any number of target players other than that player each "
         "draw cards equal to the number of Curses attached to that player\" | `EachTargetPlayerDrawsCardsEqualToAttached"
         "CountEffect(PermanentHasSubtypePredicate(CURSE))` | Triggers.upkeep(ActivePlayer.ENCHANTED_PLAYER) | Curse of "
         "Surveillance; the upkeep target pipeline excludes the enchanted player, supports choosing zero or more "
         "players and gives the entry the enchanted player as its triggering player (\"that player\") |"),
        ("| \"whenever you gain life, you may pay {cost}. If you do, put a +1/+1 counter on target creature",
         "| \"whenever you gain life, you may pay {cost}. If you do, put a +1/+1 counter on target creature for each 1 "
         "life you gained\" | `MayPayManaEffect(\"{cost}\", PutCounterOnTargetPermanentEffect.withTargetRestriction("
         "PLUS_ONE_PLUS_ONE, new EventValue(), new PermanentIsCreaturePredicate()), prompt)` | Triggers.youGainLife() | "
         "Cradle of Vitality. Creature target chosen when the trigger goes on the stack (no player targets; skipped if "
         "no creature is a legal target); the life gained is the entry's event value; the \"you may pay\" is asked as "
         "it resolves |"),
        ("| \"Whenever you gain life, you may put that many +1/+1 counters on each creature you control.",
         "| \"Whenever you gain life, you may put that many +1/+1 counters on each creature you control. Do this only "
         "once each turn.\" | `MayEffect(PutCounterOnEachControlledPermanentEffect(PLUS_ONE_PLUS_ONE, new EventValue(), "
         "new PermanentIsCreaturePredicate()), prompt)` | Triggers.youGainLife(), `TriggerOptions.DEFAULT.withLimit("
         "TriggerLimit.DO_ONLY_ONCE_EACH_TURN)` | Nykthos Paragon. The ability goes on the stack with the life gained as "
         "its event value, asks as it resolves, and consumes the source's once-per-turn marker only when accepted |"),
        ("| \"Whenever a creature card is put into an opponent's graveyard from anywhere, you may pay life equal to its",
         "| \"Whenever a creature card is put into an opponent's graveyard from anywhere, you may pay life equal to its "
         "mana value. If you do, put it onto the battlefield under your control. It's a Warlock in addition to its "
         "other types. If a Warlock you control would die, exile it instead\" | `MayPayLifeEqualToAmountEffect(new "
         "TriggeringCardManaValue(), ReturnTriggeringCardFromGraveyardToBattlefieldEffect(false, true, null, 0, "
         "WARLOCK), prompt)` + `ExileOwnCreaturesOfSubtypeInsteadOfDyingEffect(WARLOCK)` | `Triggers."
         "putIntoGraveyardFromAnywhere(Triggers.NONTOKEN_CREATURE, ObjectRelation.OPPONENT_OWNS)` + `STATIC` | Lorcan, "
         "Warlock Collector. The collector gives the entry the card with its graveyard incarnation as its triggering "
         "card; the replacement is checked in `PermanentRemovalService` |"),
    ],
    "CARD_PATTERNS_CREATURES_TRIGGERED.md": [
        ("| Creature card into opponent's graveyard → pay mana value to return as a Warlock |",
         "| Creature card into opponent's graveyard → pay mana value to return as a Warlock | "
         "`l/LorcanWarlockCollector.java` | Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN_CREATURE, "
         "ObjectRelation.OPPONENT_OWNS) `MayPayLifeEqualToAmountEffect(new TriggeringCardManaValue(), "
         "ReturnTriggeringCardFromGraveyardToBattlefieldEffect with WARLOCK, prompt)` — the collector records the card as "
         "the entry's triggering card; the static subtype replacement exiles Warlocks you control instead of letting "
         "them die |"),
        ("| Player loses life — mill |",
         "| Player loses life — mill | `m/Mindcrank.java`, `t/TheMasterOfLakeTown.java` | `Triggers.opponentLosesLife()` "
         "and, for wording that includes the controller, one ability with `Triggers.youLoseLife()` too "
         "(`addTrigger(List.of(Triggers.opponentLosesLife(), Triggers.youLoseLife()), …)`), using `MillEffect(new "
         "EventValue(), MillRecipient.TRIGGERING_PLAYER)` — that player mills that many cards. Trigger fires from damage "
         "(spell + combat) and life-loss effects |"),
    ],
    "CARD_PATTERN_INDEX.md": [
        ("| post-vote trigger that compares the controller's choices",
         "| post-vote trigger that compares the controller's choices with each opponent's choices | "
         "`e/ErestorOfTheCouncil.java` + `ErestorOfTheCouncilEffect` (reads the entry's triggering `VotingResult`) |"),
        ("| post-vote trigger that offers the controller and matching voters",
         "| post-vote trigger that offers the controller and matching voters a may-scry ability | `m/ModelOfUnity.java` "
         "+ `ModelOfUnityEffect` (reads the entry's triggering `VotingResult`) |"),
    ],
    "CARD_PATTERNS_PERMANENTS_STATIC.md": [
        ("| Can't lose + life gain draw + life loss exile + LTB lose |",
         "| Can't lose + life gain draw + life loss exile + LTB lose | `l/LichsMastery.java` | STATIC CantLoseGameEffect + "
         "GrantKeywordEffect(HEXPROOF, SELF), Triggers.youGainLife() DrawCardEffect(new EventValue()), "
         "Triggers.youLoseLife() ExileForEachLifeLostEffect, Triggers.leavesBattlefield(THIS) "
         "ControllerLosesGameOnLeavesEffect |"),
        ("| Curse (enchant player + upkeep draws for any number of other players) |",
         "| Curse (enchant player + upkeep draws for any number of other players) | `c/CurseOfSurveillance.java` | "
         "Triggers.upkeep(ActivePlayer.ENCHANTED_PLAYER) EachTargetPlayerDrawsCardsEqualToAttachedCountEffect(new "
         "PermanentHasSubtypePredicate(CardSubtype.CURSE)) — the controller chooses zero or more target players other "
         "than the enchanted player; each target draws for the current Curse count attached to the enchanted player, "
         "the entry's triggering player |"),
    ],
    "CARD_PATTERNS_ABILITIES_WALKERS_SAGAS.md": [
        ("| Planeswalker with draw/life loss, creature removal, and opponent draw-punisher emblem |",
         "| Planeswalker with draw/life loss, creature removal, and opponent draw-punisher emblem | "
         "`o/ObNixilisReignited.java` | +1 `DrawCardEffect(1)` + `LoseLifeEffect(1, CONTROLLER)`; −3 "
         "`DestroyTargetPermanentEffect` with `TargetFilters.creature()`; −8 `CreateEmblemEffect(List.of(), …, "
         "TARGET_PLAYER, List.of(), List.of(new TriggeredAbility(Triggers.playerDrawsCard(), new LoseLifeEffect(2, "
         "CONTROLLER))))` with an opponent player filter |"),
        ("| Planeswalker with resolution-time impulse cast-or-burn, mana, creature damage, and spell-cast damage emblem |",
         "| Planeswalker with resolution-time impulse cast-or-burn, mana, creature damage, and spell-cast damage emblem | "
         "`c/ChandraTorchOfDefiance.java` | +1 `ExileTopCardMayCastOrDealDamageEffect(2)`; the engine offers the exiled "
         "nonland card for a normal-cost cast during the ability's resolution and otherwise deals 2 to each opponent. "
         "Second +1 `AwardManaEffect(RED, 2)`, −3 `DealDamageToTargetCreatureEffect(4)`, −7 `CreateEmblemEffect(new "
         "TriggeredAbility(Triggers.youCastSpell(), new DealDamageToAnyTargetEffect(5)), \"…\")` |"),
    ],
}

FRAGMENTS = {
    "ARCHITECTURE.md": [
        ("which the scheme collector sets to the scheme's set-in-motion object as it was.",
         "which the scheme collector sets to the scheme's set-in-motion object as it was. The votes a \"whenever "
         "players finish voting\" ability reads are the entry's triggering vote (`StackEntry.triggeringVotingResult`, "
         "the `VotingResult` as the players finished it); \"that player\" of an enchanted player's upkeep whose "
         "targets are chosen as it triggers (Curse of Surveillance) is the entry's triggering player, and of a "
         "non-targeting one (Curse of Oblivion) its `targetId`; an emblem's draw ability is read by `DrawService` "
         "from `Emblem.triggeredAbilities`, the drawing player its triggering player."),
    ],
    "ACTIVATED_ABILITY_GUIDE.md": [
        ("| `Triggers.opponentDrawsCard()` | Whenever an opponent draws a card (all draws",
         "| `Triggers.playerDrawsCard()` | Whenever a player draws a card. Read only from an emblem's "
         "`TriggeredAbility` (`DrawService`'s emblem reader, Ob Nixilis Reignited); no collector reads it on a "
         "permanent |\n| `Triggers.opponentDrawsCard()` | Whenever an opponent draws a card (all draws"),
        ("`affectedPlayerId` is baked in at trigger time with the enchanted permanent's controller. Effects: "
         "`GivePoisonCountersEffect` (Relic Putrescence)",
         "The tapped permanent is the entry's triggering permanent. Effects: `GivePoisonCountersEffect(1, "
         "TRIGGERING_PERMANENT_CONTROLLER)` (Relic Putrescence, \"its controller gets a poison counter\")"),
    ],
    "EFFECTS_INDEX.md": [
        ("`OWN` (a player exiles `count` cards from their own graveyard, their choice; `affectedPlayerId` baked at "
         "trigger time",
         "`OWN` (the controller exiles `count` cards from their own graveyard, their choice), `TRIGGERING_PLAYER` "
         "(\"that player exiles `count` cards from their graveyard\": the entry's non-targeting player, the enchanted "
         "player of an enchanted-player upkeep"),
    ],
    "EFFECTS_QUICK_REFERENCE.md": [
        ("- `MillEffect(..., MillRecipient.CHOSEN_PLAYER)`",
         "- `MillEffect(new EventValue(), MillRecipient.TRIGGERING_PLAYER)` — \"that player mills that many cards\": "
         "the entry's non-targeting player the trigger event was about (Mindcrank's player who lost life, the life lost "
         "the event value).\n- `MillEffect(..., MillRecipient.CHOSEN_PLAYER)`"),
        ("- `CreateEmblemEffect(TriggeredAbility, String reminderText)` — \"you get an emblem with 'whenever [a player] "
         "casts a [quality] spell, …'\":",
         "- `CreateEmblemEffect(TriggeredAbility, String reminderText)` — \"you get an emblem with 'whenever [a player] "
         "casts a [quality] spell / draws a card, …'\" (a draw trigger is read by `DrawService`, `Triggers."
         "playerDrawsCard()`):"),
        ("- `LoseLifeEffect(new CardTypesAmongTriggeringCards(), LoseLifeRecipient.EACH_OPPONENT)`",
         "- `MayPayLifeEqualToAmountEffect(new TriggeringCardManaValue(), wrapped, prompt)` — \"you may pay life equal to "
         "its mana value\" of the card a graveyard trigger was about (Lorcan, Warlock Collector): "
         "`TriggeringCardManaValue` reads the entry's triggering card as it was in the event.\n"
         "- `LoseLifeEffect(new CardTypesAmongTriggeringCards(), LoseLifeRecipient.EACH_OPPONENT)`"),
    ],
}

REMOVED = ["MillOpponentOnLifeLossEffect()", "CradleOfVitalityLifeGainEffect(\"", "NykthosParagonLifeGainEffect()",
           "DrawCardsEqualToLifeGainedEffect()", "EmblemControllerLosesLifeOnAnyPlayerDrawEffect(int",
           "EmblemControllerLosesLifeOnAnyPlayerDrawEffect(2)", "DealDamageToAnyTargetOnControllerSpellCastEffect(5)",
           "ENCHANTED_PERMANENT_CONTROLLER (`", "ErestorOfTheCouncilEffect(VotingResult)",
           "ModelOfUnityEffect(VotingResult)", "IntoTopFiveEffect(Card creatureCard)", "IntoTopFiveEffect(snapshot)",
           "`affectedPlayerId` is baked in at trigger time with the enchanted permanent's controller"]


def edit_lines(path, edits):
    lines = path.read_text().split("\n")
    for key, new in edits:
        hits = [i for i, line in enumerate(lines) if key in line]
        if new in lines:
            continue
        if len(hits) != 1:
            raise SystemExit(f"{path.name}: {len(hits)} lines contain {key!r}")
        lines[hits[0]] = new
    path.write_text("\n".join(lines))


def edit_fragments(path, edits):
    text = path.read_text()
    for old, new in edits:
        if new in text:
            continue
        if text.count(old) != 1:
            raise SystemExit(f"{path.name}: {text.count(old)} occurrences of {old[:60]!r}")
        text = text.replace(old, new)
    path.write_text(text)


def main():
    for name, edits in LINES.items():
        edit_lines(DOCS / name, edits)
    for name, edits in FRAGMENTS.items():
        edit_fragments(DOCS / name, edits)
    paths = list(DOCS.glob("*.md")) + list((ROOT / ".claude/skills").rglob("*.md"))
    for path in paths:
        text = path.read_text()
        for form in REMOVED:
            if form in text:
                raise SystemExit(f"{path.relative_to(ROOT)} still names {form!r}")


if __name__ == "__main__":
    main()
