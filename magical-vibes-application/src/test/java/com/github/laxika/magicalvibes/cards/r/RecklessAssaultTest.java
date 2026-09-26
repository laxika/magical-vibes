package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.MightWeaver;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RecklessAssault.class, MightWeaver.class})
class RecklessAssaultTest extends BaseCardTest {

    @Test
    @DisplayName("Pays 2 life and deals 1 damage to a player")
    void paysLifeAndDamagesPlayer() {
        harness.addToBattlefield(player1, new RecklessAssault());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Deals 1 damage to a target creature")
    void damagesCreature() {
        harness.addToBattlefield(player1, new RecklessAssault());
        harness.addToBattlefield(player2, new MightWeaver());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        var targetId = harness.getPermanentId(player2, "Might Weaver");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Might Weaver");
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Cannot activate without enough life to pay the cost")
    void cannotPayLifeCost() {
        harness.addToBattlefield(player1, new RecklessAssault());
        harness.setLife(player1, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate at exactly 2 life but loses before the ability resolves")
    void canActivateAtExactlyTwoLife() {
        harness.addToBattlefield(player1, new RecklessAssault());
        harness.setLife(player1, 2);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertLife(player1, 0);
        harness.assertLife(player2, 20);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }
}
