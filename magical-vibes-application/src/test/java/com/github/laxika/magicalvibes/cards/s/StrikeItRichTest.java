package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(StrikeItRich.class)
class StrikeItRichTest extends BaseCardTest {

    @Test
    @DisplayName("Strike It Rich creates a Treasure token")
    void createsTreasureToken() {
        harness.setHand(player1, List.of(new StrikeItRich()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        harness.assertInGraveyard(player1, "Strike It Rich");
    }

    @Test
    @DisplayName("Flashback creates a Treasure token and exiles Strike It Rich")
    void flashbackCreatesTreasureAndExilesSpell() {
        harness.setGraveyard(player1, List.of(new StrikeItRich()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        harness.assertNotInGraveyard(player1, "Strike It Rich");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Strike It Rich"));
    }

    @Test
    @DisplayName("Flashback requires both generic and red mana")
    void flashbackRequiresBothManaTypes() {
        harness.setGraveyard(player1, List.of(new StrikeItRich()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
