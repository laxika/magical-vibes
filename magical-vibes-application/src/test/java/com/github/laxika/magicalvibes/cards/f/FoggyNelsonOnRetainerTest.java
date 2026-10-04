package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FoggyNelsonOnRetainer.class, GrizzlyBears.class})
class FoggyNelsonOnRetainerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on another creature and grants hexproof")
    void boostsAndProtectsAnotherCreature() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castFoggyNelson();
        resolveEtbTargeting(ally);

        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ally.hasKeyword(Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("ETB hexproof expires at the end of the turn")
    void grantedHexproofExpiresAtEndOfTurn() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castFoggyNelson();
        resolveEtbTargeting(ally);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ally.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB cannot target Foggy Nelson itself or an opponent's creature")
    void rejectsIllegalTargets() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castFoggyNelson();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(ally.getId());

        Permanent foggyNelson = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, foggyNelson.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's upkeep")
    void canBeCastDuringOpponentsTurn() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        castFoggyNelson();
        resolveEtbTargeting(ally);

        harness.assertOnBattlefield(player1, "Foggy Nelson, On Retainer");
        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ally.hasKeyword(Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Foggy enters without a legal target and does not enhance itself")
    void entersWithoutAnotherCreature() {
        castFoggyNelson();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        Permanent foggy = findPermanent(player1, "Foggy Nelson, On Retainer");
        assertThat(foggy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(foggy.hasKeyword(Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("The ETB ability resolves even after Foggy leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castFoggyNelson();
        harness.handlePermanentChosen(player1, ally.getId());
        Permanent foggy = findPermanent(player1, "Foggy Nelson, On Retainer");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, foggy));

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Foggy Nelson, On Retainer");
        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ally.hasKeyword(Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("An ETB target that leaves receives neither counters nor hexproof")
    void triggerDoesNothingWhenTargetLeaves() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castFoggyNelson();
        harness.handlePermanentChosen(player1, ally.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, ally));

        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ally.hasKeyword(Keyword.HEXPROOF)).isFalse();
        Permanent foggy = findPermanent(player1, "Foggy Nelson, On Retainer");
        assertThat(foggy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(foggy.hasKeyword(Keyword.HEXPROOF)).isFalse();
    }

    private void castFoggyNelson() {
        harness.castFromHand(player1, new FoggyNelsonOnRetainer(), "{2}{W}");
        harness.passBothPriorities();
    }

    private void resolveEtbTargeting(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
