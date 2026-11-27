package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.PlayersCantLoseLifeThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ZombieCutthroat.class)
class ZombieCutthroatTest extends BaseCardTest {

    @Test
    @DisplayName("Turns face up by paying 5 life")
    void turnsFaceUpByPayingFiveLife() {
        Permanent cutthroat = castFaceDown();
        harness.setLife(player1, 10);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cutthroat));

        assertThat(cutthroat.isFaceDown()).isFalse();
        harness.assertLife(player1, 5);
    }

    @Test
    @DisplayName("Can't turn face up with less than 5 life")
    void cannotTurnFaceUpWithoutFiveLife() {
        Permanent cutthroat = castFaceDown();
        harness.setLife(player1, 4);

        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(cutthroat)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");

        assertThat(cutthroat.isFaceDown()).isTrue();
        harness.assertLife(player1, 4);
    }

    @Test
    @DisplayName("Can pay exactly 5 life to turn face up")
    void canPayExactlyFiveLifeToTurnFaceUp() {
        Permanent cutthroat = castFaceDown();
        harness.setLife(player1, 5);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cutthroat));

        assertThat(cutthroat.isFaceDown()).isFalse();
        harness.assertLife(player1, 0);
    }

    @Test
    @DisplayName("Turning face up needs no mana and does not use the stack")
    void turningFaceUpNeedsNoManaAndDoesNotUseTheStack() {
        Permanent cutthroat = castFaceDown();
        harness.setLife(player1, 10);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.stack).isEmpty();

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(cutthroat));

        assertThat(cutthroat.isFaceDown()).isFalse();
        harness.assertLife(player1, 5);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Casting face down neither requires nor pays life")
    void castingFaceDownDoesNotRequireOrPayLife() {
        harness.setLife(player1, 4);

        Permanent cutthroat = castFaceDown();

        assertThat(cutthroat.isFaceDown()).isTrue();
        harness.assertLife(player1, 4);
    }

    @Test
    @DisplayName("Casting it face up does not pay the morph life cost")
    void castingNormallyDoesNotPayTheMorphLifeCost() {
        harness.setLife(player1, 4);
        harness.castFromHand(player1, new ZombieCutthroat(), "{3}{B}{B}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Zombie Cutthroat").isFaceDown()).isFalse();
        harness.assertLife(player1, 4);
    }

    @Test
    @DisplayName("A player who can't lose life can't pay the morph life cost, so it stays face down")
    void cannotTurnFaceUpWhileUnableToLoseLife() {
        Permanent cutthroat = castFaceDown();
        harness.setLife(player1, 10);
        gd.addFloatingEffect(FloatingContinuousEffect.onPlayer("Test", null, player1.getId(),
                new PlayersCantLoseLifeThisTurnEffect(Subjects.you()), player1.getId(), Duration.UNTIL_END_OF_TURN));

        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(cutthroat)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot pay life");

        assertThat(cutthroat.isFaceDown()).isTrue();
        harness.assertLife(player1, 10);
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new ZombieCutthroat()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Zombie Cutthroat");
    }
}
