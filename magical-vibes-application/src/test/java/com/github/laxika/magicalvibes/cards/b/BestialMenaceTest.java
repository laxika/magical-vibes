package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BestialMenaceTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Bestial Menace creates a 1/1 Snake, a 2/2 Wolf, and a 3/3 Elephant")
    void createsSnakeWolfAndElephantTokens() {
        harness.setHand(player1, List.of(new BestialMenace()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(3);
        assertThat(tokens).anySatisfy(token -> assertToken(token, "Snake", 1, 1, CardSubtype.SNAKE));
        assertThat(tokens).anySatisfy(token -> assertToken(token, "Wolf", 2, 2, CardSubtype.WOLF));
        assertThat(tokens).anySatisfy(token -> assertToken(token, "Elephant", 3, 3, CardSubtype.ELEPHANT));
        harness.assertInGraveyard(player1, "Bestial Menace");
    }

    private void assertToken(Permanent token, String name, int power, int toughness, CardSubtype subtype) {
        assertThat(token.getCard().getName()).isEqualTo(name);
        assertThat(token.getCard().getPower()).isEqualTo(power);
        assertThat(token.getCard().getToughness()).isEqualTo(toughness);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getSubtypes()).contains(subtype);
    }
}
