package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BondedConstruct;
import com.github.laxika.magicalvibes.cards.g.GuardiansOfMeletis;
import com.github.laxika.magicalvibes.cards.r.ReaveSoul;
import com.github.laxika.magicalvibes.cards.s.ShamblingGhoul;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TouchOfMoonglove.class, ShamblingGhoul.class, GuardiansOfMeletis.class,
        BondedConstruct.class, ReaveSoul.class})
class TouchOfMoongloveTest extends BaseCardTest {

    @Test
    @DisplayName("Deathtouch damage from the buffed creature kills the blocker and its controller loses 2 life")
    void deathtouchKillDrainsBlockersController() {
        Permanent attacker = addAttackingGhoul();
        Permanent blocker = addToughBlocker();
        castOn(attacker);

        harness.passBothPriorities(); // combat damage — deathtouch destroys the 0/6 blocker
        harness.passBothPriorities(); // resolve the "its controller loses 2 life" trigger

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The +1/+0 boost applies to the targeted creature")
    void boostsTargetPower() {
        Permanent attacker = addAttackingGhoul();
        addToughBlocker();
        castOn(attacker);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
    }

    @Test
    @DisplayName("A creature that was not targeted drains nobody when its victim dies")
    void untargetedCreatureDoesNotDrain() {
        Permanent attacker = addAttackingGhoul();
        Permanent other = addCreatureReady(player1, new ShamblingGhoul());
        other.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new BondedConstruct());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(1);

        castOn(attacker);

        harness.passBothPriorities(); // combat damage — the untargeted ghoul kills the 2/1
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        // Player 2 only took the unblocked attacker's combat damage, never the 2-life drain.
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The boost, deathtouch, and death trigger expire at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent attacker = addAttackingGhoul();
        Permanent blocker = addToughBlocker();
        castOn(attacker);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.resolveCombatDamage();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);

        destroyWithReaveSoul(blocker);
        resolveAllTriggers();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The delayed trigger survives the targeted creature leaving the battlefield")
    void delayedTriggerSurvivesSourceLeaving() {
        Permanent attacker = addAttackingGhoul();
        Permanent blocker = addToughBlocker();
        dealDamageBeforeCasting(attacker, blocker);
        castOn(attacker);

        destroyWithReaveSoul(attacker);
        destroyWithReaveSoul(blocker);
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Damage dealt earlier this turn still qualifies for the delayed trigger")
    void damageBeforeSpellResolvesQualifies() {
        Permanent attacker = addAttackingGhoul();
        Permanent blocker = addToughBlocker();
        dealDamageBeforeCasting(attacker, blocker);
        castOn(attacker);

        destroyWithReaveSoul(blocker);
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Two resolutions create two independent delayed triggers")
    void repeatedResolutionsEachCauseLifeLoss() {
        Permanent attacker = addAttackingGhoul();
        Permanent blocker = addToughBlocker();
        castOn(attacker);
        castOn(attacker);

        harness.resolveCombatDamage();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        harness.assertLife(player2, 16);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new ShamblingGhoul());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new TouchOfMoonglove()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addAttackingGhoul() {
        Permanent attacker = addCreatureReady(player1, new ShamblingGhoul());
        attacker.setAttacking(true);
        return attacker;
    }

    /** A blocker that survives ordinary damage from the boosted attacker. */
    private Permanent addToughBlocker() {
        Permanent blocker = addCreatureReady(player2, new GuardiansOfMeletis());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        return blocker;
    }

    private void dealDamageBeforeCasting(Permanent attacker, Permanent blocker) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.resolveCombatDamage();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    private void destroyWithReaveSoul(Permanent creature) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ReaveSoul()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, creature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    private void castOn(Permanent creature) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new TouchOfMoonglove()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
    }
}
