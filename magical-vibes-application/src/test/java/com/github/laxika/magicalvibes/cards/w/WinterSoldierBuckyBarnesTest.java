package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(WinterSoldierBuckyBarnes.class)
class WinterSoldierBuckyBarnesTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        Permanent winterSoldier = harness.addToBattlefieldAndReturn(player1, new WinterSoldierBuckyBarnes());

        assertThat(winterSoldier.isTapped()).isTrue();
    }
}
