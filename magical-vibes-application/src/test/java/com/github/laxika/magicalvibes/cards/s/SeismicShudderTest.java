package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SeismicShudderTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to each creature without flying")
    void damagesOnlyCreaturesWithoutFlying() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new SeismicShudder()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fugitive Wizard");
        harness.assertOnBattlefield(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Does not damage players")
    void doesNotDamagePlayers() {
        harness.setHand(player1, List.of(new SeismicShudder()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gameData.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
