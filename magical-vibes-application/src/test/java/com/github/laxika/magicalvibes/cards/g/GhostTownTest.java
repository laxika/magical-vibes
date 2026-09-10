package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(GhostTown.class)
class GhostTownTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability taps for {C}")
    void manaAbilityAddsColorless() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new GhostTown());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bounce ability returns the land to its owner's hand during an opponent's turn")
    void bouncesItselfOnOpponentsTurn() {
        harness.addToBattlefield(player1, new GhostTown());
        harness.forceActivePlayer(player2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ghost Town");
        harness.assertInHand(player1, "Ghost Town");
    }

    @Test
    @DisplayName("Bounce ability returns a controlled land to its owner's hand")
    void bouncesToOwnersHandWhenControllerDiffers() {
        GhostTown ghostTown = new GhostTown();
        ghostTown.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, ghostTown);
        harness.forceActivePlayer(player2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ghost Town");
        harness.assertInHand(player2, "Ghost Town");
        harness.assertNotInHand(player1, "Ghost Town");
    }

    @Test
    @DisplayName("Bounce ability cannot be activated on its controller's own turn")
    void cannotBounceOnOwnTurn() {
        harness.addToBattlefield(player1, new GhostTown());
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .hasMessageContaining("opponent's turn");

        harness.assertOnBattlefield(player1, "Ghost Town");
    }
}
