package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.IvoryMask;
import com.github.laxika.magicalvibes.cards.o.OboroPalaceInTheClouds;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeedTheLand.class, OboroPalaceInTheClouds.class, SakuraTribeScout.class, IvoryMask.class})
class SeedTheLandTest extends BaseCardTest {

    @Test
    @DisplayName("A land entering under your control creates a Snake token for you")
    void ownLandCreatesSnake() {
        harness.addToBattlefield(player1, new SeedTheLand());
        harness.setHand(player1, List.of(new OboroPalaceInTheClouds()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent snake = findPermanent(player1, "Snake");
        assertThat(snake.getCard().isToken()).isTrue();
        assertThat(snake.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(snake.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(snake.getCard().getSubtypes()).containsExactly(CardSubtype.SNAKE);
        assertThat(snake.getEffectivePower()).isEqualTo(1);
        assertThat(snake.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("A land entering under an opponent's control creates a Snake token for that player")
    void opponentsLandCreatesSnakeForOpponent() {
        harness.addToBattlefield(player1, new SeedTheLand());
        harness.setHand(player2, List.of(new OboroPalaceInTheClouds()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Snake")).singleElement()
                .satisfies(snake -> {
                    assertThat(snake.getCard().isToken()).isTrue();
                    assertThat(snake.getCard().getType()).isEqualTo(CardType.CREATURE);
                    assertThat(snake.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(snake.getCard().getSubtypes()).containsExactly(CardSubtype.SNAKE);
                    assertThat(snake.getEffectivePower()).isEqualTo(1);
                    assertThat(snake.getEffectiveToughness()).isEqualTo(1);
                });
        assertThat(findPermanents(player1, "Snake")).isEmpty();
    }

    @Test
    @DisplayName("A shrouded opponent still creates a Snake when their land enters")
    void opponentsLandCreatesSnakeForShroudedOpponent() {
        harness.addToBattlefield(player1, new SeedTheLand());
        harness.addToBattlefield(player2, new IvoryMask());
        harness.setHand(player2, List.of(new OboroPalaceInTheClouds()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Snake")).hasSize(1);
    }

    @Test
    @DisplayName("A nonland permanent entering does not create a Snake token")
    void nonlandDoesNotCreateSnake() {
        harness.addToBattlefield(player1, new SeedTheLand());
        harness.castFromHand(player1, new SakuraTribeScout(), "{G}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Snake")).isEmpty();
    }

    @Test
    void landPutOntoBattlefieldByAbilityCreatesSnake() {
        harness.addToBattlefield(player1, new SeedTheLand());
        addCreatureReady(player1, new SakuraTribeScout());
        harness.setHand(player1, List.of(new OboroPalaceInTheClouds()));

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Snake")).hasSize(1);
        assertThat(findPermanents(player2, "Snake")).isEmpty();
    }

    @Test
    void eachEnchantmentCreatesSnakeForLandController() {
        harness.addToBattlefield(player1, new SeedTheLand());
        harness.addToBattlefield(player2, new SeedTheLand());
        harness.setHand(player1, List.of(new OboroPalaceInTheClouds()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Snake")).hasSize(2);
        assertThat(findPermanents(player2, "Snake")).isEmpty();
    }

    @Test
    void ownLandChangingControllerBeforeResolutionCreatesSnakeForNewController() {
        harness.addToBattlefield(player1, new SeedTheLand());
        harness.setHand(player1, List.of(new OboroPalaceInTheClouds()));
        harness.playLand(player1, 0);

        Permanent land = findPermanent(player1, "Oboro, Palace in the Clouds");
        gd.playerBattlefields.get(player1.getId()).remove(land);
        gd.playerBattlefields.get(player2.getId()).add(land);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Snake")).isEmpty();
        assertThat(findPermanents(player2, "Snake")).hasSize(1);
    }

    @Test
    void opponentsLandChangingControllerBeforeResolutionCreatesSnakeForNewController() {
        harness.addToBattlefield(player1, new SeedTheLand());
        harness.setHand(player2, List.of(new OboroPalaceInTheClouds()));
        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);

        Permanent land = findPermanent(player2, "Oboro, Palace in the Clouds");
        gd.playerBattlefields.get(player2.getId()).remove(land);
        gd.playerBattlefields.get(player1.getId()).add(land);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Snake")).hasSize(1);
        assertThat(findPermanents(player2, "Snake")).isEmpty();
    }
}
