package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TattermungeWitchTest extends BaseCardTest {

    @Test
    @DisplayName("Only blocked creatures get +1/+0 and trample")
    void buffsOnlyBlockedCreatures() {
        Permanent witch = addReadyCreature(player1, new TattermungeWitch());
        Permanent blockedAttacker = addReadyCreature(player1, new GrizzlyBears());
        blockedAttacker.setAttacking(true);
        Permanent bystander = addReadyCreature(player1, new GrizzlyBears());
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(blockedAttacker.getId());

        activateWitch(witch);

        // The blocked attacker gets the buff.
        assertThat(blockedAttacker.getEffectivePower()).isEqualTo(3);
        assertThat(blockedAttacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(blockedAttacker.hasKeyword(Keyword.TRAMPLE)).isTrue();

        // A non-attacking bystander is not blocked → unaffected.
        assertThat(bystander.getEffectivePower()).isEqualTo(2);
        assertThat(bystander.hasKeyword(Keyword.TRAMPLE)).isFalse();

        // The blocker is blocking, not blocked → unaffected.
        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("An unblocked attacker is not buffed")
    void unblockedAttackerNotBuffed() {
        Permanent witch = addReadyCreature(player1, new TattermungeWitch());
        Permanent unblockedAttacker = addReadyCreature(player1, new GrizzlyBears());
        unblockedAttacker.setAttacking(true);

        activateWitch(witch);

        assertThat(unblockedAttacker.getEffectivePower()).isEqualTo(2);
        assertThat(unblockedAttacker.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The buff wears off at end of turn")
    void buffWearsOffAtEndOfTurn() {
        Permanent witch = addReadyCreature(player1, new TattermungeWitch());
        Permanent blockedAttacker = addReadyCreature(player1, new GrizzlyBears());
        blockedAttacker.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(blockedAttacker.getId());

        activateWitch(witch);

        assertThat(blockedAttacker.getEffectivePower()).isEqualTo(3);
        assertThat(blockedAttacker.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blockedAttacker.getEffectivePower()).isEqualTo(2);
        assertThat(blockedAttacker.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    private void activateWitch(Permanent witch) {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(witch);
        harness.activateAbility(player1, index, null, null);
        harness.passBothPriorities();
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
