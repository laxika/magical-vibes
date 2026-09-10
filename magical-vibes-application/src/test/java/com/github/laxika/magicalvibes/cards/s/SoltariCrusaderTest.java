package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.k.KnightOfDawn;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoltariCrusader.class, KnightOfDawn.class, SoltariFootSoldier.class})
class SoltariCrusaderTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants +1/+0 without tapping")
    void abilityBoostsPower() {
        Permanent crusader = addCreatureReady(player1, new SoltariCrusader());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(1);
        assertThat(crusader.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Ability can be activated multiple times")
    void abilityStacks() {
        Permanent crusader = addCreatureReady(player1, new SoltariCrusader());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent crusader = addCreatureReady(player1, new SoltariCrusader());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(2);
    }

    @Test
    @DisplayName("Shadow prevents a non-shadow creature from blocking")
    void shadowPreventsNonShadowCreatureFromBlocking() {
        Permanent crusader = addCreatureReady(player1, new SoltariCrusader());
        crusader.setAttacking(true);
        addCreatureReady(player2, new KnightOfDawn());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    @DisplayName("Shadow allows a shadow creature to block")
    void shadowAllowsShadowCreatureToBlock() {
        Permanent crusader = addCreatureReady(player1, new SoltariCrusader());
        crusader.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SoltariFootSoldier());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
