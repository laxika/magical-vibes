package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KumenasSpeaker;
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

class HarpoonSniperTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability taps the sniper and puts it on the stack")
    void activatingPutsOnStack() {
        Permanent sniper = addSniperReady(player1);
        Permanent attacker = addAttackingCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, null, attacker.getId());

        assertThat(sniper.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("With a single Merfolk, deals 1 damage — 2-toughness target survives")
    void dealsOneWithSingleMerfolk() {
        addSniperReady(player1);
        Permanent attacker = addAttackingCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(attacker.getId()));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("deals 1 damage"));
    }

    @Test
    @DisplayName("X scales with Merfolk count — two Merfolk destroys a 2-toughness target")
    void dealsDamageEqualToMerfolkCount() {
        addSniperReady(player1);
        Permanent otherMerfolk = new Permanent(new KumenasSpeaker());
        otherMerfolk.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(otherMerfolk);
        Permanent attacker = addAttackingCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(attacker.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        addSniperReady(player1);
        Permanent bystander = new Permanent(new GrizzlyBears());
        bystander.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bystander);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking");
    }

    private Permanent addSniperReady(Player player) {
        Permanent sniper = new Permanent(new HarpoonSniper());
        sniper.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(sniper);
        return sniper;
    }

    private Permanent addAttackingCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        creature.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }
}
