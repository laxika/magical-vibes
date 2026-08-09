package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

class BlindingPowderTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving equip attaches Blinding Powder to target creature")
    void equipAttachesToCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent powder = addPowderReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(powder.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Unattaching Blinding Powder prevents combat damage to the equipped creature this turn")
    void unattachPreventsCombatDamageToEquippedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent powder = addPowderReady(player1);
        powder.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(powder.getAttachedTo()).isNull();

        creature.setBlocking(true);
        creature.addBlockingTarget(0);
        Permanent attacker = new Permanent(new HillGiant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Blinding Powder does not prevent noncombat damage")
    void doesNotPreventNoncombatDamage() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent powder = addPowderReady(player1);
        powder.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    private Permanent addPowderReady(Player player) {
        Permanent powder = new Permanent(new BlindingPowder());
        powder.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(powder);
        return powder;
    }
}
