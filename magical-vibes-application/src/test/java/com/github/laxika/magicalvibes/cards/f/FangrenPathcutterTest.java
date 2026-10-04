package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DrossCrocodile;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FangrenPathcutter.class, DrossCrocodile.class})
class FangrenPathcutterTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creatures gain trample, but nonattacking creatures do not")
    void attackingCreaturesGainTrample() {
        Permanent pathcutter = addCreatureReady(player1, new FangrenPathcutter());
        Permanent attackingCreature = addCreatureReady(player1, new DrossCrocodile());
        Permanent nonattackingCreature = addCreatureReady(player1, new DrossCrocodile());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(pathcutter.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(attackingCreature.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(nonattackingCreature.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Attacking another creature does not trigger Fangren Pathcutter")
    void doesNotTriggerWhenPathcutterDoesNotAttack() {
        Permanent pathcutter = addCreatureReady(player1, new FangrenPathcutter());
        Permanent attackingCreature = addCreatureReady(player1, new DrossCrocodile());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(pathcutter.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(attackingCreature.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Granted trample wears off at end of turn")
    void grantedTrampleWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new FangrenPathcutter());
        Permanent attackingCreature = addCreatureReady(player1, new DrossCrocodile());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(attackingCreature.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attackingCreature.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Trample is granted only after the attack trigger resolves")
    void trampleWaitsForTriggerResolution() {
        Permanent pathcutter = addCreatureReady(player1, new FangrenPathcutter());
        Permanent attacker = addCreatureReady(player1, new DrossCrocodile());
        Permanent defender = addCreatureReady(player2, new DrossCrocodile());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.stack).hasSize(1);
        assertThat(pathcutter.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isFalse();

        resolveAllTriggers();

        assertThat(pathcutter.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(defender.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The attack trigger still grants trample after Pathcutter leaves the battlefield")
    void triggerResolvesWithoutItsSource() {
        Permanent pathcutter = addCreatureReady(player1, new FangrenPathcutter());
        Permanent attacker = addCreatureReady(player1, new DrossCrocodile());

        declareAttackers(player1, List.of(0, 1));
        assertThat(gd.stack).hasSize(1);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, pathcutter);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Fangren Pathcutter");
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Creatures entering after resolution do not gain trample")
    void laterCreaturesDoNotGainTrample() {
        Permanent pathcutter = addCreatureReady(player1, new FangrenPathcutter());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        Permanent laterCreature = addCreatureReady(player1, new DrossCrocodile());

        assertThat(pathcutter.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(laterCreature.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }
}
