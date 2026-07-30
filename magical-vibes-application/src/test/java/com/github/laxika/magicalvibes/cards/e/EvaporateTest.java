package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EvaporateTest extends BaseCardTest {

    private void castEvaporate() {
        harness.setHand(player1, List.of(new Evaporate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Evaporate kills white and blue 1/1s on both sides")
    void damagesWhiteAndBlueCreatures() {
        harness.addToBattlefield(player1, new SuntailHawk());    // white 1/1
        harness.addToBattlefield(player2, new FugitiveWizard()); // blue 1/1

        castEvaporate();

        harness.assertNotOnBattlefield(player1, "Suntail Hawk");
        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Evaporate leaves creatures that are neither white nor blue alone")
    void sparesOtherColorsAndColorless() {
        harness.addToBattlefield(player1, new LlanowarElves()); // green 1/1
        harness.addToBattlefield(player2, new Ornithopter());   // colorless 0/2

        castEvaporate();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Evaporate deals no damage to players")
    void doesNotDamagePlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castEvaporate();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
