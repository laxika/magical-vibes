package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CorruptedHarvesterTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Corrupted Harvester has correct activated ability structure")
    void hasCorrectAbilityStructure() {
        CorruptedHarvester card = new CorruptedHarvester();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isEqualTo("{B}");
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeCreatureCost.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(RegenerateEffect.class);
    }

    // ===== Activation: sacrificing a creature =====

    @Test
    @DisplayName("Activating ability sacrifices the chosen creature and puts regenerate on the stack")
    void activatingAbilitySacrificesCreatureAndPutsRegenerateOnStack() {
        Permanent harvesterPerm = addHarvesterReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);

        GameData gd = harness.getGameData();

        // Grizzly Bears should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Corrupted Harvester should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Corrupted Harvester"));

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Corrupted Harvester");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(harvesterPerm.getId());
    }

    @Test
    @DisplayName("Resolving ability grants a regeneration shield")
    void resolvingAbilityGrantsRegenerationShield() {
        addHarvesterReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        Permanent harvester = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Corrupted Harvester"))
                .findFirst().orElseThrow();
        assertThat(harvester.getRegenerationShield()).isEqualTo(1);
    }

    // ===== Regeneration saves from lethal combat damage =====

    @Test
    @DisplayName("Regeneration shield saves Corrupted Harvester from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        // Corrupted Harvester (6/3) with regen shield blocks ... we need something with 3+ power
        // Use two Grizzly Bears (2/2) as attackers? No, we need a single creature with 3+ power.
        // Let's set up directly: harvester blocking, attacker deals 3+ damage
        Permanent harvesterPerm = addHarvesterReady(player1);
        harvesterPerm.setRegenerationShield(1);
        harvesterPerm.setBlocking(true);
        harvesterPerm.addBlockingTarget(0);

        // Create a 4/4 attacker to deal lethal to the 6/3 harvester (3 toughness)
        Permanent attacker = addHarvesterReady(player2);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Corrupted Harvester should survive via regeneration
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Corrupted Harvester"));
        Permanent harvester = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Corrupted Harvester"))
                .findFirst().orElseThrow();
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

        GameData gd = harness.getGameData();
        // Both Corrupted Harvesters should kill each other (6 damage vs 3 toughness)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Corrupted Harvester"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Corrupted Harvester"));
    }

    // ===== Can sacrifice itself =====

    @Test
    @DisplayName("Can sacrifice Corrupted Harvester to its own ability")
    void canSacrificeItself() {
        addHarvesterReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();

        // Harvester should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Corrupted Harvester"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Corrupted Harvester"));

        // Ability should still be on the stack
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Regeneration fizzles when Corrupted Harvester sacrifices itself")
    void regenerationFizzlesWhenSacrificedItself() {
        addHarvesterReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();

        // Harvester is in the graveyard, ability fizzled
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Corrupted Harvester"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Corrupted Harvester"));
    }

    // ===== Mana cost =====

    @Test
    @DisplayName("Mana is consumed when activating the ability")
    void manaIsConsumedWhenActivating() {
        addHarvesterReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addHarvesterReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        // No mana added

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== Auto-sacrifice =====

    @Test
    @DisplayName("Auto-sacrifices when only one creature is available")
    void autoSacrificesWhenOnlyOneCreature() {
        addHarvesterReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        GameData gd = harness.getGameData();

        // Harvester should be auto-sacrificed (only creature available)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Corrupted Harvester"));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    // ===== Does not require tap =====

    @Test
    @DisplayName("Ability does not tap Corrupted Harvester")
    void activatingAbilityDoesNotTap() {
        addHarvesterReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);

        Permanent harvester = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(harvester.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate ability even when tapped")
    void canActivateWhenTapped() {
        Permanent harvesterPerm = addHarvesterReady(player1);
        harvesterPerm.tap();
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    // ===== Helper methods =====

    private Permanent addHarvesterReady(Player player) {
        CorruptedHarvester card = new CorruptedHarvester();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
