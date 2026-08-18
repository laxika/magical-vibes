package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class IvoryCraneNetsukeTest extends BaseCardTest {

    private List<Card> bears(int count) {
        return Stream.generate(GrizzlyBears::new).limit(count).map(Card.class::cast).toList();
    }

    @Test
    void gainsFourLifeAtSevenCardsInHand() {
        harness.addToBattlefield(player1, new IvoryCraneNetsuke());
        harness.setHand(player1, bears(7));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    @Test
    void doesNotTriggerWithFewerThanSevenCardsInHand() {
        harness.addToBattlefield(player1, new IvoryCraneNetsuke());
        harness.setHand(player1, bears(6));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void doesNotGainLifeIfHandFallsBelowThresholdBeforeResolution() {
        harness.addToBattlefield(player1, new IvoryCraneNetsuke());
        harness.setHand(player1, bears(7));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        gd.playerHands.get(player1.getId()).remove(0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }
}
