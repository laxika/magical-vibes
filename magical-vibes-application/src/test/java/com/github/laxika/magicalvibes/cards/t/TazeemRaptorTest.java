package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TazeemRaptor.class, Plains.class, GrizzlyBears.class})
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
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Plains());
        UUID ownPlainsId = harness.getPermanentId(player1, "Plains");
        castAndResolveRaptor();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(ownPlainsId);
    }

    private void castAndResolveRaptor() {
        harness.setHand(player1, List.of(new TazeemRaptor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
