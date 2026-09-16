package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ChatterOfTheSquirrel.class)
class ChatterOfTheSquirrelTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Chatter of the Squirrel creates a 1/1 green Squirrel token")
    void createsSquirrelToken() {
        harness.setHand(player1, List.of(new ChatterOfTheSquirrel()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        List<Permanent> squirrels = squirrelTokens();
        assertThat(squirrels).hasSize(1);
        assertThat(squirrels.getFirst().getCard().getPower()).isEqualTo(1);
        assertThat(squirrels.getFirst().getCard().getToughness()).isEqualTo(1);
        assertThat(squirrels.getFirst().getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(squirrels.getFirst().getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(squirrels.getFirst().getCard().getSubtypes()).contains(CardSubtype.SQUIRREL);
        harness.assertInGraveyard(player1, "Chatter of the Squirrel");
    }

    @Test
    @DisplayName("Flashback creates a Squirrel token and exiles Chatter of the Squirrel")
    void flashbackCreatesSquirrelAndExilesSpell() {
        harness.setGraveyard(player1, List.of(new ChatterOfTheSquirrel()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(squirrelTokens()).hasSize(1);
        harness.assertNotInGraveyard(player1, "Chatter of the Squirrel");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Chatter of the Squirrel"));
    }

    @Test
    @DisplayName("Flashback requires generic mana in addition to green mana")
    void flashbackRequiresGenericMana() {
        harness.setGraveyard(player1, List.of(new ChatterOfTheSquirrel()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback requires green mana in addition to generic mana")
    void flashbackRequiresGreenMana() {
        harness.setGraveyard(player1, List.of(new ChatterOfTheSquirrel()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private List<Permanent> squirrelTokens() {
        return findPermanents(player1, "Squirrel").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
