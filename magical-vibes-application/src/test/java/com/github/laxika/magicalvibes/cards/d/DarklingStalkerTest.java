package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DarklingStalker.class})
class DarklingStalkerTest extends BaseCardTest {

    @Test
    @DisplayName("First ability grants a regeneration shield")
    void regenerateAbilityGrantsShield() {
        Permanent stalker = addCreatureReady(player1, new DarklingStalker());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(stalker.getRegenerationShield()).isEqualTo(1);
        assertThat(stalker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Regeneration shield saves Darkling Stalker from lethal combat damage")
    void regenerationShieldSavesFromLethalCombatDamage() {
        Permanent stalker = addCreatureReady(player1, new DarklingStalker());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        stalker.setBlocking(true);
        stalker.addBlockingTarget(0);
        Permanent attacker = addCreatureReady(player2, new DarklingStalker());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Darkling Stalker");
        assertThat(stalker.isTapped()).isTrue();
        assertThat(stalker.isBlocking()).isFalse();
        assertThat(stalker.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Second ability pumps +1/+1 and can be activated repeatedly")
    void pumpAbilityStacks() {
        Permanent stalker = addCreatureReady(player1, new DarklingStalker());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, stalker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, stalker)).isEqualTo(3);
        assertThat(stalker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent stalker = addCreatureReady(player1, new DarklingStalker());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, stalker)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, stalker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, stalker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Both abilities work while tapped and summoning sick")
    void abilitiesWorkWhileTappedAndSummoningSick() {
        Permanent stalker = harness.addToBattlefieldAndReturn(player1, new DarklingStalker());
        stalker.setSummoningSick(true);
        stalker.tap();
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(stalker.getRegenerationShield()).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, stalker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, stalker)).isEqualTo(2);
        assertThat(stalker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Neither ability can use colorless mana to pay its black cost")
    void abilitiesRequireBlackMana() {
        addCreatureReady(player1, new DarklingStalker());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Unused regeneration shields expire at end of turn")
    void unusedRegenerationShieldsExpire() {
        Permanent stalker = addCreatureReady(player1, new DarklingStalker());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(stalker.getRegenerationShield()).isEqualTo(2);
        assertThat(stalker.isTapped()).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(stalker.getRegenerationShield()).isZero();
        harness.assertOnBattlefield(player1, "Darkling Stalker");
    }
}
