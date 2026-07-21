package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DawnrayArcherTest extends BaseCardTest {

    // ===== Exalted =====

    @Test
    @DisplayName("Exalted — another creature attacking alone gets +1/+1")
    void allyAttackingAloneBoosted() {
        addCreatureReady(player1, new DawnrayArcher());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1)); // Grizzly Bears attacks alone
        harness.passBothPriorities(); // resolve exalted trigger

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exalted — the Archer attacking alone boosts itself")
    void selfAttackingAloneBoosted() {
        Permanent archer = addCreatureReady(player1, new DawnrayArcher());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities(); // resolve exalted trigger

        assertThat(gqs.getEffectivePower(gd, archer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, archer)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exalted boost wears off at end of turn")
    void boostWearsOff() {
        addCreatureReady(player1, new DawnrayArcher());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exalted does not trigger when attacking with more than one creature")
    void noTriggerWhenNotAlone() {
        addCreatureReady(player1, new DawnrayArcher());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1)); // both attack — not alone

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    // ===== {W}, {T}: deal 1 damage to attacking or blocking creature =====

    @Test
    @DisplayName("Ability deals 1 damage to an attacking creature, killing a 1/1")
    void abilityDestroysAttackingOneToughness() {
        Permanent archer = addCreatureReady(player1, new DawnrayArcher());
        Permanent target = addAttackingCreature(player2, new FugitiveWizard());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, 0, target.getId());
        assertThat(archer.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Fugitive Wizard"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Fugitive Wizard"));
    }

    @Test
    @DisplayName("Ability can target a blocking creature")
    void abilityTargetsBlockingCreature() {
        addCreatureReady(player1, new DawnrayArcher());
        GrizzlyBears bear = new GrizzlyBears();
        Permanent blocker = new Permanent(bear);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player1, 0, 0, blocker.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(blocker.getId());
    }

    @Test
    @DisplayName("Cannot target a creature that is neither attacking nor blocking")
    void cannotTargetNonCombatCreature() {
        addCreatureReady(player1, new DawnrayArcher());
        Permanent nonCombat = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, nonCombat.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking");
    }

    private Permanent addAttackingCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
