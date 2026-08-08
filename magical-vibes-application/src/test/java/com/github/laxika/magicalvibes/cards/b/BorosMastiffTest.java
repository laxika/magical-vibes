package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BorosMastiffTest extends BaseCardTest {

    @Test
    @DisplayName("Battalion grants lifelink to Boros Mastiff only")
    void battalionGrantsLifelink() {
        Permanent mastiff = addCreatureReady(player1, new BorosMastiff());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(mastiff.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(attacker.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Battalion does not trigger without two other attackers")
    void battalionDoesNotTriggerWithFewerThanTwoOtherAttackers() {
        Permanent mastiff = addCreatureReady(player1, new BorosMastiff());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(mastiff.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Lifelink from battalion gains life on combat damage")
    void lifelinkGainsLife() {
        addCreatureReady(player1, new BorosMastiff());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Battalion's lifelink grant wears off at end of turn")
    void lifelinkWearsOff() {
        Permanent mastiff = addCreatureReady(player1, new BorosMastiff());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();
        assertThat(mastiff.hasKeyword(Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mastiff.hasKeyword(Keyword.LIFELINK)).isFalse();
    }
}
