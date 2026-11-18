package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;

import com.github.laxika.magicalvibes.cards.c.Cobblebrute;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JoragaInvocation.class, Cobblebrute.class})
class JoragaInvocationTest extends BaseCardTest {

    @Test
    @DisplayName("Every creature you control gets +3/+3 and must be blocked this turn")
    void boostsAndLuresAllOwnCreatures() {
        harness.addToBattlefield(player1, new Cobblebrute());
        harness.addToBattlefield(player1, new Cobblebrute());
        castInvocation();

        for (Permanent bears : gd.playerBattlefields.get(player1.getId())) {
            assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(8);
            assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
            assertThat(gd.hasFloatingEffectOnPermanent(bears.getId(), MustBeBlockedIfAbleEffect.class)).isTrue();
        }
    }

    @Test
    @DisplayName("Opponent creatures are unaffected")
    void opponentCreaturesUnaffected() {
        harness.addToBattlefield(player1, new Cobblebrute());
        harness.addToBattlefield(player2, new Cobblebrute());
        castInvocation();

        Permanent theirs = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, theirs)).isEqualTo(5);
        assertThat(gd.hasFloatingEffectOnPermanent(theirs.getId(), MustBeBlockedIfAbleEffect.class)).isFalse();
    }

    @Test
    @DisplayName("A boosted creature must be blocked if the defender is able")
    void attackerMustBeBlocked() {
        harness.addToBattlefield(player1, new Cobblebrute());
        castInvocation();

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new Cobblebrute());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Boost and must-be-blocked flag wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new Cobblebrute());
        castInvocation();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(PermanentGrants.powerBoostOf(gd, bears)).isEqualTo(0);
        assertThat(PermanentGrants.toughnessBoostOf(gd, bears)).isEqualTo(0);
        assertThat(gd.hasFloatingEffectOnPermanent(bears.getId(), MustBeBlockedIfAbleEffect.class)).isFalse();
    }

    @Test
    @DisplayName("Two available blockers must cover both attacking creatures")
    void blockersMustBeSpreadAcrossAttackers() {
        harness.addToBattlefield(player1, new Cobblebrute());
        harness.addToBattlefield(player1, new Cobblebrute());
        harness.addToBattlefield(player2, new Cobblebrute());
        harness.addToBattlefield(player2, new Cobblebrute());
        castInvocation();
        gd.playerBattlefields.get(player1.getId()).forEach(attacker -> attacker.setAttacking(true));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be blocked if able");

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1), new BlockerAssignment(1, 0)));
        assertThat(gd.playerBattlefields.get(player2.getId())).allMatch(Permanent::isBlocking);
    }

    @Test
    @DisplayName("One blocker may block either of two required attackers")
    void fewerBlockersThanAttackersIsLegal() {
        harness.addToBattlefield(player1, new Cobblebrute());
        harness.addToBattlefield(player1, new Cobblebrute());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new Cobblebrute());
        castInvocation();
        gd.playerBattlefields.get(player1.getId()).forEach(attacker -> attacker.setAttacking(true));
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A tapped defender cannot be forced to block")
    void noLegalBlockerAllowsNoBlocks() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new Cobblebrute());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new Cobblebrute());
        blocker.tap();
        castInvocation();
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());

        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive the boost")
    void laterCreaturesDoNotReceiveBoost() {
        harness.addToBattlefield(player1, new Cobblebrute());
        castInvocation();

        Permanent later = harness.enterBattlefieldAndReturn(player1, new Cobblebrute());

        assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, later)).isEqualTo(2);
    }

    @Test
    @DisplayName("Invocation can resolve without any creatures")
    void resolvesOnEmptyBattlefield() {
        castInvocation();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof JoragaInvocation);
    }

    private void castInvocation() {
        harness.setHand(player1, List.of(new JoragaInvocation()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
