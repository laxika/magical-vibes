package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IntangibleVibes.class, GrizzlyBears.class})
class IntangibleVibesTest extends BaseCardTest {

    @Test
    void makesAllCreaturesTokensAndTheyCeaseAfterLeavingBattlefield() {
        harness.addToBattlefield(player1, new IntangibleVibes());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.isToken(gd, bears)).isTrue();

        harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, bears);
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting("name")
                .containsExactly("Grizzly Bears");
        harness.passBothPriorities();
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }
}
