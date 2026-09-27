package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.ElectrostaticBolt;
import com.github.laxika.magicalvibes.cards.g.Groffskithur;
import com.github.laxika.magicalvibes.cards.m.MyrEnforcer;
import com.github.laxika.magicalvibes.cards.p.PredatorsStrike;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrollAscetic.class, ElectrostaticBolt.class, Groffskithur.class, MyrEnforcer.class,
        PredatorsStrike.class, Terror.class})
class TrollAsceticTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting Troll Ascetic puts it on the stack and resolves to battlefield")
    void castingAndResolving() {
        harness.castFromHand(player1, new TrollAscetic(), "{1}{G}{G}");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Troll Ascetic");
    }

    // ===== Hexproof: opponent cannot target =====

    @Test
    @DisplayName("Opponent cannot target Troll Ascetic with spells")
    void opponentCannotTargetWithSpells() {
        // Player1 is active and owns the Troll
        Permanent trollPerm = addCreatureReady(player1, new TrollAscetic());
        addCreatureReady(player1, new Groffskithur());

        // Player2 tries to Electrostatic Bolt the Troll
        harness.setHand(player2, List.of(new ElectrostaticBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, trollPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Opponent cannot target Troll Ascetic with Terror")
    void opponentCannotTargetWithTerror() {
        Permanent trollPerm = addCreatureReady(player1, new TrollAscetic());

        // Add valid target so spell is playable
        addCreatureReady(player1, new Groffskithur());

        harness.setHand(player2, List.of(new Terror()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, trollPerm.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    // ===== Hexproof: controller CAN target =====

    @Test
    @DisplayName("Controller can target own Troll Ascetic with spells")
    void controllerCanTargetOwnTrollAscetic() {
        Permanent trollPerm = addCreatureReady(player1, new TrollAscetic());

        harness.setHand(player1, List.of(new PredatorsStrike()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, trollPerm.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Predator's Strike");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(trollPerm.getId());
    }

    // ===== Hexproof: hasKeyword check =====

    @Test
    @DisplayName("Troll Ascetic has hexproof keyword on the battlefield")
    void hasHexproofKeyword() {
        Permanent trollPerm = addCreatureReady(player1, new TrollAscetic());

        assertThat(gqs.hasKeyword(gd, trollPerm, Keyword.HEXPROOF)).isTrue();
    }

    // ===== Regenerate activated ability =====

    @Test
    @DisplayName("Activating regeneration ability puts it on the stack")
    void activatingRegenPutsOnStack() {
        Permanent trollPerm = addCreatureReady(player1, new TrollAscetic());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Troll Ascetic");
        assertThat(entry.getTargetId()).isEqualTo(trollPerm.getId());
    }

    @Test
    @DisplayName("Resolving regeneration ability grants a regeneration shield")
    void resolvingRegenGrantsShield() {
        Permanent troll = addCreatureReady(player1, new TrollAscetic());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(troll.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Mana is consumed when activating regeneration ability")
    void manaConsumedOnRegenActivation() {
        addCreatureReady(player1, new TrollAscetic());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate regeneration ability without enough mana")
    void cannotActivateRegenWithoutMana() {
        addCreatureReady(player1, new TrollAscetic());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== Regeneration saves from combat damage =====

    @Test
    @DisplayName("Regeneration shield saves Troll Ascetic from lethal combat damage")
    void regenSavesFromLethalCombatDamage() {
        Permanent trollPerm = addCreatureReady(player1, new TrollAscetic());
        trollPerm.setRegenerationShield(1);
        trollPerm.setBlocking(true);
        trollPerm.addBlockingTarget(0);

        // 4/4 attacker deals lethal to 3/2 Troll Ascetic
        Permanent attacker = addCreatureReady(player2, new MyrEnforcer());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Troll Ascetic");
        Permanent troll = findPermanent(player1, "Troll Ascetic");
        assertThat(troll.isTapped()).isTrue();
        assertThat(troll.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Troll Ascetic dies without regeneration shield in combat")
    void diesWithoutRegenShield() {
        Permanent trollPerm = addCreatureReady(player1, new TrollAscetic());
        trollPerm.setBlocking(true);
        trollPerm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new MyrEnforcer());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Troll Ascetic");
        harness.assertInGraveyard(player1, "Troll Ascetic");
    }

    // ===== Regeneration shield clears at end of turn =====

    @Test
    @DisplayName("Regeneration shield clears at end of turn cleanup")
    void regenShieldClearsAtEndOfTurn() {
        Permanent troll = addCreatureReady(player1, new TrollAscetic());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(troll.getRegenerationShield()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(troll.getRegenerationShield()).isEqualTo(0);
    }

}
