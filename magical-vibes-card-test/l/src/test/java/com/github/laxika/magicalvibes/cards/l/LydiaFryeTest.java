package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AssassinInitiate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LydiaFrye.class, AssassinInitiate.class, LoyalInventor.class})
class LydiaFryeTest extends BaseCardTest {

    @Test
    @DisplayName("Lydia Frye can't be blocked by creatures with power 3 or greater")
    void cannotBeBlockedByPowerThreeOrGreaterCreature() {
        Permanent blocker = addCreatureReady(player2, new LydiaFrye());
        Permanent lydia = addCreatureReady(player1, new LydiaFrye());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blocker.isBlocking()).isFalse();
        assertThat(lydia.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Lydia Frye can be blocked by a creature with power less than 3")
    void canBeBlockedByCreatureWithPowerLessThanThree() {
        Permanent blocker = addCreatureReady(player2, new LoyalInventor());
        addCreatureReady(player1, new LydiaFrye());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The end-step trigger surveils for each tapped Assassin you control")
    void surveilsForTappedAssassins() {
        addCreatureReady(player1, new LydiaFrye());
        Permanent tappedAssassin = addCreatureReady(player1, new AssassinInitiate());
        addCreatureReady(player1, new AssassinInitiate());
        Card first = new LoyalInventor();
        Card second = new LydiaFrye();
        harness.setLibrary(player1, List.of(first, second));

        tappedAssassin.tap();
        enterEndStep();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(tappedAssassin.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    void countsLydiaHerselfAndCanKeepTheTopCard() {
        Permanent lydia = addCreatureReady(player1, new LydiaFrye());
        lydia.tap();
        Card top = new LoyalInventor();
        harness.setLibrary(player1, List.of(top));

        enterEndStep();
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void surveilsMultipleCardsAndReordersThoseKeptOnTop() {
        Permanent lydia = addCreatureReady(player1, new LydiaFrye());
        lydia.tap();
        addCreatureReady(player1, new AssassinInitiate()).tap();
        addCreatureReady(player1, new AssassinInitiate()).tap();
        addCreatureReady(player1, new AssassinInitiate());
        addCreatureReady(player1, new LoyalInventor()).tap();
        addCreatureReady(player2, new AssassinInitiate()).tap();
        Card first = new LoyalInventor();
        Card second = new AssassinInitiate();
        Card third = new LydiaFrye();
        Card fourth = new LoyalInventor();
        harness.setLibrary(player1, List.of(first, second, third, fourth));

        enterEndStep();
        resolveAllTriggers();
        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(first, second, third);
        assertThat(surveil.toGraveyard()).isTrue();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(2, 0), List.of(1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, first, fourth);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(second);
    }

    @Test
    void determinesTappedAssassinCountWhenTheTriggerResolves() {
        addCreatureReady(player1, new LydiaFrye());
        Permanent assassin = addCreatureReady(player1, new AssassinInitiate());
        Card top = new LoyalInventor();
        harness.setLibrary(player1, List.of(top));

        enterEndStep();
        assertThat(gd.stack).hasSize(1);
        assassin.tap();
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void surveilsZeroWithoutTappedAssassinsYouControl() {
        addCreatureReady(player1, new LydiaFrye());
        addCreatureReady(player1, new LoyalInventor()).tap();
        addCreatureReady(player2, new AssassinInitiate()).tap();
        Card top = new AssassinInitiate();
        harness.setLibrary(player1, List.of(top));

        enterEndStep();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        addCreatureReady(player1, new LydiaFrye()).tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void usesBlockersCurrentPowerRatherThanPrintedPower() {
        Permanent blocker = addCreatureReady(player2, new LoyalInventor());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player1, new LydiaFrye());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    void canBeBlockedByCreatureWhosePowerHasFallenBelowThree() {
        Permanent blocker = addCreatureReady(player2, new LydiaFrye());
        blocker.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        addCreatureReady(player1, new LydiaFrye());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void surveilsAllAvailableCardsWhenLibraryIsSmallerThanAssassinCount() {
        addCreatureReady(player1, new LydiaFrye()).tap();
        addCreatureReady(player1, new AssassinInitiate()).tap();
        addCreatureReady(player1, new AssassinInitiate()).tap();
        Card top = new LoyalInventor();
        harness.setLibrary(player1, List.of(top));

        enterEndStep();
        resolveAllTriggers();
        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(top);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(top);
    }

    private void enterEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
