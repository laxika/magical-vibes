package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArwenMortalQueen.class, GrizzlyBears.class})
class ArwenMortalQueenTest extends BaseCardTest {

    @Test
    @DisplayName("Arwen enters with an indestructible counter")
    void entersWithIndestructibleCounter() {
        Permanent arwen = castArwen();

        assertThat(arwen.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, arwen, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Arwen removes an indestructible counter to strengthen another creature")
    void activatesOnAnotherCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent arwen = addCreatureReady(player1, new ArwenMortalQueen());
        arwen.setCounterCount(CounterType.INDESTRUCTIBLE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(arwen), null, target.getId());
        harness.passBothPriorities();

        assertThat(arwen.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        assertThat(arwen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(arwen.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The granted indestructible keyword expires while counters remain")
    void grantedIndestructibleExpiresAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent arwen = addCreatureReady(player1, new ArwenMortalQueen());
        arwen.setCounterCount(CounterType.INDESTRUCTIBLE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(arwen), null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Arwen cannot target herself or activate without an indestructible counter")
    void enforcesCounterCostAndAnotherCreatureTarget() {
        Permanent arwen = addCreatureReady(player1, new ArwenMortalQueen());
        arwen.setCounterCount(CounterType.INDESTRUCTIBLE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(arwen), null, arwen.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature");

        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        arwen.setCounterCount(CounterType.INDESTRUCTIBLE, 0);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(arwen), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent castArwen() {
        harness.setHand(player1, List.of(new ArwenMortalQueen()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Arwen, Mortal Queen");
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
