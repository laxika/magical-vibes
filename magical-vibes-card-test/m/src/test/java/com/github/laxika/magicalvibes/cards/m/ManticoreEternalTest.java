package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NicolBolasGodPharaoh;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ManticoreEternal.class, GrizzlyBears.class, NicolBolasGodPharaoh.class})
class ManticoreEternalTest extends BaseCardTest {

    @Test
    @DisplayName("Afflict 3: becoming blocked makes the defending player lose 3 life")
    void blockedAfflictsDefender() {
        Permanent atk = harness.addToBattlefieldAndReturn(player1, new ManticoreEternal());
        atk.setSummoningSick(false);
        atk.setAttacking(true);
        atk.setAttackTarget(player2.getId());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setSummoningSick(false);

        harness.setHand(player1, new ArrayList<>());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Declaring no attackers when Manticore Eternal can attack throws exception")
    void mustAttackWhenAble() {
        Permanent manticore = harness.addToBattlefieldAndReturn(player1, new ManticoreEternal());
        manticore.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void multipleBlockersCauseOnlyOneAfflictTrigger() {
        prepareBlockedCombat();
        harness.addToBattlefield(player2, new ManticoreEternal());
        harness.addToBattlefield(player2, new ManticoreEternal());

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));

        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
    }

    @Test
    void unblockedManticoreDoesNotTriggerAfflict() {
        prepareBlockedCombat();

        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    void afflictResolvesAfterManticoreLeavesBattlefield() {
        Permanent attacker = prepareBlockedCombat();
        harness.addToBattlefield(player2, new ManticoreEternal());
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        gd.playerGraveyards.get(player1.getId()).add(attacker.getCard());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    @Test
    void afflictStillAffectsDefenderAfterAttackedPlaneswalkerLeavesBattlefield() {
        Permanent attacker = prepareBlockedCombat();
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new NicolBolasGodPharaoh());
        attacker.setAttackTarget(planeswalker.getId());
        harness.addToBattlefield(player2, new ManticoreEternal());
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player2.getId()).remove(planeswalker);
        gd.playerGraveyards.get(player2.getId()).add(planeswalker.getCard());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
    }

    @Test
    void tappedManticoreIsNotRequiredToAttack() {
        Permanent manticore = harness.addToBattlefieldAndReturn(player1, new ManticoreEternal());
        manticore.setSummoningSick(false);
        manticore.tap();
        prepareAttackerDeclaration();

        gs.declareAttackers(gd, player1, List.of());

        assertThat(manticore.isAttacking()).isFalse();
    }

    @Test
    void summoningSickManticoreIsNotRequiredToAttack() {
        Permanent manticore = harness.addToBattlefieldAndReturn(player1, new ManticoreEternal());
        manticore.setSummoningSick(true);
        prepareAttackerDeclaration();

        gs.declareAttackers(gd, player1, List.of());

        assertThat(manticore.isAttacking()).isFalse();
    }

    private Permanent prepareBlockedCombat() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new ManticoreEternal());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        return attacker;
    }

    private void prepareAttackerDeclaration() {
        prepareAttackerDeclaration();
    }
}
