package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefenderEnVecTest extends BaseCardTest {

    @Test
    @DisplayName("Defender en-Vec enters with four fade counters")
    void entersWithFadeCounters() {
        harness.setHand(player1, List.of(new DefenderEnVec()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent defender = findPermanent(player1, "Defender en-Vec");
        assertThat(defender.getCounterCount(CounterType.FADE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Fading removes one fade counter during its controller's upkeep")
    void removesFadeCounterAtUpkeep() {
        Permanent defender = addCreatureReady(player1, new DefenderEnVec());
        defender.setCounterCount(CounterType.FADE, 2);

        advanceToUpkeep(player1);

        assertThat(defender.getCounterCount(CounterType.FADE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Defender en-Vec");
    }

    @Test
    @DisplayName("Fading sacrifices the creature when it has no fade counters")
    void sacrificesWithoutFadeCounters() {
        addCreatureReady(player1, new DefenderEnVec());

        advanceToUpkeep(player1);

        harness.assertNotOnBattlefield(player1, "Defender en-Vec");
    }

    @Test
    @DisplayName("Removing a fade counter prevents the next 2 damage to any target")
    void removesCounterAndPreventsDamage() {
        Permanent defender = addCreatureReady(player1, new DefenderEnVec());
        defender.setCounterCount(CounterType.FADE, 1);
        addCreatureReady(player1, new ProdigalPyromancer());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(defender.getCounterCount(CounterType.FADE)).isZero();
        assertThat(defender.getDamagePreventionShield()).isEqualTo(2);

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The prevention ability cannot target a land")
    void cannotTargetLand() {
        addCreatureReady(player1, new DefenderEnVec());
        Permanent forest = addCreatureReady(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
