package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.s.SeatOfTheSynod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Regress.class, AlphaMyr.class, SeatOfTheSynod.class})
class RegressTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target land permanent to its owner's hand")
    void returnsTargetLandToOwnersHand() {
        harness.addToBattlefield(player2, new SeatOfTheSynod());
        harness.setHand(player1, List.of(new Regress()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Seat of the Synod");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Seat of the Synod");
        harness.assertInHand(player2, "Seat of the Synod");
        harness.assertInGraveyard(player1, "Regress");
    }

    @Test
    @DisplayName("Returns a target nonland permanent to its owner's hand")
    void returnsTargetNonlandPermanentToOwnersHand() {
        harness.addToBattlefield(player2, new AlphaMyr());
        harness.setHand(player1, List.of(new Regress()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player2, "Alpha Myr");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Alpha Myr");
        harness.assertInHand(player2, "Alpha Myr");
        harness.assertNotInGraveyard(player2, "Alpha Myr");
    }

    @Test
    @DisplayName("Returns a target permanent to its owner's hand rather than its controller's")
    void returnsTargetToOwnersHandWhenControllerDiffers() {
        AlphaMyr targetCard = new AlphaMyr();
        targetCard.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);
        harness.setHand(player1, List.of(new Regress()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Alpha Myr");
        harness.assertInHand(player1, "Alpha Myr");
        harness.assertNotInHand(player2, "Alpha Myr");
    }

    @Test
    @DisplayName("Fizzles if the target permanent leaves before resolution")
    void fizzlesIfTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlphaMyr());
        harness.setHand(player1, List.of(new Regress()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Alpha Myr");
        harness.assertNotInHand(player2, "Alpha Myr");
        harness.assertInGraveyard(player1, "Regress");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new Regress()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("This spell cannot target players");
    }
}
