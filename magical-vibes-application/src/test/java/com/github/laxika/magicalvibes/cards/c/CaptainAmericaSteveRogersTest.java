package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AIMBot;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaptainAmericaSteveRogers.class, AIMBot.class})
class CaptainAmericaSteveRogersTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts a counter on another creature you control and grants indestructible")
    void attackBoostsAnotherCreatureYouControl() {
        Permanent captain = addCreatureReady(player1, new CaptainAmericaSteveRogers());
        Permanent ally = addCreatureReady(player1, new AIMBot());

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(ally.getId());

        harness.handlePermanentChosen(player1, ally.getId());
        harness.passBothPriorities();

        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, ally, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The attack trigger cannot target Captain America or an opponent's creature")
    void targetMustBeAnotherCreatureYouControl() {
        Permanent captain = addCreatureReady(player1, new CaptainAmericaSteveRogers());
        addCreatureReady(player1, new AIMBot());
        Permanent opponent = addCreatureReady(player2, new AIMBot());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, captain.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The granted indestructible wears off at end of turn")
    void indestructibleWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new CaptainAmericaSteveRogers());
        Permanent ally = addCreatureReady(player1, new AIMBot());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, ally.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, ally, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("An attack without another friendly creature does not boost Captain America")
    void attackWithoutLegalTarget() {
        Permanent captain = addCreatureReady(player1, new CaptainAmericaSteveRogers());
        addCreatureReady(player2, new AIMBot());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(captain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, captain, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Captain America's unblocked combat damage gains life")
    void combatDamageGainsLife() {
        addCreatureReady(player1, new CaptainAmericaSteveRogers());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("The attack trigger still resolves after Captain America leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        Permanent captain = addCreatureReady(player1, new CaptainAmericaSteveRogers());
        Permanent ally = addCreatureReady(player1, new AIMBot());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, ally.getId());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToHand(gd, captain));
        harness.passBothPriorities();

        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, ally, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("An attack trigger does not affect a target that left and returned")
    void returnedCreatureIsNotTheOriginalTarget() {
        addCreatureReady(player1, new CaptainAmericaSteveRogers());
        AIMBot allyCard = new AIMBot();
        Permanent ally = addCreatureReady(player1, allyCard);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, ally.getId());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToHand(gd, ally));
        gd.playerHands.get(player1.getId()).remove(allyCard);
        Permanent returnedAlly = harness.enterBattlefieldAndReturn(player1, allyCard);
        harness.passBothPriorities();

        assertThat(returnedAlly.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, returnedAlly, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
