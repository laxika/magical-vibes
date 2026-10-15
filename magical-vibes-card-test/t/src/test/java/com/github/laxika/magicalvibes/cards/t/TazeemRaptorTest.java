package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TazeemRaptor.class, Plains.class})
class TazeemRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB can return a land you control to your hand")
    void etbReturnsChosenLand() {
        harness.addToBattlefield(player1, new Plains());
        UUID plainsId = harness.getPermanentId(player1, "Plains");
        castAndResolveRaptor();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(plainsId);

        harness.handlePermanentChosen(player1, plainsId);

        harness.assertOnBattlefield(player1, "Tazeem Raptor");
        harness.assertNotOnBattlefield(player1, "Plains");
        harness.assertInHand(player1, "Plains");
    }

    @Test
    @DisplayName("Declining the ETB return leaves the land on the battlefield")
    void decliningReturnLeavesLandInPlay() {
        harness.addToBattlefield(player1, new Plains());
        castAndResolveRaptor();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Tazeem Raptor");
        harness.assertOnBattlefield(player1, "Plains");
        harness.assertNotInHand(player1, "Plains");
    }

    @Test
    @DisplayName("The ETB choice excludes nonlands and lands controlled by an opponent")
    void etbOnlyOffersOwnLands() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new TazeemRaptor());
        harness.addToBattlefield(player2, new Plains());
        UUID ownPlainsId = harness.getPermanentId(player1, "Plains");
        castAndResolveRaptor();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(ownPlainsId);
    }

    @Test
    @DisplayName("Accepting the ETB with no lands finishes without a permanent choice")
    void acceptingReturnWithNoLandsDoesNothing() {
        harness.addToBattlefield(player2, new Plains());
        castAndResolveRaptor();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Tazeem Raptor");
        harness.assertOnBattlefield(player2, "Plains");
        harness.assertNotInHand(player1, "Plains");
    }

    @Test
    @DisplayName("The controller chooses exactly one of multiple lands to return")
    void returnsOnlyTheChosenLand() {
        UUID firstId = harness.addToBattlefieldAndReturn(player1, new Plains()).getId();
        UUID secondId = harness.addToBattlefieldAndReturn(player1, new Plains()).getId();
        castAndResolveRaptor();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(firstId, secondId);
        harness.handlePermanentChosen(player1, secondId);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getId()).contains(firstId).doesNotContain(secondId);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Plains");
    }

    @Test
    @DisplayName("A land controlled by you returns to its opponent owner's hand")
    void returnsLandToItsOwnerRatherThanItsController() {
        Plains land = new Plains();
        land.setOwnerId(player2.getId());
        UUID landId = harness.addToBattlefieldAndReturn(player1, land).getId();
        castAndResolveRaptor();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, landId);

        harness.assertNotOnBattlefield(player1, "Plains");
        harness.assertInHand(player2, "Plains");
        harness.assertNotInHand(player1, "Plains");
        harness.assertOnBattlefield(player1, "Tazeem Raptor");
    }

    private void castAndResolveRaptor() {
        harness.castFromHand(player1, new TazeemRaptor(), "{2}{W}");
        harness.passBothPriorities();
    }
}
