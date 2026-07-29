package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReignOfTerrorTest extends BaseCardTest {

    private void castReign(int mode) {
        harness.setHand(player1, List.of(new ReignOfTerror()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 20);
        harness.castSorcery(player1, 0, mode);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Green mode destroys only green creatures and costs 2 life each")
    void greenMode() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new EliteVanguard());

        castReign(0);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Elite Vanguard");

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("White mode destroys only white creatures and costs 2 life each")
    void whiteMode() {
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player2, new EliteVanguard());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castReign(1);

        harness.assertNotOnBattlefield(player1, "Suntail Hawk");
        harness.assertNotOnBattlefield(player2, "Elite Vanguard");
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("No matching creatures means no life loss")
    void noCreaturesNoLifeLoss() {
        harness.addToBattlefield(player2, new EliteVanguard());

        castReign(0);

        harness.assertOnBattlefield(player2, "Elite Vanguard");
        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
