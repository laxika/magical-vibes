package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AnimateArtifact;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Weakstone.class, GrizzlyBears.class, AnimateArtifact.class})
class WeakstoneTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creatures get -1/-0")
    void weakensAttackingCreatures() {
        harness.addToBattlefield(player1, new Weakstone());
        Permanent attacker = addAttacker(player2, player1);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Nonattacking creatures are unaffected")
    void ignoresNonattackingCreatures() {
        harness.addToBattlefield(player1, new Weakstone());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking creatures on either side are weakened")
    void weakensAttackingCreaturesRegardlessOfController() {
        harness.addToBattlefield(player1, new Weakstone());
        Permanent ownAttacker = addAttacker(player1, player2);
        Permanent opponentAttacker = addAttacker(player2, player1);

        assertThat(gqs.getEffectivePower(gd, ownAttacker)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentAttacker)).isEqualTo(1);
    }

    @Test
    @DisplayName("The penalty follows the creature's current attacking status")
    void penaltyEndsWhenCreatureStopsAttacking() {
        harness.addToBattlefield(player1, new Weakstone());
        Permanent attacker = addAttacker(player2, player1);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(1);
        attacker.setAttacking(false);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        attacker.setAttacking(true);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple Weakstones stack and allow negative power")
    void multipleWeakstonesStack() {
        harness.addToBattlefield(player1, new Weakstone());
        harness.addToBattlefield(player2, new Weakstone());
        harness.addToBattlefield(player1, new Weakstone());
        Permanent attacker = addAttacker(player2, player1);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapped Weakstone still weakens attackers")
    void tappedWeakstoneStillApplies() {
        Permanent weakstone = harness.addToBattlefieldAndReturn(player1, new Weakstone());
        weakstone.tap();
        Permanent attacker = addAttacker(player2, player1);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocking creatures are unaffected")
    void ignoresBlockingCreatures() {
        harness.addToBattlefield(player1, new Weakstone());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setBlocking(true);

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Animated Weakstone weakens itself while attacking")
    void animatedWeakstoneWeakensItself() {
        Permanent weakstone = harness.addToBattlefieldAndReturn(player1, new Weakstone());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AnimateArtifact());
        aura.setAttachedTo(weakstone.getId());
        weakstone.setSummoningSick(false);
        weakstone.setAttacking(true);
        weakstone.setAttackTarget(player2.getId());

        assertThat(gqs.getEffectivePower(gd, weakstone)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, weakstone)).isEqualTo(4);
    }

    private Permanent addAttacker(Player controller, Player attackTarget) {
        Permanent attacker = harness.addToBattlefieldAndReturn(controller, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(attackTarget.getId());
        return attacker;
    }
}
