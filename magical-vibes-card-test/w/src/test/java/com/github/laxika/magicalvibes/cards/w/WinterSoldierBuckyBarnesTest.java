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
        Permanent winterSoldier = harness.enterBattlefieldAndReturn(player1, new WinterSoldierBuckyBarnes());

        assertThat(winterSoldier.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters tapped when its creature spell resolves")
    void entersTappedWhenCast() {
        harness.castFromHand(player1, new WinterSoldierBuckyBarnes(), "{W}");
        harness.passBothPriorities();

        Permanent winterSoldier = findPermanent(player1, "Winter Soldier, Bucky Barnes");
        assertThat(winterSoldier.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering tapped does not prevent untapping during its controller's untap step")
    void untapsNormallyAfterEnteringTapped() {
        Permanent winterSoldier = harness.enterBattlefieldAndReturn(player1, new WinterSoldierBuckyBarnes());
        assertThat(winterSoldier.isTapped()).isTrue();

        harness.performUntapStep(player2);
        assertThat(winterSoldier.isTapped()).isTrue();

        harness.performUntapStep(player1);
        assertThat(winterSoldier.isTapped()).isFalse();
    }
}
