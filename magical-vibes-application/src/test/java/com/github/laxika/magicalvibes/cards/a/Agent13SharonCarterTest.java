package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Agent13SharonCarter.class, GrizzlyBears.class})
class Agent13SharonCarterTest extends BaseCardTest {

    @Test
    void investigatesWhenACreatureAttacksAlone() {
        addCreatureReady(player1, new Agent13SharonCarter());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void doesNotInvestigateWhenMultipleCreaturesAttack() {
        addCreatureReady(player1, new Agent13SharonCarter());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }
}
