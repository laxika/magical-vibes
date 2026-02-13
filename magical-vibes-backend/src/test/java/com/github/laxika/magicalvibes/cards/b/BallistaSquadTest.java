package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BallistaSquadTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Ballista Squad has correct card properties")
    void hasCorrectProperties() {
        BallistaSquad card = new BallistaSquad();

        assertThat(card.getName()).isEqualTo("Ballista Squad");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{3}{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(2);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.HUMAN, CardSubtype.REBEL);
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(DealXDamageToTargetCreatureEffect.class);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{X}{W}");
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Ballista Squad puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new BallistaSquad()));
        harness.addMana(player1, "W", 4);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Ballista Squad");
    }

    @Test
    @DisplayName("Resolving Ballista Squad puts it on the battlefield")
    void resolvingPutsItOnBattlefield() {
        harness.setHand(player1, List.of(new BallistaSquad()));
        harness.addMana(player1, "W", 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Ballista Squad"));
    }

    // ===== Activate ability =====

    @Test
    @DisplayName("Can activate ability targeting attacking creature")
    void canActivateAbilityOnAttackingCreature() {
        Permanent ballistaPerm = addBallistaReadyToCombat(player1);
        Permanent targetPerm = addAttackingCreature(player2);
        harness.addMana(player1, "W", 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 2, targetPerm.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Ballista Squad");
        assertThat(entry.getXValue()).isEqualTo(2);
        assertThat(entry.getTargetPermanentId()).isEqualTo(targetPerm.getId());
    }

    @Test
    @DisplayName("Activating ability taps the permanent")
    void activatingAbilityTapsPermanent() {
        Permanent ballistaPerm = addBallistaReadyToCombat(player1);
        Permanent targetPerm = addAttackingCreature(player2);
        harness.addMana(player1, "W", 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, 0, targetPerm.getId());

        assertThat(ballistaPerm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolving ability deals X damage and destroys creature when X >= toughness")
    void resolvingAbilityDealsXDamageAndDestroysCreature() {
        addBallistaReadyToCombat(player1);
        Permanent targetPerm = addAttackingCreature(player2);
        harness.addMana(player1, "W", 3); // X=2, W=1 → 3 white mana needed
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, 2, targetPerm.getId());

        // Resolve the ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        // GrizzlyBears has 2 toughness, X=2 so it should be destroyed
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Resolving ability with X < toughness deals damage but does not destroy")
    void resolvingAbilityWithLowXDoesNotDestroy() {
        addBallistaReadyToCombat(player1);
        Permanent targetPerm = addAttackingCreature(player2);
        harness.addMana(player1, "W", 2); // X=1, W=1
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, 1, targetPerm.getId());

        // Resolve the ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        // GrizzlyBears has 2 toughness, X=1 so it should survive
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("deals 1 damage"));
    }

    @Test
    @DisplayName("Can activate ability targeting blocking creature")
    void canActivateAbilityOnBlockingCreature() {
        addBallistaReadyToCombat(player1);
        GrizzlyBears bear = new GrizzlyBears();
        Permanent blockerPerm = new Permanent(bear);
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(blockerPerm);
        harness.addMana(player1, "W", 3);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player1, 0, 2, blockerPerm.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetPermanentId()).isEqualTo(blockerPerm.getId());
    }

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void abilityFizzlesIfTargetRemoved() {
        addBallistaReadyToCombat(player1);
        Permanent targetPerm = addAttackingCreature(player2);
        harness.addMana(player1, "W", 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, 2, targetPerm.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Validation errors =====

    @Test
    @DisplayName("Cannot activate ability targeting non-combat creature")
    void cannotActivateAbilityOnNonCombatCreature() {
        addBallistaReadyToCombat(player1);
        GrizzlyBears bear = new GrizzlyBears();
        Permanent nonCombatPerm = new Permanent(bear);
        nonCombatPerm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(nonCombatPerm);
        harness.addMana(player1, "W", 3);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, nonCombatPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking");
    }

    @Test
    @DisplayName("Cannot activate ability without a target")
    void cannotActivateAbilityWithoutTarget() {
        addBallistaReadyToCombat(player1);
        harness.addMana(player1, "W", 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("requires a target");
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent ballistaPerm = addBallistaReadyToCombat(player1);
        ballistaPerm.tap();
        Permanent targetPerm = addAttackingCreature(player2);
        harness.addMana(player1, "W", 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, targetPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate ability with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        BallistaSquad card = new BallistaSquad();
        Permanent ballistaPerm = new Permanent(card);
        // summoningSick is true by default
        harness.getGameData().playerBattlefields.get(player1.getId()).add(ballistaPerm);
        Permanent targetPerm = addAttackingCreature(player2);
        harness.addMana(player1, "W", 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, targetPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addBallistaReadyToCombat(player1);
        Permanent targetPerm = addAttackingCreature(player2);
        // No mana added
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, targetPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addBallistaReadyToCombat(player1);
        Permanent targetPerm = addAttackingCreature(player2);
        harness.addMana(player1, "W", 4);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, 3, targetPerm.getId());

        // X=3 + {W} = 4 mana used, should have 0 left
        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Activating ability with X=0 still costs {W}")
    void activatingWithXZeroStillCostsW() {
        addBallistaReadyToCombat(player1);
        Permanent targetPerm = addAttackingCreature(player2);
        harness.addMana(player1, "W", 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, 0, targetPerm.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    // ===== Helper methods =====

    private Permanent addBallistaReadyToCombat(Player player) {
        BallistaSquad card = new BallistaSquad();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addAttackingCreature(Player player) {
        GrizzlyBears bear = new GrizzlyBears();
        Permanent perm = new Permanent(bear);
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
