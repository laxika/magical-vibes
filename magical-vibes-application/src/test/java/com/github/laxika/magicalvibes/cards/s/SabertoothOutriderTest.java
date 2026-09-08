package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SabertoothOutrider.class, GrizzlyBears.class})
class SabertoothOutriderTest extends BaseCardTest {

    @Test
    @DisplayName("Gains first strike when it attacks with 8 total power")
    void gainsFirstStrikeWithEnoughTotalPower() {
        Permanent outrider = addCreatureReady(player1, new SabertoothOutrider());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, outrider, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger when creatures you control have less than 8 total power")
    void doesNotTriggerWithInsufficientTotalPower() {
        Permanent outrider = addCreatureReady(player1, new SabertoothOutrider());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, outrider, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("First strike wears off at end of turn")
    void firstStrikeWearsOffAtEndOfTurn() {
        Permanent outrider = addCreatureReady(player1, new SabertoothOutrider());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, outrider, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, outrider, Keyword.FIRST_STRIKE)).isFalse();
    }
}
