package com.github.laxika.magicalvibes.cards.f;

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

@CardUsed({FrontierWarmonger.class, GrizzlyBears.class})
class FrontierWarmongerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creatures you control gain menace")
    void grantsMenaceToAttackers() {
        addCreatureReady(player1, new FrontierWarmonger());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonattacker, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Menace granted by Frontier Warmonger lasts only until end of turn")
    void menaceWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new FrontierWarmonger());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.MENACE)).isFalse();
    }
}
