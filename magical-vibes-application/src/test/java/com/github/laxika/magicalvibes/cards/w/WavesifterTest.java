package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Wavesifter.class)
class WavesifterTest extends BaseCardTest {

    @Test
    @DisplayName("Hardcast Wavesifter creates two Clues and remains on the battlefield")
    void hardcastCreatesTwoClues() {
        harness.setHand(player1, List.of(new Wavesifter()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
        harness.assertOnBattlefield(player1, "Wavesifter");
    }

    @Test
    @DisplayName("Evoke Wavesifter creates two Clues and sacrifices it")
    void evokeCreatesTwoCluesAndSacrificesSelf() {
        harness.setHand(player1, List.of(new Wavesifter()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Wavesifter");
        harness.assertInGraveyard(player1, "Wavesifter");
    }
}
