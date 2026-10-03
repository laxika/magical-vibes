package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.MoriokReaver;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CorruptedHarvester.class, MoriokReaver.class})
class CorruptedHarvesterTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability sacrifices the chosen creature and puts regenerate on the stack")
    void activatingAbilitySacrificesCreatureAndPutsRegenerateOnStack() {
        Permanent harvesterPerm = addHarvesterReady(player1);
        harness.addToBattlefield(player1, new MoriokReaver());
        UUID sacrificeId = harness.getPermanentId(player1, "Moriok Reaver");
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificeId);

        GameData gd = harness.getGameData();

        // Moriok Reaver should be sacrificed
        harness.assertNotOnBattlefield(player1, "Moriok Reaver");
        harness.assertInGraveyard(player1, "Moriok Reaver");

        // Corrupted Harvester should still be on the battlefield
        harness.assertOnBattlefield(player1, "Corrupted Harvester");

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Corrupted Harvester");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(harvesterPerm.getId());
        assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
    }

    @Test
    @DisplayName("Resolving ability grants a regeneration shield")
    void resolvingAbilityGrantsRegenerationShield() {
        addHarvesterReady(player1);
        harness.addToBattlefield(player1, new MoriokReaver());
        UUID sacrificeId = harness.getPermanentId(player1, "Moriok Reaver");
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificeId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        Permanent harvester = findPermanent(player1, "Corrupted Harvester");
        assertThat(harvester.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Corrupted Harvester from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent harvesterPerm = addHarvesterReady(player1);
        harvesterPerm.setRegenerationShield(1);
        harvesterPerm.setBlocking(true);
        harvesterPerm.addBlockingTarget(0);

        // The opposing Harvester deals lethal damage.
        Permanent attacker = addHarvesterReady(player2);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Corrupted Harvester should survive via regeneration
        harness.assertOnBattlefield(player1, "Corrupted Harvester");
        Permanent harvester = findPermanent(player1, "Corrupted Harvester");
        assertThat(harvester.isTapped()).isTrue();
        assertThat(harvester.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Corrupted Harvester dies without regeneration shield in combat")
    void diesWithoutRegenerationShieldInCombat() {
        Permanent harvesterPerm = addHarvesterReady(player1);
        harvesterPerm.setBlocking(true);
        harvesterPerm.addBlockingTarget(0);

        Permanent attacker = addHarvesterReady(player2);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Both Corrupted Harvesters should kill each other (6 damage vs 3 toughness)
        harness.assertNotOnBattlefield(player1, "Corrupted Harvester");
        harness.assertInGraveyard(player1, "Corrupted Harvester");
    }

    @Test
    @DisplayName("Can sacrifice Corrupted Harvester to its own ability")
    void canSacrificeItself() {
        addHarvesterReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();

        // Harvester should be sacrificed
        harness.assertNotOnBattlefield(player1, "Corrupted Harvester");
        harness.assertInGraveyard(player1, "Corrupted Harvester");

        // Ability should still be on the stack
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Regeneration does nothing when Corrupted Harvester sacrifices itself")
    void regenerationDoesNothingWhenSacrificedItself() {
        addHarvesterReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();

        // The nontargeted ability resolves without affecting a permanent.
        harness.assertNotOnBattlefield(player1, "Corrupted Harvester");
        harness.assertInGraveyard(player1, "Corrupted Harvester");
    }

    @Test
    @DisplayName("Mana is consumed when activating the ability")
    void manaIsConsumedWhenActivating() {
        addHarvesterReady(player1);
        harness.addToBattlefield(player1, new MoriokReaver());
        UUID sacrificeId = harness.getPermanentId(player1, "Moriok Reaver");
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificeId);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addHarvesterReady(player1);
        harness.addToBattlefield(player1, new MoriokReaver());
        // No mana added

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Auto-sacrifices when only one creature is available")
    void autoSacrificesWhenOnlyOneCreature() {
        addHarvesterReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();

        // Harvester should be auto-sacrificed (only creature available)
        harness.assertNotOnBattlefield(player1, "Corrupted Harvester");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Ability does not tap Corrupted Harvester")
    void activatingAbilityDoesNotTap() {
        addHarvesterReady(player1);
        harness.addToBattlefield(player1, new MoriokReaver());
        UUID sacrificeId = harness.getPermanentId(player1, "Moriok Reaver");
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificeId);

        Permanent harvester = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(harvester.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate ability even when tapped")
    void canActivateWhenTapped() {
        Permanent harvesterPerm = addHarvesterReady(player1);
        harvesterPerm.tap();
        harness.addToBattlefield(player1, new MoriokReaver());
        UUID sacrificeId = harness.getPermanentId(player1, "Moriok Reaver");
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificeId);

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("Summoning sickness does not prevent creating a regeneration shield")
    void canActivateWhileSummoningSick() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new CorruptedHarvester());
        harvester.setSummoningSick(true);
        harness.addToBattlefield(player1, new MoriokReaver());
        UUID sacrificeId = harness.getPermanentId(player1, "Moriok Reaver");
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificeId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Moriok Reaver");
        assertThat(harvester.getRegenerationShield()).isEqualTo(1);
        assertThat(harvester.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A regeneration shield cannot prevent sacrificing the Harvester")
    void regenerationDoesNotPreventSacrifice() {
        Permanent harvester = addHarvesterReady(player1);
        harness.addToBattlefield(player1, new MoriokReaver());
        UUID sacrificeId = harness.getPermanentId(player1, "Moriok Reaver");
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificeId);
        harness.passBothPriorities();
        assertThat(harvester.getRegenerationShield()).isEqualTo(1);

        harness.activateAbility(player1, 0, null, null);
        harness.assertNotOnBattlefield(player1, "Corrupted Harvester");
        harness.assertInGraveyard(player1, "Corrupted Harvester");
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Corrupted Harvester");
    }

    private Permanent addHarvesterReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new CorruptedHarvester());
        perm.setSummoningSick(false);
        return perm;
    }
}
