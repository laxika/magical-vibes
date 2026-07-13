package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OracleOfNectarsTest extends BaseCardTest {

    @Test
    @DisplayName("Activating with X=3 gains 3 life and taps the creature")
    void gainsXLife() {
        harness.setLife(player1, 20);
        Permanent oracle = addReadyOracle(player1);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, 3, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(oracle.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating with X=0 gains no life")
    void gainsNoLifeWithZero() {
        harness.setLife(player1, 20);
        addReadyOracle(player1);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    private Permanent addReadyOracle(Player player) {
        Permanent perm = new Permanent(new OracleOfNectars());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
