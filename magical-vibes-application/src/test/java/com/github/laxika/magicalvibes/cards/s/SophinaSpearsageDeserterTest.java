package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SophinaSpearsageDeserter.class, GrizzlyBears.class})
class SophinaSpearsageDeserterTest extends BaseCardTest {

    @Test
    void attackingCreatesOneClueForEachNontokenAttacker() {
        addCreatureReady(player1, new SophinaSpearsageDeserter());
        addCreatureReady(player1, new GrizzlyBears());
        Card token = new GrizzlyBears();
        token.setToken(true);
        addCreatureReady(player1, token);

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }
}
