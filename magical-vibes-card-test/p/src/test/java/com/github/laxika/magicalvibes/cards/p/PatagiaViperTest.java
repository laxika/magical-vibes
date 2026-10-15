package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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
        castPatagiaViperWithBlueMana();

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
        harness.castFromHand(player1, new PatagiaViper(), "{3}{G}");
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Snake")).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Patagia Viper");
        harness.assertInGraveyard(player1, "Patagia Viper");
    }

    @Test
    @DisplayName("Token creation and sacrifice are separate triggered abilities")
    void queuesTwoSeparateEnterTriggers() {
        harness.enterBattlefieldAndReturn(player1, new PatagiaViper());

        assertThat(gd.stack).hasSize(2);
        harness.assertOnBattlefield(player1, "Patagia Viper");
        assertThat(findPermanents(player1, "Snake")).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast creates tokens and sacrifices the Viper")
    void sacrificesItselfWhenNotCast() {
        harness.enterBattlefieldAndReturn(player1, new PatagiaViper());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Snake")).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Patagia Viper");
        harness.assertInGraveyard(player1, "Patagia Viper");
    }

    @Test
    @DisplayName("An opposing Viper creates tokens for its controller")
    void createsTokensForOpposingController() {
        harness.enterBattlefieldAndReturn(player2, new PatagiaViper());
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Snake")).hasSize(2);
        assertThat(findPermanents(player1, "Snake")).isEmpty();
        harness.assertNotOnBattlefield(player2, "Patagia Viper");
        harness.assertInGraveyard(player2, "Patagia Viper");
    }

    private void castPatagiaViperWithBlueMana() {
        harness.castFromHand(player1, new PatagiaViper(), "{2}{G}{U}");

        resolveAllTriggers();
    }
}
