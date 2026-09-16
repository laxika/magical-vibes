package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(CrushOfWurms.class)
class CrushOfWurmsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Crush of Wurms creates three 6/6 green Wurm tokens")
    void createsWurmTokens() {
        harness.setHand(player1, List.of(new CrushOfWurms()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Wurm")).hasSize(3);
        assertThat(findPermanents(player1, "Wurm")).allSatisfy(wurm -> {
            assertThat(wurm.getCard().getPower()).isEqualTo(6);
            assertThat(wurm.getCard().getToughness()).isEqualTo(6);
            assertThat(wurm.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(wurm.getCard().getSubtypes()).contains(CardSubtype.WURM);
        });
        harness.assertInGraveyard(player1, "Crush of Wurms");
    }

    @Test
    @DisplayName("Flashback creates three Wurm tokens and exiles Crush of Wurms")
    void flashbackCreatesWurmTokensAndExilesSpell() {
        harness.setGraveyard(player1, List.of(new CrushOfWurms()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Wurm")).hasSize(3);
        harness.assertNotInGraveyard(player1, "Crush of Wurms");
        GameData gameData = harness.getGameData();
        assertThat(gameData.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Crush of Wurms"));
    }

    @Test
    @DisplayName("Flashback requires the full flashback cost")
    void flashbackFailsWithoutFullCost() {
        harness.setGraveyard(player1, List.of(new CrushOfWurms()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Crush of Wurms");
        assertThat(findPermanents(player1, "Wurm")).isEmpty();
    }
}
