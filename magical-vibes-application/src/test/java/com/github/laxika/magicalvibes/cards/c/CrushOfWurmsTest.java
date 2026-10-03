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

@CardUsed({CrushOfWurms.class})
class CrushOfWurmsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Crush of Wurms creates three 6/6 green Wurm tokens")
    void createsWurmTokens() {
        harness.castFromHand(player1, new CrushOfWurms(), "{6}{G}{G}{G}");
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

    @Test
    @DisplayName("Flashback cannot be cast without its full flashback cost")
    void flashbackRequiresFullCost() {
        CrushOfWurms crushOfWurms = new CrushOfWurms();
        harness.setGraveyard(player1, List.of(crushOfWurms));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(crushOfWurms);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The same card can create six Wurms by being cast normally and then flashed back")
    void normalCastThenFlashbackCreatesSixWurms() {
        CrushOfWurms card = new CrushOfWurms();
        harness.castFromHand(player1, card, "{6}{G}{G}{G}");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Crush of Wurms");

        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 9);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Wurm")).hasSize(6);
        assertThat(findPermanents(player2, "Wurm")).isEmpty();
        harness.assertNotInGraveyard(player1, "Crush of Wurms");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(card);
    }

    @Test
    @DisplayName("Flashback cannot replace a required green mana with colorless mana")
    void flashbackRequiresThreeGreenMana() {
        CrushOfWurms card = new CrushOfWurms();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 10);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Wurm")).isEmpty();
    }
}
