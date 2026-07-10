package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AncientSilverbackTest extends BaseCardTest {

    @Test
    @DisplayName("Activating {G} regeneration ability puts it on the stack targeting itself")
    void activatingAbilityPutsOnStack() {
        Permanent apePerm = addSilverbackReady(player1);
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
        addSilverbackReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent ape = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(ape.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate regeneration ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addSilverbackReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Regeneration shield saves it from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        // Silverback (6/5) blocks a Hill Giant (3/3) but takes an extra lethal blocker;
        // simpler: give it a shield and block an attacker whose power exceeds 5.
        Permanent apePerm = addSilverbackReady(player1);
        apePerm.setRegenerationShield(1);
        apePerm.setBlocking(true);
        apePerm.addBlockingTarget(0);

        AncientSilverback attackerCard = new AncientSilverback();
        Permanent attacker = new Permanent(attackerCard);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        Permanent ape = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Ancient Silverback"))
                .findFirst().orElseThrow();
        assertThat(ape.isTapped()).isTrue();
        assertThat(ape.getRegenerationShield()).isEqualTo(0);
        assertThat(ape.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Dies without a regeneration shield from lethal combat damage")
    void diesWithoutRegenerationShield() {
        Permanent apePerm = addSilverbackReady(player1);
        apePerm.setBlocking(true);
        apePerm.addBlockingTarget(0);

        AncientSilverback attackerCard = new AncientSilverback();
        Permanent attacker = new Permanent(attackerCard);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Ancient Silverback"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Ancient Silverback"));
    }

    private Permanent addSilverbackReady(Player player) {
        AncientSilverback card = new AncientSilverback();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
