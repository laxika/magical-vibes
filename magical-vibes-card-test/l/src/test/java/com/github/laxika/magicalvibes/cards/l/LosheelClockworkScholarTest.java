package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.p.Panharmonicon;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LosheelClockworkScholar.class, Memnite.class, GrizzlyBears.class,
        SolRing.class, TurnToFrog.class, Panharmonicon.class})
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
        Permanent blocker = addBlocker(player2, 1, new GrizzlyBears());
        Permanent attacker = addAttacker(player1, new Memnite());

        resolveCombat();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage to a nonartifact attacker is not prevented")
    void doesNotPreventCombatDamageToNonartifactAttacker() {
        harness.addToBattlefield(player1, new LosheelClockworkScholar());
        Permanent blocker = addBlocker(player2, 1, new Memnite());
        Permanent attacker = addAttacker(player1, new GrizzlyBears());

        resolveCombat();

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    private void castArtifactAndResolveTrigger() {
        harness.setHand(player1, List.of(new Memnite()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    void noncreatureArtifactDoesNotConsumeDrawTrigger() {
        harness.addToBattlefield(player1, new LosheelClockworkScholar());
        harness.setHand(player1, List.of(new SolRing()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        castArtifactAndResolveTrigger();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void opposingArtifactCreatureDoesNotTriggerDraw() {
        harness.addToBattlefield(player1, new LosheelClockworkScholar());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Memnite()));
        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void artifactBlockerIsNotProtected() {
        harness.addToBattlefield(player2, new LosheelClockworkScholar());
        Permanent blocker = addBlocker(player2, 0, new Memnite());
        addAttacker(player1, new GrizzlyBears());

        resolveCombat();

        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Memnite");
    }

    @Test
    void losingAbilitiesStopsCombatProtection() {
        Permanent losheel = harness.addToBattlefieldAndReturn(player1, new LosheelClockworkScholar());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, losheel.getId());
        addBlocker(player2, 1, new GrizzlyBears());
        Permanent attacker = addAttacker(player1, new Memnite());

        resolveCombat();

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Memnite");
    }

    @Test
    void losingAbilitiesStopsDrawTrigger() {
        Permanent losheel = harness.addToBattlefieldAndReturn(player1, new LosheelClockworkScholar());
        harness.setHand(player1, List.of(new TurnToFrog(), new Memnite()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, losheel.getId());
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void panharmoniconCannotExceedOncePerTurnLimit() {
        harness.addToBattlefield(player1, new LosheelClockworkScholar());
        harness.addToBattlefield(player1, new Panharmonicon());
        harness.setHand(player1, List.of(new Memnite()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private Permanent addAttacker(Player controller, Card card) {
        Permanent attacker = addCreatureReady(controller, card);
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent addBlocker(Player controller, int blockingTarget, Card card) {
        Permanent blocker = addCreatureReady(controller, card);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(blockingTarget);
        return blocker;
    }
}
