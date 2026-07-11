package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SwellOfCourageTest extends BaseCardTest {

    @Test
    @DisplayName("Cast as an instant boosts all creatures you control +2/+2")
    void spellBoostsOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SwellOfCourage()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        for (Permanent p : gd.playerBattlefields.get(player1.getId())) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(p.getEffectivePower()).isEqualTo(4);
                assertThat(p.getEffectiveToughness()).isEqualTo(4);
            }
        }
        for (Permanent p : gd.playerBattlefields.get(player2.getId())) {
            if (p.getCard().hasType(CardType.CREATURE)) {
                assertThat(p.getEffectivePower()).isEqualTo(2);
                assertThat(p.getEffectiveToughness()).isEqualTo(2);
            }
        }
    }

    @Test
    @DisplayName("Spell boost wears off at cleanup")
    void spellBoostWearsOff() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SwellOfCourage()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Reinforce X puts X +1/+1 counters on target creature")
    void reinforcePutsXCounters() {
        harness.setHand(player1, List.of(new SwellOfCourage()));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        // Reinforce 3—{3}{W}{W}
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, bears.getId(), 3);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(bears.getEffectivePower()).isEqualTo(5);
        assertThat(bears.getEffectiveToughness()).isEqualTo(5);
        harness.assertInGraveyard(player1, "Swell of Courage");
    }

    @Test
    @DisplayName("Reinforce cannot target a non-creature; no cost is paid")
    void reinforceRejectsNonCreature() {
        harness.setHand(player1, List.of(new SwellOfCourage()));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, land.getId(), 3))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Swell of Courage");
        assertThat(gd.stack).isEmpty();
    }
}
