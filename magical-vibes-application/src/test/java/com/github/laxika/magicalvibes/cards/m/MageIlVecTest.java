package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FurnaceBrood;
import com.github.laxika.magicalvibes.cards.r.RabidWolverines;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MageIlVec.class, FurnaceBrood.class, RabidWolverines.class})
class MageIlVecTest extends BaseCardTest {

    @Test
    @DisplayName("Ability deals 1 damage to a creature and discards a card at random as a cost")
    void damagesCreatureAndDiscardsAtRandom() {
        Permanent mage = addCreatureReady(player1, new MageIlVec());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FurnaceBrood());
        harness.setHand(player1, List.of(new RabidWolverines()));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(mage.isTapped()).isTrue();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Rabid Wolverines");
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player2, "Furnace Brood");
    }

    @Test
    @DisplayName("Ability deals 1 damage to a player")
    void damagesPlayer() {
        Permanent mage = addCreatureReady(player1, new MageIlVec());
        harness.setHand(player1, List.of(new RabidWolverines()));
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(mage.isTapped()).isTrue();
        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player1, "Rabid Wolverines");
    }

    @Test
    @DisplayName("Cannot activate with an empty hand")
    void cannotActivateWithEmptyHand() {
        Permanent mage = addCreatureReady(player1, new MageIlVec());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mage.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent mage = addCreatureReady(player1, new MageIlVec());
        mage.tap();
        harness.setHand(player1, List.of(new RabidWolverines()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
        harness.assertInHand(player1, "Rabid Wolverines");
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new MageIlVec());
        harness.setHand(player1, List.of(new RabidWolverines()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        harness.assertInHand(player1, "Rabid Wolverines");
    }
}
