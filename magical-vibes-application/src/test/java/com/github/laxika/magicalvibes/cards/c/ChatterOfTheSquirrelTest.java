package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChatterOfTheSquirrelTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Chatter of the Squirrel creates a 1/1 green Squirrel token")
    void createsSquirrelToken() {
        harness.setHand(player1, List.of(new ChatterOfTheSquirrel()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> squirrels = squirrelTokens();
        assertThat(squirrels).hasSize(1);
        assertThat(squirrels.getFirst().getCard().getPower()).isEqualTo(1);
        assertThat(squirrels.getFirst().getCard().getToughness()).isEqualTo(1);
        assertThat(squirrels.getFirst().getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(squirrels.getFirst().getCard().getSubtypes()).contains(CardSubtype.SQUIRREL);
        harness.assertInGraveyard(player1, "Chatter of the Squirrel");
    }

    @Test
    @DisplayName("Flashback creates a Squirrel token and exiles Chatter of the Squirrel")
    void flashbackCreatesSquirrelAndExilesSpell() {
        harness.setGraveyard(player1, List.of(new ChatterOfTheSquirrel()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(squirrelTokens()).hasSize(1);
        harness.assertNotInGraveyard(player1, "Chatter of the Squirrel");
        GameData gameData = harness.getGameData();
        assertThat(gameData.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Chatter of the Squirrel"));
    }

    private List<Permanent> squirrelTokens() {
        return harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Squirrel"))
                .toList();
    }
}
