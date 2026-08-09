package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SosukesSummonsTest extends BaseCardTest {

    private void prepareMain(Player active) {
        harness.forceActivePlayer(active);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Casting creates two 1/1 green Snake creature tokens")
    void createsTwoSnakeTokens() {
        prepareMain(player1);
        harness.setHand(player1, List.of(new SosukesSummons()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        List<Permanent> snakes = findPermanents(player1, "Snake");
        assertThat(snakes).hasSize(2);
        assertThat(snakes).allSatisfy(snake -> {
            assertThat(snake.getCard().isToken()).isTrue();
            assertThat(snake.getCard().getSubtypes()).containsExactly(CardSubtype.SNAKE);
            assertThat(snake.getEffectivePower()).isEqualTo(1);
            assertThat(snake.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("A nontoken Snake entering lets you return Sosuke's Summons from your graveyard")
    void nontokenSnakeReturnsSummons() {
        SosukesSummons summons = new SosukesSummons();
        harness.setGraveyard(player1, List.of(summons));
        prepareMain(player1);

        harness.setHand(player1, List.of(new SkeletalSnake()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(summons.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(summons.getId()));
    }

    @Test
    @DisplayName("Snake tokens do not trigger the graveyard ability")
    void snakeTokensDoNotTrigger() {
        SosukesSummons summonsInGraveyard = new SosukesSummons();
        harness.setGraveyard(player1, List.of(summonsInGraveyard));
        prepareMain(player1);

        harness.setHand(player1, List.of(new SosukesSummons()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(summonsInGraveyard.getId()));
    }
}
