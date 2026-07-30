package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TibaltTheFiendBloodedTest extends BaseCardTest {

    @Test
    @DisplayName("+1 draws a card then discards a card at random")
    void plusOneDrawsThenDiscardsAtRandom() {
        Permanent tibalt = addReadyTibalt(player1, 2);
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(tibalt.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        // Drew one (3 in hand), then discarded one at random (back to 2).
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("-4 deals damage equal to target player's hand size to that player")
    void minusFourDealsDamageEqualToHandSize() {
        Permanent tibalt = addReadyTibalt(player1, 4);
        harness.setHand(player2, List.of(new GrizzlyBears(), new LightningBolt(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        // Loyalty hit 0, so Tibalt is gone.
        harness.assertNotOnBattlefield(player1, "Tibalt, the Fiend-Blooded");
        assertThat(tibalt.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    @DisplayName("-4 deals no damage when the target player has an empty hand")
    void minusFourEmptyHandDealsNoDamage() {
        addReadyTibalt(player1, 4);
        harness.setHand(player2, List.of());

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("-6 gains control of every creature, untaps them, and gives them haste")
    void minusSixStealsUntapsAndHastes() {
        addReadyTibalt(player1, 6);
        Permanent theirs = addCreatureReady(player2, new GrizzlyBears());
        theirs.tap();
        Permanent mine = addCreatureReady(player1, new GrizzlyBears());
        mine.tap();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(theirs.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(theirs.getId()));
        assertThat(theirs.isTapped()).isFalse();
        assertThat(mine.isTapped()).isFalse();
        assertThat(theirs.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(mine.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("-6 control gain and haste expire at end of turn")
    void minusSixWearsOff() {
        addReadyTibalt(player1, 6);
        Permanent theirs = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(theirs.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(theirs.getId()));
        assertThat(theirs.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate -6 with only 2 loyalty")
    void cannotActivateMinusSixWithoutLoyalty() {
        addReadyTibalt(player1, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    private Permanent addReadyTibalt(Player player, int loyalty) {
        Permanent perm = new Permanent(new TibaltTheFiendBlooded());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
