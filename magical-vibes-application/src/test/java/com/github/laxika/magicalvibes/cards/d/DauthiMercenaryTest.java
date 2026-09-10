package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.MoggFanatic;
import com.github.laxika.magicalvibes.cards.s.SoltariFootSoldier;
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

@CardUsed({DauthiMercenary.class, MoggFanatic.class, SoltariFootSoldier.class})
class DauthiMercenaryTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants +1/+0 without tapping")
    void abilityGrantsPower() {
        Permanent mercenary = addCreatureReady(player1, new DauthiMercenary());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mercenary)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mercenary)).isEqualTo(1);
        assertThat(mercenary.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Ability can be activated multiple times and boosts stack")
    void boostsStack() {
        Permanent mercenary = addCreatureReady(player1, new DauthiMercenary());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mercenary)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent mercenary = addCreatureReady(player1, new DauthiMercenary());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, mercenary)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mercenary)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability can be activated while the creature has summoning sickness")
    void abilityCanBeActivatedWithSummoningSickness() {
        Permanent mercenary = harness.addToBattlefieldAndReturn(player1, new DauthiMercenary());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mercenary)).isEqualTo(3);
        assertThat(mercenary.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Shadow prevents a non-shadow creature from blocking")
    void shadowPreventsNonShadowBlocker() {
        Permanent attacker = addCreatureReady(player1, new DauthiMercenary());
        attacker.setAttacking(true);
        addCreatureReady(player2, new MoggFanatic());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Shadow allows a creature with shadow to block")
    void shadowAllowsShadowBlocker() {
        Permanent attacker = addCreatureReady(player1, new DauthiMercenary());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new SoltariFootSoldier());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
