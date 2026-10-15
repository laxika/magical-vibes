package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DrownedRusalka;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HatchingPlans.class, DrownedRusalka.class})
class HatchingPlansTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards when it is put into a graveyard from the battlefield")
    void drawsThreeCardsWhenPutIntoGraveyardFromBattlefield() {
        Permanent hatchingPlans = harness.addToBattlefieldAndReturn(player1, new HatchingPlans());
        harness.setLibrary(player1, List.of(new DrownedRusalka(), new DrownedRusalka(), new DrownedRusalka()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, hatchingPlans));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
        harness.assertInGraveyard(player1, "Hatching Plans");
    }

    @Test
    @DisplayName("Does not trigger when discarded from hand")
    void doesNotTriggerWhenDiscardedFromHand() {
        Permanent rusalka = addCreatureReady(player1, new DrownedRusalka());
        Permanent fodder = addCreatureReady(player1, new DrownedRusalka());
        HatchingPlans hatchingPlans = new HatchingPlans();
        DrownedRusalka firstDrawn = new DrownedRusalka();
        DrownedRusalka secondDrawn = new DrownedRusalka();
        DrownedRusalka thirdDrawn = new DrownedRusalka();
        DrownedRusalka fourthDrawn = new DrownedRusalka();
        harness.setHand(player1, List.of(hatchingPlans));
        harness.setLibrary(player1, List.of(firstDrawn, secondDrawn, thirdDrawn, fourthDrawn));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDrawn);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(fodder.getCard(), hatchingPlans);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(rusalka);
    }

    @Test
    @DisplayName("Draws for its last controller rather than its owner")
    void drawsForControllerWhenOwnedByOpponent() {
        HatchingPlans card = new HatchingPlans();
        card.setOwnerId(player1.getId());
        Permanent hatchingPlans = harness.addToBattlefieldAndReturn(player2, card);
        DrownedRusalka firstDrawn = new DrownedRusalka();
        DrownedRusalka secondDrawn = new DrownedRusalka();
        DrownedRusalka thirdDrawn = new DrownedRusalka();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new DrownedRusalka(), new DrownedRusalka(), new DrownedRusalka()));
        harness.setLibrary(player2, List.of(firstDrawn, secondDrawn, thirdDrawn));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, hatchingPlans));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(firstDrawn, secondDrawn, thirdDrawn);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Hatching Plans");
        harness.assertNotInGraveyard(player2, "Hatching Plans");
    }

    @Test
    @DisplayName("Does not trigger when exiled from the battlefield")
    void doesNotTriggerWhenExiled() {
        Permanent hatchingPlans = harness.addToBattlefieldAndReturn(player1, new HatchingPlans());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new DrownedRusalka(), new DrownedRusalka(), new DrownedRusalka()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, hatchingPlans));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(hatchingPlans.getCard().getId())).isNotNull();
        harness.assertNotInGraveyard(player1, "Hatching Plans");
    }

    @Test
    @DisplayName("Does not trigger when returned to hand from the battlefield")
    void doesNotTriggerWhenReturnedToHand() {
        HatchingPlans card = new HatchingPlans();
        Permanent hatchingPlans = harness.addToBattlefieldAndReturn(player1, card);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new DrownedRusalka(), new DrownedRusalka(), new DrownedRusalka()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, hatchingPlans));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        harness.assertNotInGraveyard(player1, "Hatching Plans");
    }
}
