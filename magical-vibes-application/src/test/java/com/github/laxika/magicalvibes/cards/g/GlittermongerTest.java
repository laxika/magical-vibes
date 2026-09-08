package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Glittermonger.class})
class GlittermongerTest extends BaseCardTest {

    @Test
    void tapsToCreateTreasureToken() {
        Permanent glittermonger = harness.addToBattlefieldAndReturn(player1, new Glittermonger());
        glittermonger.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(glittermonger.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }
}
