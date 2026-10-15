package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GorillaChieftain.class})
class GorillaChieftainTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1}{G} grants a regeneration shield")
    void payGrantsRegenerationShield() {
        Permanent chieftain = addCreatureReady(player1, new GorillaChieftain());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(chieftain.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate regeneration without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new GorillaChieftain());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate with only one green mana")
    void requiresGenericMana() {
        addCreatureReady(player1, new GorillaChieftain());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate with only generic mana")
    void requiresGreenMana() {
        addCreatureReady(player1, new GorillaChieftain());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Regeneration shield saves Gorilla Chieftain from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent chieftain = addCreatureReady(player1, new GorillaChieftain());
        chieftain.setRegenerationShield(1);
        chieftain.setBlocking(true);
        chieftain.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new GorillaChieftain());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Gorilla Chieftain");
        assertThat(chieftain.isTapped()).isTrue();
        assertThat(chieftain.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Gorilla Chieftain dies in combat without a regeneration shield")
    void diesWithoutRegenerationShield() {
        Permanent chieftain = addCreatureReady(player1, new GorillaChieftain());
        chieftain.setBlocking(true);
        chieftain.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new GorillaChieftain());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Gorilla Chieftain");
        harness.assertInGraveyard(player1, "Gorilla Chieftain");
    }

    @Test
    @DisplayName("Each resolved activation creates an additional regeneration shield")
    void multipleActivationsCreateMultipleShields() {
        Permanent chieftain = addCreatureReady(player1, new GorillaChieftain());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(chieftain.getRegenerationShield()).isEqualTo(2);
    }

    @Test
    @DisplayName("Two green mana can pay for regeneration while tapped and summoning sick")
    void activatesWhileTappedAndSummoningSickWithGreenMana() {
        Permanent chieftain = harness.addToBattlefieldAndReturn(player1, new GorillaChieftain());
        chieftain.setSummoningSick(true);
        chieftain.tap();
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(chieftain.getRegenerationShield()).isZero();
        harness.passBothPriorities();

        assertThat(chieftain.getRegenerationShield()).isEqualTo(1);
        assertThat(chieftain.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolved regeneration prevents lethal combat damage and removes the creature from combat")
    void resolvedActivationProtectsInCombat() {
        Permanent chieftain = addCreatureReady(player1, new GorillaChieftain());
        Permanent attacker = addCreatureReady(player2, new GorillaChieftain());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(chieftain.isTapped()).isFalse();
        chieftain.setBlocking(true);
        chieftain.addBlockingTarget(0);
        attacker.setAttacking(true);
        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Gorilla Chieftain");
        harness.assertNotInGraveyard(player1, "Gorilla Chieftain");
        harness.assertInGraveyard(player2, "Gorilla Chieftain");
        assertThat(chieftain.isTapped()).isTrue();
        assertThat(chieftain.isBlocking()).isFalse();
        assertThat(chieftain.getBlockingTargets()).isEmpty();
        assertThat(chieftain.getMarkedDamage()).isZero();
        assertThat(chieftain.getRegenerationShield()).isZero();
    }
}
