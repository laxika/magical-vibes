package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EzioBrashNovice;
import com.github.laxika.magicalvibes.cards.r.RoyalAssassin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheAnimus.class, EzioBrashNovice.class, RoyalAssassin.class})
class TheAnimusTest extends BaseCardTest {

    @Test
    void exilesUpToOneLegendaryCreatureFromAnyGraveyardWithMemoryCounter() {
        Card legendaryCreature = new EzioBrashNovice();
        harness.setGraveyard(player2, List.of(legendaryCreature));
        addCreatureReady(player1, new TheAnimus());

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(legendaryCreature.getId()));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(legendaryCreature.getId());
        assertThat(gd.exiledCardsWithMemoryCounters).contains(legendaryCreature.getId());
    }

    @Test
    void makesTargetLegendaryCreatureCopyMemoryMarkedExiledCreatureUntilNextTurn() {
        Card exiledCreature = new RoyalAssassin();
        harness.setExile(player1, List.of(exiledCreature));
        gd.exiledCardsWithMemoryCounters.add(exiledCreature.getId());
        addCreatureReady(player1, new TheAnimus());
        Permanent legendaryCreature = addCreatureReady(player1, new EzioBrashNovice());

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(legendaryCreature.getId(), exiledCreature.getId()));
        harness.passBothPriorities();

        assertThat(legendaryCreature.getCard().getName()).isEqualTo("Royal Assassin");
        assertThat(legendaryCreature.isCopyUntilControllerNextTurn()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()).get(0).isTapped()).isTrue();
    }

    @Test
    void canDeclineExilingAnEligibleCard() {
        Card legendaryCreature = new EzioBrashNovice();
        harness.setGraveyard(player1, List.of(legendaryCreature));
        harness.addToBattlefield(player1, new TheAnimus());

        harness.passUntil(player1, TurnStep.END_STEP);
        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(legendaryCreature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exiledCardsWithMemoryCounters).isEmpty();
    }

    @Test
    void graveyardChoiceExcludesNonlegendaryCreaturesAndLegendaryNoncreatures() {
        Card eligible = new EzioBrashNovice();
        Card nonlegendary = new RoyalAssassin();
        Card noncreature = new TheAnimus();
        harness.setGraveyard(player1, List.of(eligible, nonlegendary, noncreature));
        harness.addToBattlefield(player1, new TheAnimus());

        harness.passUntil(player1, TurnStep.END_STEP);

        PendingInteraction.MultiGraveyardChoice choice =
                (PendingInteraction.MultiGraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice.cards()).containsExactly(eligible);
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        resolveAllTriggers();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonlegendary, noncreature);
    }

    @Test
    void doesNotTriggerOnOpponentsEndStep() {
        Card eligible = new EzioBrashNovice();
        harness.setGraveyard(player1, List.of(eligible));
        harness.addToBattlefield(player1, new TheAnimus());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(eligible);
    }

    @Test
    void graveyardTargetLeavingBeforeResolutionIsNotMarked() {
        Card eligible = new EzioBrashNovice();
        harness.setGraveyard(player1, List.of(eligible));
        harness.addToBattlefield(player1, new TheAnimus());
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP,
                () -> harness.handleMultipleCardsChosen(player1, List.of(eligible.getId())));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(eligible));

        resolveAllTriggers();

        assertThat(gd.exiledCardsWithMemoryCounters).doesNotContain(eligible.getId());
    }

    @Test
    void canCopyAnOpponentsExiledNonlegendaryCreatureAndUseItsAbility() {
        Card exiled = new RoyalAssassin();
        harness.setExile(player2, List.of(exiled));
        gd.exiledCardsWithMemoryCounters.add(exiled.getId());
        harness.addToBattlefield(player1, new TheAnimus());
        Permanent copier = addCreatureReady(player1, new EzioBrashNovice());
        Permanent victim = addCreatureReady(player2, new RoyalAssassin());
        victim.tap();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(copier.getId(), exiled.getId()));
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 0, victim.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(victim);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(victim.getOriginalCard());
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(exiled);
        assertThat(gd.exiledCardsWithMemoryCounters).contains(exiled.getId());
    }

    @Test
    void rejectsAnExiledCreatureWithoutAMemoryCounter() {
        Card exiled = new RoyalAssassin();
        harness.setExile(player1, List.of(exiled));
        Permanent animus = harness.addToBattlefieldAndReturn(player1, new TheAnimus());
        Permanent copier = addCreatureReady(player1, new EzioBrashNovice());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(copier.getId(), exiled.getId()))).isInstanceOf(IllegalStateException.class);
        assertThat(animus.isTapped()).isFalse();
    }

    @Test
    void rejectsAMemoryMarkedNoncreatureInExile() {
        Card exiled = new TheAnimus();
        harness.setExile(player1, List.of(exiled));
        gd.exiledCardsWithMemoryCounters.add(exiled.getId());
        Permanent animus = harness.addToBattlefieldAndReturn(player1, new TheAnimus());
        Permanent copier = addCreatureReady(player1, new EzioBrashNovice());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(copier.getId(), exiled.getId()))).isInstanceOf(IllegalStateException.class);
        assertThat(animus.isTapped()).isFalse();
    }

    @Test
    void rejectsActivationWhileAnotherAbilityIsOnTheStack() {
        Card exiled = new RoyalAssassin();
        harness.setExile(player1, List.of(exiled));
        gd.exiledCardsWithMemoryCounters.add(exiled.getId());
        Permanent animus = harness.addToBattlefieldAndReturn(player1, new TheAnimus());
        Permanent copier = addCreatureReady(player1, new EzioBrashNovice());
        addCreatureReady(player1, new RoyalAssassin());
        Permanent victim = addCreatureReady(player2, new RoyalAssassin());
        victim.tap();
        harness.activateAbility(player1, 2, 0, victim.getId());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(copier.getId(), exiled.getId()))).isInstanceOf(IllegalStateException.class);
        assertThat(animus.isTapped()).isFalse();
        harness.passBothPriorities();
    }

    @Test
    void preservesCountersAndTappedStateWhenCopying() {
        Card exiled = new RoyalAssassin();
        harness.setExile(player1, List.of(exiled));
        gd.exiledCardsWithMemoryCounters.add(exiled.getId());
        harness.addToBattlefield(player1, new TheAnimus());
        Permanent copier = addCreatureReady(player1, new EzioBrashNovice());
        copier.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        copier.tap();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(copier.getId(), exiled.getId()));
        harness.passBothPriorities();

        assertThat(copier.getCard().getName()).isEqualTo("Royal Assassin");
        assertThat(copier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(copier.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, copier)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, copier)).isEqualTo(3);
    }

    @Test
    void copyDoesNothingIfTheMemoryCounterIsRemovedBeforeResolution() {
        Card exiled = new RoyalAssassin();
        harness.setExile(player1, List.of(exiled));
        gd.exiledCardsWithMemoryCounters.add(exiled.getId());
        harness.addToBattlefield(player1, new TheAnimus());
        Permanent copier = addCreatureReady(player1, new EzioBrashNovice());
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(copier.getId(), exiled.getId()));
        gd.exiledCardsWithMemoryCounters.remove(exiled.getId());

        harness.passBothPriorities();

        assertThat(copier.getCard().getName()).isEqualTo("Ezio, Brash Novice");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiled);
    }

    @Test
    void rejectsNonlegendaryOrOpponentsBattlefieldTargets() {
        Card exiled = new RoyalAssassin();
        harness.setExile(player1, List.of(exiled));
        gd.exiledCardsWithMemoryCounters.add(exiled.getId());
        Permanent animus = harness.addToBattlefieldAndReturn(player1, new TheAnimus());
        Permanent nonlegendary = addCreatureReady(player1, new RoyalAssassin());
        Permanent opposingLegendary = addCreatureReady(player2, new EzioBrashNovice());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(nonlegendary.getId(), exiled.getId()))).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(opposingLegendary.getId(), exiled.getId()))).isInstanceOf(IllegalStateException.class);
        assertThat(animus.isTapped()).isFalse();
    }

    @Test
    void rejectsActivationOutsideYourMainPhase() {
        Card exiled = new RoyalAssassin();
        harness.setExile(player1, List.of(exiled));
        gd.exiledCardsWithMemoryCounters.add(exiled.getId());
        Permanent animus = harness.addToBattlefieldAndReturn(player1, new TheAnimus());
        Permanent copier = addCreatureReady(player1, new EzioBrashNovice());
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(copier.getId(), exiled.getId()))).isInstanceOf(IllegalStateException.class);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(copier.getId(), exiled.getId()))).isInstanceOf(IllegalStateException.class);
        assertThat(animus.isTapped()).isFalse();
    }

    @Test
    void copyDoesNothingIfTheExiledTargetLeavesBeforeResolution() {
        Card exiled = new RoyalAssassin();
        harness.setExile(player1, List.of(exiled));
        gd.exiledCardsWithMemoryCounters.add(exiled.getId());
        harness.addToBattlefield(player1, new TheAnimus());
        Permanent copier = addCreatureReady(player1, new EzioBrashNovice());
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(copier.getId(), exiled.getId()));
        gd.removeFromExile(exiled.getId());

        harness.passBothPriorities();

        assertThat(copier.getCard().getName()).isEqualTo("Ezio, Brash Novice");
    }

    @Test
    void copyDoesNothingIfTheCreatureChangesControllerBeforeResolution() {
        Card exiled = new RoyalAssassin();
        harness.setExile(player1, List.of(exiled));
        gd.exiledCardsWithMemoryCounters.add(exiled.getId());
        harness.addToBattlefield(player1, new TheAnimus());
        Permanent copier = addCreatureReady(player1, new EzioBrashNovice());
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(copier.getId(), exiled.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(copier);
        gd.playerBattlefields.get(player2.getId()).add(copier);

        harness.passBothPriorities();

        assertThat(copier.getCard().getName()).isEqualTo("Ezio, Brash Novice");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiled);
    }

    @Test
    void copySurvivesOpponentsTurnAndEndsAtTheBeginningOfYourNextTurn() {
        Card exiled = new RoyalAssassin();
        harness.setExile(player1, List.of(exiled));
        gd.exiledCardsWithMemoryCounters.add(exiled.getId());
        harness.addToBattlefield(player1, new TheAnimus());
        Permanent copier = addCreatureReady(player1, new EzioBrashNovice());
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(copier.getId(), exiled.getId()));
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).removeFirst();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(copier.getCard().getName()).isEqualTo("Royal Assassin");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.UPKEEP);

        assertThat(copier.getCard().getName()).isEqualTo("Ezio, Brash Novice");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiled);
    }
}
