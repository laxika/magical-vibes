package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mossdog;
import com.github.laxika.magicalvibes.cards.s.SealOfCleansing;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReverentSilence.class, Forest.class, Mossdog.class, SealOfCleansing.class})
class ReverentSilenceTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all enchantments and makes the opponent gain life when cast for its alternate cost")
    void castsForAlternateCost() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent ownEnchantment = harness.addToBattlefieldAndReturn(player1, new SealOfCleansing());
        Permanent opponentEnchantment = harness.addToBattlefieldAndReturn(player2, new SealOfCleansing());
        Permanent creature = addCreatureReady(player2, new Mossdog());
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        harness.setHand(player1, List.of(new ReverentSilence()));
        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownEnchantment).contains(forest);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentEnchantment).contains(creature);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife + 6);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot use the alternate cost without controlling a Forest")
    void alternateCostRequiresForest() {
        harness.setHand(player1, List.of(new ReverentSilence()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, (java.util.UUID) null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be cast normally and only destroys enchantments")
    void castsNormally() {
        Permanent ownEnchantment = harness.addToBattlefieldAndReturn(player1, new SealOfCleansing());
        Permanent opponentEnchantment = harness.addToBattlefieldAndReturn(player2, new SealOfCleansing());
        Permanent creature = addCreatureReady(player2, new Mossdog());
        harness.castFromHand(player1, new ReverentSilence(), "{3}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownEnchantment);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentEnchantment).contains(creature);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A tapped Forest enables the alternate cost and life is gained before resolution")
    void alternateCostIsPaidDuringCastingWithTappedForest() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new SealOfCleansing());
        harness.setHand(player1, List.of(new ReverentSilence()));

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 26);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchantment);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        harness.assertLife(player2, 26);
        harness.assertInGraveyard(player2, "Seal of Cleansing");
        harness.assertInGraveyard(player1, "Reverent Silence");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(forest);
    }

    @Test
    @DisplayName("An opponent's Forest does not enable the alternate cost")
    void opponentsForestDoesNotEnableAlternateCost() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new ReverentSilence()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, (java.util.UUID) null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player2, 20);
        harness.assertInHand(player1, "Reverent Silence");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The caster can choose the mana cost despite controlling a Forest")
    void normalCostRemainsAvailableWithForestAndNoEnchantments() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.castFromHand(player1, new ReverentSilence(), "{3}{G}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Reverent Silence");
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(forest);
    }
}
