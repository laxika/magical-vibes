package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PatagiaViper.class})
class PatagiaViperTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two green and blue Snake tokens and remains when blue mana was spent")
    void createsTokensAndRemainsWhenBlueManaWasSpent() {
        castPatagiaViper(ManaColor.BLUE);

        List<Permanent> snakes = findPermanents(player1, "Snake");
        assertThat(snakes).hasSize(2);
        assertThat(snakes).allSatisfy(snake -> {
            assertThat(snake.getCard().getColors())
                    .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.BLUE);
            assertThat(snake.getCard().getSubtypes()).containsExactly(CardSubtype.SNAKE);
            assertThat(snake.getEffectivePower()).isEqualTo(1);
            assertThat(snake.getEffectiveToughness()).isEqualTo(1);
        });
        harness.assertOnBattlefield(player1, "Patagia Viper");
    }

    @Test
    @DisplayName("Creates the Snake tokens but sacrifices itself when blue mana was not spent")
    void sacrificesItselfWithoutBlueMana() {
        castPatagiaViper(ManaColor.GREEN);

        assertThat(findPermanents(player1, "Snake")).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Patagia Viper");
        harness.assertInGraveyard(player1, "Patagia Viper");
    }

    private void castPatagiaViper(ManaColor coloredGenericMana) {
        harness.setHand(player1, List.of(new PatagiaViper()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, coloredGenericMana, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
