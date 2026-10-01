package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.SnowCoveredIsland;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeidarRimewindMaster.class, SnowCoveredIsland.class})
class HeidarRimewindMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target permanent to its owner's hand with four snow permanents")
    void returnsTargetPermanentWithFourSnowPermanents() {
        Permanent heidar = addCreatureReady(player1, new HeidarRimewindMaster());
        addSnowPermanents(player1, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SnowCoveredIsland());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Snow-Covered Island");
        harness.assertInHand(player2, "Snow-Covered Island");
        assertThat(heidar.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without four snow permanents you control")
    void cannotActivateWithoutFourSnowPermanentsYouControl() {
        addCreatureReady(player1, new HeidarRimewindMaster());
        addSnowPermanents(player1, 3);
        addSnowPermanents(player2, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SnowCoveredIsland());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("four or more snow permanents");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        addCreatureReady(player1, new HeidarRimewindMaster());
        addSnowPermanents(player1, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent heidar = addCreatureReady(player1, new HeidarRimewindMaster());
        addSnowPermanents(player1, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SnowCoveredIsland());
        heidar.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without paying the full generic cost")
    void cannotActivateWithoutFullGenericCost() {
        addCreatureReady(player1, new HeidarRimewindMaster());
        addSnowPermanents(player1, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SnowCoveredIsland());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addSnowPermanents(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new SnowCoveredIsland());
        }
    }
}
