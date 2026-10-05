package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrdruunVeteran.class})
class OrdruunVeteranTest extends BaseCardTest {

    @Test
    @DisplayName("Does not gain double strike when attacking with fewer than two other creatures")
    void noDoubleStrikeWithFewerThanTwoOtherAttackers() {
        Permanent veteran = addCreatureReady(player1, new OrdruunVeteran());
        addCreatureReady(player1, new OrdruunVeteran());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, veteran, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Gains double strike when attacking with two other creatures")
    void gainsDoubleStrikeWithTwoOtherAttackers() {
        Permanent veteran = addCreatureReady(player1, new OrdruunVeteran());
        addCreatureReady(player1, new OrdruunVeteran());
        addCreatureReady(player1, new OrdruunVeteran());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, veteran, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Double strike wears off at end of turn")
    void doubleStrikeWearsOffAtEndOfTurn() {
        Permanent veteran = addCreatureReady(player1, new OrdruunVeteran());
        addCreatureReady(player1, new OrdruunVeteran());
        addCreatureReady(player1, new OrdruunVeteran());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, veteran, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, veteran, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("A nonattacking Veteran does not gain double strike when three other creatures attack")
    void nonattackingVeteranDoesNotGainDoubleStrike() {
        Permanent veteran = addCreatureReady(player1, new OrdruunVeteran());
        addCreatureReady(player1, new OrdruunVeteran());
        addCreatureReady(player1, new OrdruunVeteran());
        addCreatureReady(player1, new OrdruunVeteran());

        declareAttackers(player1, List.of(1, 2, 3));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, veteran, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Battalion still resolves after the other attackers leave combat")
    void gainsDoubleStrikeAfterOtherAttackersLeaveCombat() {
        Permanent veteran = addCreatureReady(player1, new OrdruunVeteran());
        Permanent otherAttacker = addCreatureReady(player1, new OrdruunVeteran());
        Permanent thirdAttacker = addCreatureReady(player1, new OrdruunVeteran());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0, 1, 2)));
        assertThat(gd.stack).hasSize(3);
        assertThat(gqs.hasKeyword(gd, veteran, Keyword.DOUBLE_STRIKE)).isFalse();

        otherAttacker.setAttacking(false);
        thirdAttacker.setAttacking(false);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, veteran, Keyword.DOUBLE_STRIKE)).isTrue();
    }
}
