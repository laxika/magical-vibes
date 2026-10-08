package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WolfCoveVillager.class})
class WolfCoveVillagerTest extends BaseCardTest {

    @Test
    @DisplayName("Wolf Cove Villager enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.castFromHand(player1, new WolfCoveVillager(), "{W}");
        harness.passBothPriorities();

        Permanent villager = findPermanent(player1, "Wolf Cove Villager");
        assertThat(villager.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Wolf Cove Villager enters tapped even when it is not cast")
    void entersTappedWithoutBeingCast() {
        Permanent villager = harness.enterBattlefieldAndReturn(player2, new WolfCoveVillager());

        assertThat(villager.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering tapped does not prevent untapping during the controller's untap step")
    void untapsDuringControllersUntapStep() {
        harness.castFromHand(player1, new WolfCoveVillager(), "{W}");
        harness.passBothPriorities();
        Permanent villager = findPermanent(player1, "Wolf Cove Villager");
        assertThat(villager.isTapped()).isTrue();

        harness.performUntapStep(player2);
        assertThat(villager.isTapped()).isTrue();

        harness.performUntapStep(player1);
        assertThat(villager.isTapped()).isFalse();
    }
}
