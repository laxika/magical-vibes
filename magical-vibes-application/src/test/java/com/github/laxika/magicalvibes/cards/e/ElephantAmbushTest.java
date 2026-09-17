package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ElephantAmbush.class)
class ElephantAmbushTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Elephant Ambush creates a 3/3 green Elephant token")
    void createsElephantToken() {
        harness.castFromHand(player1, new ElephantAmbush(), "{2}{G}{G}");

        harness.passBothPriorities();

        List<Permanent> elephants = elephantTokens();
        assertThat(elephants).hasSize(1);
        assertThat(elephants.getFirst().getCard().getPower()).isEqualTo(3);
        assertThat(elephants.getFirst().getCard().getToughness()).isEqualTo(3);
        assertThat(elephants.getFirst().getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(elephants.getFirst().getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(elephants.getFirst().getCard().getSubtypes()).contains(CardSubtype.ELEPHANT);
        harness.assertInGraveyard(player1, "Elephant Ambush");
    }

    @Test
    @DisplayName("Flashback creates an Elephant token and exiles Elephant Ambush")
    void flashbackCreatesElephantAndExilesSpell() {
        harness.setGraveyard(player1, List.of(new ElephantAmbush()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(elephantTokens()).hasSize(1);
        harness.assertNotInGraveyard(player1, "Elephant Ambush");
        GameData gameData = harness.getGameData();
        assertThat(gameData.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Elephant Ambush"));
    }

    @Test
    @DisplayName("Flashback requires all six generic mana")
    void flashbackRequiresSixGenericMana() {
        harness.setGraveyard(player1, List.of(new ElephantAmbush()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback requires two green mana")
    void flashbackRequiresTwoGreenMana() {
        harness.setGraveyard(player1, List.of(new ElephantAmbush()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private List<Permanent> elephantTokens() {
        return harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Elephant"))
                .toList();
    }
}
