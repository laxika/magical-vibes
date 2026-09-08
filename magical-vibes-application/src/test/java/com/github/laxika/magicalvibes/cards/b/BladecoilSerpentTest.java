package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BladecoilSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Draws one card for each pair of blue mana spent")
    void drawsForBlueManaPairs() {
        harness.setHand(player1, List.of(new BladecoilSerpent()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GiantGrowth(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each opponent discards one card for one pair of black mana")
    void eachOpponentDiscardsForBlackManaPairs() {
        harness.setHand(player1, List.of(new BladecoilSerpent()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Gets +1/+0 and trample and haste for each pair of red mana")
    void getsRedManaPairBonusUntilEndOfTurn() {
        harness.setHand(player1, List.of(new BladecoilSerpent()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent serpent = findPermanent(player1, "Bladecoil Serpent");
        assertThat(serpent.getEffectivePower()).isEqualTo(5);

        harness.passBothPriorities();

        assertThat(serpent.getEffectivePower()).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, serpent, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, serpent, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("One colored mana does not count as a pair")
    void oneColoredManaDoesNotCountAsPair() {
        harness.setHand(player1, List.of(new BladecoilSerpent()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent serpent = findPermanent(player1, "Bladecoil Serpent");
        assertThat(serpent.getEffectivePower()).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, serpent, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, serpent, Keyword.HASTE)).isFalse();
    }
}
