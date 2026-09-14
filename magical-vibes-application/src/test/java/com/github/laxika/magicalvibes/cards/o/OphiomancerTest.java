package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.s.SkeletalSnake;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Ophiomancer.class, SkeletalSnake.class})
class OphiomancerTest extends BaseCardTest {

    private List<Permanent> snakeTokens(Player player) {
        return findPermanents(player, "Snake").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }

    @Test
    @DisplayName("Creates a 1/1 black Snake token with deathtouch during each upkeep")
    void createsSnakeDuringEachUpkeep() {
        harness.addToBattlefield(player1, new Ophiomancer());

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        List<Permanent> tokens = snakeTokens(player1);
        assertThat(tokens).hasSize(1);
        Permanent snake = tokens.getFirst();
        assertThat(snake.getCard().getPower()).isEqualTo(1);
        assertThat(snake.getCard().getToughness()).isEqualTo(1);
        assertThat(snake.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(snake.getCard().getSubtypes()).containsExactly(CardSubtype.SNAKE);
        assertThat(snake.getCard().getKeywords()).contains(Keyword.DEATHTOUCH);

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(snakeTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when the controller already controls a Snake")
    void doesNotTriggerWhenControllerControlsSnake() {
        harness.addToBattlefield(player1, new Ophiomancer());
        harness.addToBattlefield(player1, new SkeletalSnake());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(snakeTokens(player1)).isEmpty();
    }

    @Test
    @DisplayName("Rechecks the no-Snake condition when the trigger resolves")
    void rechecksConditionAtResolution() {
        harness.addToBattlefield(player1, new Ophiomancer());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.addToBattlefield(player1, new SkeletalSnake());
        harness.passBothPriorities();

        assertThat(snakeTokens(player1)).isEmpty();
    }
}
