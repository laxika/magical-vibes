package com.github.laxika.magicalvibes.cards.a;

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

@CardUsed(AncientSilverback.class)
class AncientSilverbackTest extends BaseCardTest {

    @Test
    @DisplayName("Activating {G} regeneration ability puts it on the stack targeting itself")
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
}
