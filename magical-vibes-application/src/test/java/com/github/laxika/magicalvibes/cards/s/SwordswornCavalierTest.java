package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KnightErrant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SwordswornCavalier.class, GrizzlyBears.class, KnightErrant.class})
class SwordswornCavalierTest extends BaseCardTest {

    @Test
    @DisplayName("Gains first strike after another Knight enters under its controller's control")
    void gainsFirstStrikeAfterAnotherKnightEnters() {
        Permanent cavalier = addCreatureReady(player1, new SwordswornCavalier());
        assertThat(gqs.hasKeyword(gd, cavalier, Keyword.FIRST_STRIKE)).isFalse();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cavalier, Keyword.FIRST_STRIKE)).isFalse();

        harness.castFromHand(player1, new KnightErrant(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cavalier, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Loses the conditional first strike at the end of the turn")
    void losesFirstStrikeAtEndOfTurn() {
        Permanent cavalier = addCreatureReady(player1, new SwordswornCavalier());

        harness.castFromHand(player1, new KnightErrant(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cavalier, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cavalier, Keyword.FIRST_STRIKE)).isFalse();
    }
}
