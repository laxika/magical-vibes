package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ArmoredGriffin;
import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.p.PlatedWurm;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DakmorPlague.class, ArmoredGriffin.class, BearCub.class, PlatedWurm.class})
class DakmorPlagueTest extends BaseCardTest {

    private void castDakmorPlague() {
        harness.castFromHand(player1, new DakmorPlague(), "{3}{B}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Dakmor Plague deals 3 damage to each player")
    void dealsThreeToEachPlayer() {
        castDakmorPlague();

        GameData gd = harness.getGameData();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Dakmor Plague kills creatures with 3 or less toughness but not tougher ones")
    void killsSmallCreatures() {
        harness.addToBattlefield(player1, new BearCub());
        harness.addToBattlefield(player2, new PlatedWurm());

        castDakmorPlague();

        harness.assertNotOnBattlefield(player1, "Bear Cub");
        harness.assertOnBattlefield(player2, "Plated Wurm");
    }

    @Test
    @DisplayName("Dakmor Plague kills creatures with exactly 3 toughness")
    void killsCreaturesWithExactlyThreeToughness() {
        harness.addToBattlefield(player2, new ArmoredGriffin());

        castDakmorPlague();

        harness.assertNotOnBattlefield(player2, "Armored Griffin");
    }

    @Test
    @DisplayName("Dakmor Plague can kill the caster")
    void canKillCaster() {
        harness.setLife(player1, 3);

        castDakmorPlague();

        GameData gd = harness.getGameData();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }
}
