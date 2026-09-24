package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LosheelClockworkScholar.class, Memnite.class, GrizzlyBears.class})
class LosheelClockworkScholarTest extends BaseCardTest {

    @Test
    @DisplayName("An artifact creature entering under your control draws a card")
    void artifactCreatureEntryDrawsCard() {
        harness.addToBattlefield(player1, new LosheelClockworkScholar());
        harness.setHand(player1, List.of(new Memnite()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Losheel draws only once each turn")
    void drawsOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new LosheelClockworkScholar());

        castArtifactAndResolveTrigger();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        castArtifactAndResolveTrigger();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Combat damage to an attacking artifact creature you control is prevented")
    void preventsCombatDamageToAttackingArtifactCreature() {
        harness.addToBattlefield(player1, new LosheelClockworkScholar());
        Permanent blocker = addBlocker(player2.getId(), 1, new GrizzlyBears());
        Permanent attacker = addAttacker(player1.getId(), new Memnite());

        resolveLosheelCombat();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage to a nonartifact attacker is not prevented")
    void doesNotPreventCombatDamageToNonartifactAttacker() {
        harness.addToBattlefield(player1, new LosheelClockworkScholar());
        Permanent blocker = addBlocker(player2.getId(), 1, new Memnite());
        Permanent attacker = addAttacker(player1.getId(), new GrizzlyBears());

        resolveLosheelCombat();

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    private void castArtifactAndResolveTrigger() {
        harness.setHand(player1, List.of(new Memnite()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void resolveLosheelCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent addAttacker(UUID controllerId, Card card) {
        Permanent attacker = new Permanent(card);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(controllerId).add(attacker);
        return attacker;
    }

    private Permanent addBlocker(UUID controllerId, int blockingTarget, Card card) {
        Permanent blocker = new Permanent(card);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(blockingTarget);
        gd.playerBattlefields.get(controllerId).add(blocker);
        return blocker;
    }
}
