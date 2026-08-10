package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MageIlVecTest extends BaseCardTest {

    @Test
    @DisplayName("Ability deals 1 damage to a creature and discards a card at random as a cost")
    void damagesCreatureAndDiscardsAtRandom() {
        addReadyMageIlVec();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));

        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, bearId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(harness.getGameData().playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Ability deals 1 damage to a player")
    void damagesPlayer() {
        addReadyMageIlVec();
        harness.setHand(player1, List.of(new Forest()));
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot activate with an empty hand")
    void cannotActivateWithEmptyHand() {
        addReadyMageIlVec();
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addReadyMageIlVec() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new MageIlVec());
        mage.setSummoningSick(false);
    }
}
