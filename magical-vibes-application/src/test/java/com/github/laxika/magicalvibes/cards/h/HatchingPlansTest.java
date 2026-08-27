package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HatchingPlans.class, GrizzlyBears.class})
class HatchingPlansTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards when it is put into a graveyard from the battlefield")
    void drawsThreeCardsWhenPutIntoGraveyardFromBattlefield() {
        Permanent hatchingPlans = harness.addToBattlefieldAndReturn(player1, new HatchingPlans());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, hatchingPlans));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
        harness.assertInGraveyard(player1, "Hatching Plans");
    }
}
