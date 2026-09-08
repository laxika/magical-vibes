package com.github.laxika.magicalvibes.service.planar;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Panopticon;
import com.github.laxika.magicalvibes.cards.v.Voidslime;
import com.github.laxika.magicalvibes.model.*;
import com.github.laxika.magicalvibes.model.effect.*;
import com.github.laxika.magicalvibes.model.planar.*;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.service.state.StateBasedActionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.testutil.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@CardUsed({Panopticon.class, GrizzlyBears.class, Voidslime.class})
class PlanechaseServiceTest extends BaseCardTest {
    private PlanechaseService planar;

    @BeforeEach
    void setupPlanarState() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.deck.add(new Panopticon());
        gd.startingPlayerId = player1.getId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Card fixture(String name, CardType type, EffectSlot slot, CardEffect effect) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        if (effect != null) card.addEffect(slot, effect);
        return card;
    }

    private PlanarObject faceUp(Card card) {
        PlanarObject object = new PlanarObject(card, gd.nextTimestamp());
        gd.planechase.faceUp.add(object);
        return object;
    }

    @Test
    void continuousBoostAffectsBothPlayersAndStopsAfterPlaneswalking() {
        faceUp(fixture("Test plane", CardType.PLANE, EffectSlot.STATIC,
                new StaticBoostEffect(1, 1, Set.of(Keyword.FLYING), GrantScope.ALL_CREATURES)));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent first = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent second = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isTrue();
        harness.inMutationScope(() -> planar.planeswalk(gd));
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isFalse();
    }

    @Test
    void controllerRelativeContinuousEffectMovesWithController() {
        faceUp(fixture("Test plane", CardType.PLANE, EffectSlot.STATIC,
                new StaticBoostEffect(1, 1, Set.of(), GrantScope.OWN_CREATURES)));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent first = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent second = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        gd.planechase.controllerId = player2.getId();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
    }

    @Test
    void encounterWaitsForResolutionThenAutomaticallyPlaneswalks() {
        gd.planechase.deck.addFirst(fixture("Test phenomenon", CardType.PHENOMENON,
                EffectSlot.ENCOUNTER_TRIGGERED, new DrawCardEffect(1)));
        int before = gd.playerHands.get(player1.getId()).size();
        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.runStateBasedActions();
        assertThat(gd.planechase.faceUp.getFirst().getCard().getName()).isEqualTo("Test phenomenon");
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(before + 1);
        assertThat(gd.planechase.faceUp.getFirst().getCard()).isInstanceOf(Panopticon.class);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(before + 2);
    }

    @Test
    void phenomenonWaitsForAllOfItsTriggeredAbilities() {
        Card phenomenon = fixture("Test phenomenon", CardType.PHENOMENON,
                EffectSlot.ENCOUNTER_TRIGGERED, new DrawCardEffect(1));
        phenomenon.addEffect(EffectSlot.ENCOUNTER_TRIGGERED, new DrawCardEffect(1));
        gd.planechase.deck.addFirst(phenomenon);
        harness.inMutationScope(() -> planar.reveal(gd, true));
        harness.passBothPriorities();
        assertThat(gd.planechase.faceUp.getFirst().getCard()).isSameAs(phenomenon);
        harness.passBothPriorities();
        assertThat(gd.planechase.faceUp.getFirst().getCard()).isInstanceOf(Panopticon.class);
    }

    @Test
    void startingRevealSkipsPhenomenaWithoutTriggering() {
        gd.planechase.deck.addFirst(fixture("Test phenomenon", CardType.PHENOMENON,
                EffectSlot.ENCOUNTER_TRIGGERED, new DrawCardEffect(1)));
        harness.inMutationScope(() -> planar.start(gd));
        assertThat(gd.stack).isEmpty();
        assertThat(gd.planechase.faceUp.getFirst().getCard()).isInstanceOf(Panopticon.class);
        assertThat(gd.planechase.deck).hasSize(1);
    }

    @Test
    void targetedChaosUsesExistingChoiceAndDamageResolution() {
        faceUp(fixture("Test plane", CardType.PLANE, EffectSlot.CHAOS_TRIGGERED,
                new DealDamageToAnyTargetEffect(2)));
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(TriggerCollectionService.class).processNextSpellTargetTrigger(gd));
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    void targetedEncounterIsNotSkippedWhileWaitingForItsTarget() {
        gd.planechase.deck.addFirst(fixture("Test phenomenon", CardType.PHENOMENON,
                EffectSlot.ENCOUNTER_TRIGGERED, new DealDamageToAnyTargetEffect(2)));
        harness.inMutationScope(() -> planar.reveal(gd, true));
        assertThat(planar.checkPhenomena(gd)).isFalse();
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(TriggerCollectionService.class).processNextSpellTargetTrigger(gd));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        assertThat(gd.planechase.faceUp.getFirst().getCard()).isInstanceOf(Panopticon.class);
    }

    @Test
    void planarActivatedAbilityPaysThenResolvesNormally() {
        Card plane = fixture("Test plane", CardType.PLANE, EffectSlot.STATIC, null);
        plane.addActivatedAbility(new ActivatedAbility(false, "{1}", List.of(new DrawCardEffect(1)), "Draw a card."));
        var source = faceUp(plane);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int before = gd.playerHands.get(player1.getId()).size();
        gs.activatePlanarAbility(gd, player1, source.getId(), 0, 0, null, null);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(before);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(before + 1);
    }

    @Test
    void sourceLessPlaneswalkingAbilityCanBeCountered() {
        var source = faceUp(new Panopticon());
        gd.stack.add(new StackEntry(StackEntryType.TRIGGERED_ABILITY, null, player1.getId(),
                "Planeswalk", List.of(new PlaneswalkEffect())));
        var id = gd.stack.getLast().getTargetableId();
        gd.playerSpellsCantBeCounteredByColorsThisTurn.put(player1.getId(), Set.of(CardColor.BLUE));
        harness.setHand(player2, List.of(new Voidslime()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        var validTargets = GameTestEngineContext.get().getBean(
                com.github.laxika.magicalvibes.service.target.ValidTargetService.class)
                .computeValidTargetsForSpell(gd, gd.playerHands.get(player2.getId()).getFirst(), player2.getId(), List.of());
        assertThat(validTargets.validPermanentIds()).contains(id);
        harness.castInstant(player2, 0, id);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.planechase.faceUp).containsExactly(source);
    }

    @Test
    void multipleDepartingPlanesAreOrderedWithoutTouchingLibraries() {
        var first = faceUp(fixture("First plane", CardType.PLANE, EffectSlot.STATIC, null));
        var second = faceUp(fixture("Second plane", CardType.PLANE, EffectSlot.STATIC, null));
        int librarySize = gd.playerDecks.get(player1.getId()).size();
        harness.inMutationScope(() -> planar.planeswalk(gd));
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));
        assertThat(gd.planechase.deck).containsExactly(second.getCard(), first.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySize);
    }

    @Test
    void publicProjectionNeverIncludesHiddenDeckOrder() {
        faceUp(new Panopticon());
        var view = GameTestEngineContext.get().getBean(PlanechaseViewService.class).create(gd, player1.getId());
        assertThat(view.deckSize()).isEqualTo(1);
        assertThat(view.faceUp()).hasSize(1);
        assertThat(view.canRoll()).isTrue();
        assertThat(view.canPayRoll()).isTrue();
        assertThat(GameTestEngineContext.get().getBean(PlanechaseViewService.class)
                .create(gd, player2.getId()).canRoll()).isFalse();
    }
    @Test
    void counteredEncounterStillPlaneswalksOnward() {
        gd.planechase.deck.addFirst(fixture("Test phenomenon", CardType.PHENOMENON,
                EffectSlot.ENCOUNTER_TRIGGERED, new DrawCardEffect(1)));
        harness.inMutationScope(() -> planar.reveal(gd, true));
        var target = gd.stack.getLast().getTargetableId();
        int before = gd.playerHands.get(player1.getId()).size();
        harness.setHand(player2, List.of(new Voidslime()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target);
        harness.passBothPriorities();
        assertThat(gd.planechase.faceUp.getFirst().getCard()).isInstanceOf(Panopticon.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(before);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(before + 1);
    }

    @Test
    void phenomenonDoesNotMoveDuringAnUnfinishedResolution() {
        faceUp(fixture("Test phenomenon", CardType.PHENOMENON, EffectSlot.STATIC, null));
        gd.effectResolutionDepth = 1;
        assertThat(planar.checkPhenomena(gd)).isFalse();
        gd.effectResolutionDepth = 0;
        harness.inMutationScope(() -> assertThat(planar.checkPhenomena(gd)).isTrue());
        assertThat(gd.planechase.faceUp.getFirst().getCard()).isInstanceOf(Panopticon.class);
    }

    @Test
    void staleOrUnauthorizedPlanarActivationDoesNotSpendMana() {
        Card plane = fixture("Test plane", CardType.PLANE, EffectSlot.STATIC, null);
        plane.addActivatedAbility(new ActivatedAbility(false, "{1}", List.of(new DrawCardEffect(1)), "Draw a card."));
        var source = faceUp(plane);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> gs.activatePlanarAbility(gd, player2, source.getId(), 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.inMutationScope(() -> planar.planeswalk(gd));
        assertThatThrownBy(() -> gs.activatePlanarAbility(gd, player1, source.getId(), 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void planeswalkingExpiresOnlyTheMatchingFloatingDuration() {
        faceUp(fixture("Test plane", CardType.PLANE, EffectSlot.STATIC, null));
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent creature = gd.playerBattlefields.get(player1.getId()).getFirst();
        gd.addFloatingEffect(new com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect(
                java.util.UUID.randomUUID(), "Test plane", null, player1.getId(),
                new BuffTargetCreatureIndefinitelyEffect(1, 1), creature.getId(), null, null,
                EffectDuration.UNTIL_PLANESWALK, gd.nextTimestamp()));
        gd.addFloatingEffect(new com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect(
                java.util.UUID.randomUUID(), "Other effect", null, player1.getId(),
                new BuffTargetCreatureIndefinitelyEffect(2, 2), creature.getId(), null, null,
                EffectDuration.UNTIL_END_OF_TURN, gd.nextTimestamp()));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        harness.inMutationScope(() -> planar.planeswalk(gd));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }

    @Test
    void separateAbilitiesOfTheSamePlaneHaveIndependentStackTargets() {
        faceUp(new Panopticon());
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> planar.chaos(gd));
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getFirst().getTargetableId()).isNotEqualTo(gd.stack.getLast().getTargetableId());
    }

    @Test
    void simulationCopiesPendingPlanarTargetSnapshots() {
        faceUp(fixture("Test plane", CardType.PLANE, EffectSlot.CHAOS_TRIGGERED,
                new DealDamageToAnyTargetEffect(2)));
        harness.inMutationScope(() -> planar.chaos(gd));
        var original = (PermanentChoiceContext.SpellTargetTriggerAnyTarget) gd.pendingInteractions.getFirst();
        GameData copy = gd.simulationCopy();
        var copied = (PermanentChoiceContext.SpellTargetTriggerAnyTarget) copy.pendingInteractions.getFirst();
        copied.planarSource().getCounters().put(CounterType.CHARGE, 2);
        copy.planechase.faceUp.getFirst().getCounters().put(CounterType.CHARGE, 3);
        assertThat(original.planarSource().getCounters()).isEmpty();
        assertThat(gd.planechase.faceUp.getFirst().getCounters()).isEmpty();
        assertThat(copied.planarSource().getId()).isEqualTo(original.planarSource().getId());
    }


    @Test
    @CardUsed(com.github.laxika.magicalvibes.cards.s.StrionicResonator.class)
    void sourceLessPlaneswalkingAbilityCanBeCopied() {
        faceUp(new Panopticon());
        harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.s.StrionicResonator());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        gd.stack.add(new StackEntry(StackEntryType.TRIGGERED_ABILITY, null, player1.getId(),
                "Planeswalk", List.of(new PlaneswalkEffect())));
        var target = gd.stack.getLast().getTargetableId();
        harness.activateAbility(player1, 0, null, target);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getFirst().getTargetableId()).isEqualTo(target);
        assertThat(gd.stack.getLast().getTargetableId()).isNotEqualTo(target);
        assertThat(gd.stack.getLast().isCopy()).isTrue();
    }

    @Test
    void turnControllerReceivesTheControlledPlayersRollAvailability() {
        faceUp(new Panopticon());
        harness.forceActivePlayer(player2);
        gd.planechase.controllerId = player2.getId();
        gd.mindControllerPlayerId = player1.getId();
        gd.mindControlledPlayerId = player2.getId();
        var views = GameTestEngineContext.get().getBean(PlanechaseViewService.class);
        assertThat(views.create(gd, player1.getId()).canRoll()).isTrue();
        assertThat(views.create(gd, player2.getId()).canRoll()).isFalse();
        gs.rollPlanarDie(gd, player1);
        assertThat(gd.planechase.lastRollPlayerId).isEqualTo(player2.getId());
    }

    @Test
    void upkeepAndEndStepAbilitiesUseTheCurrentPlanarController() {
        Card plane = fixture("Test plane", CardType.PLANE, EffectSlot.UPKEEP_TRIGGERED, new DrawCardEffect(1));
        plane.addEffect(EffectSlot.END_STEP_TRIGGERED, new DrawCardEffect(1));
        faceUp(plane);
        var steps = GameTestEngineContext.get().getBean(com.github.laxika.magicalvibes.service.turn.StepTriggerService.class);
        int firstBefore = gd.playerHands.get(player1.getId()).size();
        harness.inMutationScope(() -> steps.handleUpkeepTriggers(gd));
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(firstBefore + 1);
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        int secondBefore = gd.playerHands.get(player2.getId()).size();
        harness.inMutationScope(() -> steps.handleEndStepTriggers(gd));
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(secondBefore + 1);
    }

    @Test
    void reconnectAndUpdatesProjectTheSameIndependentPlanarSnapshot() {
        var source = faceUp(new Panopticon());
        source.getCounters().put(CounterType.CHARGE, 1);
        var projections = GameTestEngineContext.get().getBean(com.github.laxika.magicalvibes.service.GameViewProjectionFactory.class);
        var initial = projections.getJoinGame(gd, player1.getId()).planechase();
        var update = projections.createGameStateMessages(gd, List.of(), List.of(player1.getId()))
                .get(player1.getId()).planechase();
        assertThat(update).isEqualTo(initial);
        source.getCounters().put(CounterType.CHARGE, 2);
        assertThat(initial.faceUp().getFirst().counters()).containsEntry(CounterType.CHARGE, 1);
        var reconnected = projections.getJoinGame(gd, player1.getId()).planechase();
        assertThat(reconnected.faceUp().getFirst().counters()).containsEntry(CounterType.CHARGE, 2);
        assertThat(reconnected.faceUp().getFirst().id()).isEqualTo(source.getId());
    }

    @Test
    void powerstoneManaSourcesKeepAPayableRollAvailable() {
        faceUp(new Panopticon());
        gd.planechase.recordSpecialAction(player1.getId(), gd.turnNumber);
        gd.planechase.recordSpecialAction(player1.getId(), gd.turnNumber);
        gd.playerManaPools.get(player1.getId()).addPowerstoneOnlyColorless(1);
        Card powerstone = fixture("Powerstone", CardType.ARTIFACT, EffectSlot.STATIC, null);
        powerstone.addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new AwardRestrictedManaEffect(ManaColor.COLORLESS, 1, new ManaRestriction.Powerstone())), "Add mana."));
        harness.addToBattlefield(player1, powerstone);
        var views = GameTestEngineContext.get().getBean(PlanechaseViewService.class);
        assertThat(views.create(gd, player1.getId()).canRoll()).isTrue();
        assertThat(views.create(gd, player1.getId()).canPayRoll()).isFalse();
        harness.activateAbility(player1, 0, null, null);
        assertThat(views.create(gd, player1.getId()).canPayRoll()).isTrue();
        gs.rollPlanarDie(gd, player1);
        assertThat(gd.planechase.rollSequence).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isZero();
    }

    @Test
    void queuedTargetsPreventRollingBeforeTheTriggerIsPutOnTheStack() {
        faceUp(fixture("Test plane", CardType.PLANE, EffectSlot.CHAOS_TRIGGERED,
                new DealDamageToAnyTargetEffect(2)));
        harness.inMutationScope(() -> planar.chaos(gd));
        assertThat(planar.canRollAtThisTime(gd, player1.getId())).isFalse();
    }

    @Test
    void lobbyCreationEnablesPlanarStateOnlyWhenRequested() {
        var setup = harness.getGameSetupService();
        var creator = new Player(java.util.UUID.randomUUID(), "Planar player");
        GameData enabled = setup.createGame("Planechase", creator, "10e-white-theme-deck", false, null, true);
        try {
            assertThat(enabled.planechase.deck).hasSize(20);
            assertThat(enabled.planechase.faceUp).isEmpty();
            assertThat(enabled.status).isEqualTo(GameStatus.WAITING);
        } finally {
            harness.getGameRegistry().remove(enabled.id);
        }
        GameData ordinary = setup.createGame("Ordinary", creator, "10e-white-theme-deck", false);
        try {
            assertThat(ordinary.planechase).isNull();
        } finally {
            harness.getGameRegistry().remove(ordinary.id);
        }
    }

}
