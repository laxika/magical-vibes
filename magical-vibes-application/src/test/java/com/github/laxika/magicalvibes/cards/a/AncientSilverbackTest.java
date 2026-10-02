package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AncientSilverback.class, GrizzlyBears.class})
class AncientSilverbackTest extends BaseCardTest {

    @Test
    @DisplayName("Activating {G} regeneration ability puts it on the stack referring to its source")
    void activatingAbilityPutsOnStack() {
        Permanent apePerm = addCreatureReady(player1, new AncientSilverback());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(apePerm.getId());
    }

    @Test
    @DisplayName("Resolving the regeneration ability grants a regeneration shield")
    void resolvingGrantsRegenerationShield() {
        addCreatureReady(player1, new AncientSilverback());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent ape = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(ape.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate regeneration ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new AncientSilverback());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Regeneration shield saves it from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent apePerm = addCreatureReady(player1, new AncientSilverback());
        apePerm.setRegenerationShield(1);
        apePerm.setBlocking(true);
        apePerm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new AncientSilverback());
        attacker.setAttacking(true);

        resolveCombat(player2);

        Permanent ape = findPermanent(player1, "Ancient Silverback");
        assertThat(ape.isTapped()).isTrue();
        assertThat(ape.getRegenerationShield()).isEqualTo(0);
        assertThat(ape.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Nonlethal combat damage does not consume a regeneration shield")
    void nonlethalDamageDoesNotConsumeRegenerationShield() {
        Permanent apePerm = addCreatureReady(player1, new AncientSilverback());
        apePerm.setRegenerationShield(1);
        apePerm.setBlocking(true);
        apePerm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player2);

        Permanent ape = findPermanent(player1, "Ancient Silverback");
        assertThat(ape.getRegenerationShield()).isEqualTo(1);
        assertThat(ape.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Dies without a regeneration shield from lethal combat damage")
    void diesWithoutRegenerationShield() {
        Permanent apePerm = addCreatureReady(player1, new AncientSilverback());
        apePerm.setBlocking(true);
        apePerm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new AncientSilverback());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Ancient Silverback");
        harness.assertInGraveyard(player1, "Ancient Silverback");
    }

    @Test
    @DisplayName("Regeneration heals all marked damage when its shield is used")
    void regenerationClearsMarkedDamage() {
        Permanent apePerm = addCreatureReady(player1, new AncientSilverback());
        apePerm.setMarkedDamage(4);
        apePerm.setRegenerationShield(1);
        apePerm.setBlocking(true);
        apePerm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new AncientSilverback());
        attacker.setAttacking(true);
        resolveCombat(player2);

        Permanent ape = findPermanent(player1, "Ancient Silverback");
        assertThat(ape.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Dies without a regeneration shield from lethal combat damage")
    void diesWithoutRegenerationShieldUpstreamReview() {
        Permanent apePerm = addCreatureReady(player1, new AncientSilverback());
        apePerm.setBlocking(true);
        apePerm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new AncientSilverback());
        attacker.setAttacking(true);
        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Ancient Silverback");
        harness.assertInGraveyard(player1, "Ancient Silverback");
    }

    @Test
    @DisplayName("Regeneration can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent ape = harness.addToBattlefieldAndReturn(player1, new AncientSilverback());
        ape.setSummoningSick(true);
        ape.setTapped(true);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ape.getRegenerationShield()).isEqualTo(1);
        assertThat(ape.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creating a shield does not immediately tap the creature or heal damage")
    void creatingShieldDoesNotRegenerateImmediately() {
        Permanent ape = addCreatureReady(player1, new AncientSilverback());
        ape.setMarkedDamage(2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ape.getRegenerationShield()).isEqualTo(1);
        assertThat(ape.isTapped()).isFalse();
        assertThat(ape.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Repeated activations protect against separate lethal damage events")
    void multipleActivationsProtectAgainstSeparateDestructions() {
        Permanent ape = addCreatureReady(player1, new AncientSilverback());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(ape.getRegenerationShield()).isEqualTo(2);

        ape.setMarkedDamage(5);
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Ancient Silverback");
        assertThat(ape.getRegenerationShield()).isEqualTo(1);
        assertThat(ape.getMarkedDamage()).isZero();
        assertThat(ape.isTapped()).isTrue();

        ape.setMarkedDamage(5);
        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Ancient Silverback");
        assertThat(ape.getRegenerationShield()).isZero();
        assertThat(ape.getMarkedDamage()).isZero();

        ape.setMarkedDamage(5);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Ancient Silverback");
        harness.assertInGraveyard(player1, "Ancient Silverback");
    }
}
