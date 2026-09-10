package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KozileksSentinel.class, Ornithopter.class, GrizzlyBears.class})
class KozileksSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+0 when you cast a colorless spell")
    void pumpsWhenColorlessSpellIsCast() {
        Permanent sentinel = addSentinel();

        harness.setHand(player1, List.of(new Ornithopter()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(sentinel.getPowerModifier()).isEqualTo(1);
        assertThat(sentinel.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Does not trigger when you cast a colored spell")
    void doesNotPumpWhenColoredSpellIsCast() {
        Permanent sentinel = addSentinel();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(sentinel.getPowerModifier()).isZero();
        assertThat(sentinel.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent sentinel = addSentinel();

        harness.setHand(player1, List.of(new Ornithopter()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(sentinel.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sentinel.getPowerModifier()).isZero();
        assertThat(sentinel.getToughnessModifier()).isZero();
    }

    private Permanent addSentinel() {
        harness.addToBattlefield(player1, new KozileksSentinel());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return findPermanent(player1, "Kozilek's Sentinel");
    }
}
