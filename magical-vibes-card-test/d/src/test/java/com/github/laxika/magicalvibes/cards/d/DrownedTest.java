package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(Drowned.class)
class DrownedTest extends BaseCardTest {

    @Test
    @DisplayName("Its activated ability grants a regeneration shield")
    void activatedAbilityGrantsRegenerationShield() {
        Permanent drowned = addCreatureReady(player1, new Drowned());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(drowned.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Its non-tap ability works while it has summoning sickness")
    void nonTapAbilityWorksWhileSummoningSick() {
        Permanent drowned = harness.addToBattlefieldAndReturn(player1, new Drowned());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(drowned.getRegenerationShield()).isEqualTo(1);
        assertThat(drowned.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Its regeneration shield saves it from lethal combat damage")
    void regenerationShieldSavesDrowned() {
        Permanent drowned = addCreatureReady(player1, new Drowned());
        drowned.setRegenerationShield(1);
        drowned.setBlocking(true);
        drowned.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new Drowned());
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(drowned);
        assertThat(drowned.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Its non-tap ability works while tapped")
    void abilityWorksWhileTapped() {
        Permanent drowned = addCreatureReady(player1, new Drowned());
        drowned.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(drowned.getRegenerationShield()).isEqualTo(1);
        assertThat(drowned.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Repeated activations create separate shields only when they resolve")
    void repeatedActivationsCreateSeparateShields() {
        Permanent drowned = addCreatureReady(player1, new Drowned());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(drowned.getRegenerationShield()).isZero();
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(drowned.getRegenerationShield()).isEqualTo(2);
        assertThat(drowned.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Its ability cannot be activated without paying mana")
    void abilityRequiresMana() {
        Permanent drowned = addCreatureReady(player1, new Drowned());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(drowned.getRegenerationShield()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
