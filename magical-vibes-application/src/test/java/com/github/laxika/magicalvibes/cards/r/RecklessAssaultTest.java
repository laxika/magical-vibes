package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BirdsOfParadise;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecklessAssaultTest extends BaseCardTest {

    @Test
    @DisplayName("Pays 2 life and deals 1 damage to a player")
    void paysLifeAndDamagesPlayer() {
        harness.addToBattlefield(player1, new RecklessAssault());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, "Reckless Assault"), null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals 1 damage to a target creature")
    void damagesCreature() {
        harness.addToBattlefield(player1, new RecklessAssault());
        harness.addToBattlefield(player2, new BirdsOfParadise());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        var targetId = harness.getPermanentId(player2, "Birds of Paradise");
        harness.activateAbility(player1, battlefieldIndex(player1, "Reckless Assault"), null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Birds of Paradise");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot activate without enough life to pay the cost")
    void cannotPayLifeCost() {
        harness.addToBattlefield(player1, new RecklessAssault());
        harness.setLife(player1, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Reckless Assault"), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private int battlefieldIndex(Player player, String cardName) {
        List<Permanent> battlefield = harness.getGameData().playerBattlefields.get(player.getId());
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getCard().getName().equals(cardName)) {
                return i;
            }
        }
        throw new IllegalStateException("Permanent not found: " + cardName);
    }
}
