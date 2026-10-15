package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

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

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void treasureCanImmediatelyBeSacrificedForAnyColor(ManaColor color) {
        harness.setHand(player1, List.of(new StrikeItRich()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canCastFromHandThenFlashbackTheSameCard() {
        StrikeItRich card = new StrikeItRich();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        harness.assertNotInGraveyard(player1, "Strike It Rich");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void flashbackCannotBePaidWithOnlyGenericMana() {
        harness.setGraveyard(player1, List.of(new StrikeItRich()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Strike It Rich");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
