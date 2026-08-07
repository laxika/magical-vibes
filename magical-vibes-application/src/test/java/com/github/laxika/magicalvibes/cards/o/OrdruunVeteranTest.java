package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
}
