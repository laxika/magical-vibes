package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ScarwoodTreefolk.class)
class ScarwoodTreefolkTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.castFromHand(player1, new ScarwoodTreefolk(), "{3}{G}");
        harness.passBothPriorities();

        Permanent treefolk = findPermanent(player1, "Scarwood Treefolk");
        assertThat(treefolk.isTapped()).isTrue();
    }
}
