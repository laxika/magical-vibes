package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkyblinderStaffTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = equipStaffTo(new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature can't be blocked by a creature with flying")
    void cannotBeBlockedByFlier() {
        Permanent attacker = equipStaffTo(new GrizzlyBears());
        Permanent blocker = addBlocker(new SuntailHawk());

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Equipped creature can still be blocked by a creature without flying")
    void canBeBlockedByNonFlier() {
        Permanent attacker = equipStaffTo(new GrizzlyBears());
        Permanent blocker = addBlocker(new GrizzlyBears());

        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Unattached Skyblinder Staff does not stop fliers from blocking")
    void unattachedStaffDoesNotRestrictBlocks() {
        addStaffReady(player1);
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(attacker);
        Permanent blocker = addBlocker(new SuntailHawk());

        attacker.setAttacking(true);
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent equipStaffTo(Card creatureCard) {
        addStaffReady(player1);
        Permanent creature = new Permanent(creatureCard);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(creature);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        return creature;
    }

    private Permanent addStaffReady(Player player) {
        Permanent perm = new Permanent(new SkyblinderStaff());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addBlocker(Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(perm);
        return perm;
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
    }
}
