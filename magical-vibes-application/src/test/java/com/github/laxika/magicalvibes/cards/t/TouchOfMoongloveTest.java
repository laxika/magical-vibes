package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TouchOfMoongloveTest extends BaseCardTest {

    @Test
    @DisplayName("Deathtouch damage from the buffed creature kills the blocker and its controller loses 2 life")
    void deathtouchKillDrainsBlockersController() {
        Permanent attacker = addAttackingBears();
        Permanent blocker = addToughBlocker();
        castOn(attacker);

        harness.passBothPriorities(); // combat damage — deathtouch destroys the 0/5 blocker
        harness.passBothPriorities(); // resolve the "its controller loses 2 life" trigger

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The +1/+0 boost applies to the targeted creature")
    void boostsTargetPower() {
        Permanent attacker = addAttackingBears();
        addToughBlocker();
        castOn(attacker);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
    }

    @Test
    @DisplayName("A creature that was not targeted drains nobody when its victim dies")
    void untargetedCreatureDoesNotDrain() {
        Permanent attacker = addAttackingBears();
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        other.setAttacking(true);

        GrizzlyBears smallCard = new GrizzlyBears();
        smallCard.setPower(0);
        smallCard.setToughness(1);
        Permanent blocker = new Permanent(smallCard);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        castOn(attacker);

        harness.passBothPriorities(); // combat damage — the untargeted bear kills the 0/1
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        // Player 2 only took the unblocked attacker's combat damage, never the 2-life drain.
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The granted death-drain ability wears off at end of turn")
    void grantedAbilityWearsOffAtEndOfTurn() {
        Permanent attacker = addAttackingBears();
        addToughBlocker();
        castOn(attacker);

        assertThat(attacker.getTemporaryTriggeredEffects(EffectSlot.ON_DAMAGED_CREATURE_DIES)).isNotEmpty();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getTemporaryTriggeredEffects(EffectSlot.ON_DAMAGED_CREATURE_DIES)).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new TouchOfMoonglove()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addAttackingBears() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        return attacker;
    }

    /** A 0/5 blocker: it survives 3 ordinary damage, so only deathtouch can kill it. */
    private Permanent addToughBlocker() {
        GrizzlyBears wallCard = new GrizzlyBears();
        wallCard.setPower(0);
        wallCard.setToughness(5);
        Permanent blocker = new Permanent(wallCard);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        return blocker;
    }

    private void castOn(Permanent creature) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new TouchOfMoonglove()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve Touch of Moonglove
    }
}
