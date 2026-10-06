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

    @Test
    @DisplayName("Enters tapped even when put onto the battlefield without casting")
    void entersTappedWithoutCasting() {
        Permanent treefolk = harness.enterBattlefieldAndReturn(player2, new ScarwoodTreefolk());

        assertThat(treefolk.isTapped()).isTrue();
        harness.assertOnBattlefield(player2, "Scarwood Treefolk");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Untaps normally during its controller's untap step")
    void untapsNormally() {
        harness.castFromHand(player1, new ScarwoodTreefolk(), "{3}{G}");
        harness.passBothPriorities();
        Permanent treefolk = findPermanent(player1, "Scarwood Treefolk");
        assertThat(treefolk.isTapped()).isTrue();

        harness.performUntapStep(player2);
        assertThat(treefolk.isTapped()).isTrue();

        harness.performUntapStep(player1);
        assertThat(treefolk.isTapped()).isFalse();
    }
}
