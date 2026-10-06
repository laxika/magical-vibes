package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GuardianShieldBearer;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RevealingWind.class, GuardianShieldBearer.class})
class RevealingWindTest extends BaseCardTest {

    @Test
    void preventsCombatDamageAndRevealsFaceDownCombatCreatures() {
        Permanent attackingCreature = addFaceDownCreature(player2);
        attackingCreature.setAttacking(true);
        Permanent blockingCreature = addFaceDownCreature(player1);
        blockingCreature.setBlocking(true);
        addFaceDownCreature(player2);
        Permanent faceUpAttacker = harness.addToBattlefieldAndReturn(player2, new GuardianShieldBearer());
        faceUpAttacker.setAttacking(true);
        harness.clearMessages();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.castFromHand(player1, new RevealingWind(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(gd.preventAllCombatDamage).isTrue();
        assertThat(harness.getConn1().getMessagesContaining("REVEAL_PERMANENT")).hasSize(2);
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_PERMANENT")).isEmpty();
    }

    @Test
    void preventsDamageToPlayersAndBothCombatantsWithoutFaceDownCreatures() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GuardianShieldBearer());
        attacker.setAttacking(true);
        Permanent unblockedAttacker = harness.addToBattlefieldAndReturn(player2, new GuardianShieldBearer());
        unblockedAttacker.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player1, new GuardianShieldBearer());
        blocker.setBlocking(true);
        blocker.getBlockingTargets().add(0);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new RevealingWind(), "{2}{G}");
        harness.passBothPriorities();
        harness.resolveCombatDamage();

        harness.assertLife(player1, 20);
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker, unblockedAttacker);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blocker);
        assertThat(harness.getConn1().getMessagesContaining("REVEAL_PERMANENT")).isEmpty();
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_PERMANENT")).isEmpty();
    }

    @Test
    void attackingPlayerCanLookAtBlockersWithoutTurningThemFaceUp() {
        Permanent attacker = addFaceDownCreature(player1);
        attacker.setAttacking(true);
        Permanent blocker = addFaceDownCreature(player2);
        blocker.setBlocking(true);
        blocker.getBlockingTargets().add(0);
        Permanent uninvolvedCreature = addFaceDownCreature(player2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearMessages();

        harness.castFromHand(player1, new RevealingWind(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(harness.getConn1().getMessagesContaining("REVEAL_PERMANENT")).hasSize(2);
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_PERMANENT")).isEmpty();
        assertThat(attacker.isFaceDown()).isTrue();
        assertThat(blocker.isFaceDown()).isTrue();
        assertThat(uninvolvedCreature.isFaceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addFaceDownCreature(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GuardianShieldBearer());
        permanent.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        return permanent;
    }
}
