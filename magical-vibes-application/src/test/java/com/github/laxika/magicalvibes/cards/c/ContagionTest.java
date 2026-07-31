package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BogImp;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContagionTest extends BaseCardTest {

    @Test
    @DisplayName("Puts both -2/-1 counters on a single target creature")
    void putsBothCountersOnOneTarget() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new Contagion()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, List.of(giant.getId()));
        harness.passBothPriorities();

        // Hill Giant (3/3) with two -2/-1 counters → -1/1.
        assertThat(giant.getCounterCount(CounterType.MINUS_TWO_MINUS_ONE)).isEqualTo(2);
        assertThat(giant.getEffectivePower()).isEqualTo(-1);
        assertThat(giant.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Distributes one -2/-1 counter on each of two target creatures")
    void distributesOneCounterEachAmongTwoTargets() {
        Permanent giant1 = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent giant2 = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new Contagion()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, List.of(giant1.getId(), giant2.getId()));
        harness.passBothPriorities();

        // Each Hill Giant (3/3) with one -2/-1 counter → 1/2.
        assertThat(giant1.getCounterCount(CounterType.MINUS_TWO_MINUS_ONE)).isEqualTo(1);
        assertThat(giant1.getEffectivePower()).isEqualTo(1);
        assertThat(giant1.getEffectiveToughness()).isEqualTo(2);
        assertThat(giant2.getEffectivePower()).isEqualTo(1);
        assertThat(giant2.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can be cast for 1 life and exiling a black card instead of its mana cost")
    void castsForAlternateCost() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new Contagion(), new BogImp()));
        int lifeBefore = gd.getLife(player1.getId());

        harness.castInstantWithAlternateExileFromHand(player1, 0, giant.getId(), 1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(giant.getCounterCount(CounterType.MINUS_TWO_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Contagion()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
