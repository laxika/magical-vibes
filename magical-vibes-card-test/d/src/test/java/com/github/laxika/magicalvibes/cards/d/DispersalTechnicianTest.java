package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BaralChiefOfCompliance;
import com.github.laxika.magicalvibes.cards.p.PlanarBridge;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DispersalTechnician.class, PlanarBridge.class, BaralChiefOfCompliance.class})
class DispersalTechnicianTest extends BaseCardTest {

    @Test
    @DisplayName("ETB targets any artifact and returns it to its owner's hand")
    void etbReturnsTargetArtifactToOwnersHand() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new PlanarBridge());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new PlanarBridge());
        harness.addToBattlefield(player2, new BaralChiefOfCompliance());

        castDispersalTechnician();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownArtifact.getId(), opponentArtifact.getId());
        assertThat(choice.validIds()).doesNotContain(harness.getPermanentId(player2, "Baral, Chief of Compliance"));

        harness.handlePermanentChosen(player1, opponentArtifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player2, "Planar Bridge");
        harness.assertNotOnBattlefield(player2, "Planar Bridge");
        harness.assertOnBattlefield(player1, "Dispersal Technician");
    }

    @Test
    @DisplayName("Declining the ETB may ability leaves the artifact on the battlefield")
    void decliningMayDoesNotReturnArtifact() {
        harness.addToBattlefield(player1, new PlanarBridge());

        castDispersalTechnician();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Planar Bridge"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Planar Bridge");
        harness.assertOnBattlefield(player1, "Dispersal Technician");
    }

    @Test
    @DisplayName("ETB cannot remain on the stack without a legal artifact target")
    void noTriggerWithoutArtifactTarget() {
        harness.addToBattlefield(player2, new BaralChiefOfCompliance());

        castDispersalTechnician();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Dispersal Technician");
    }

    @Test
    @DisplayName("The ETB can return an artifact controlled by its controller")
    void returnsOwnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PlanarBridge());

        castDispersalTechnician();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Planar Bridge");
        harness.assertNotOnBattlefield(player1, "Planar Bridge");
    }

    @Test
    @DisplayName("A borrowed artifact returns to its owner's hand")
    void returnsBorrowedArtifactToOwner() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PlanarBridge());
        gd.stolenCreatures.put(artifact.getId(), player2.getId());

        castDispersalTechnician();
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player2, "Planar Bridge");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Planar Bridge");
    }

    @Test
    @DisplayName("The ETB resolves even after Dispersal Technician leaves the battlefield")
    void resolvesWithoutSource() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PlanarBridge());

        castDispersalTechnician();
        harness.handlePermanentChosen(player1, artifact.getId());
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Dispersal Technician"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player2, "Planar Bridge");
        harness.assertNotOnBattlefield(player2, "Planar Bridge");
    }

    @Test
    @DisplayName("An artifact that leaves before resolution is not returned from another zone")
    void doesNotReturnDepartedTarget() {
        harness.setHand(player2, List.of());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PlanarBridge());

        castDispersalTechnician();
        harness.handlePermanentChosen(player1, artifact.getId());
        gd.playerBattlefields.get(player2.getId()).remove(artifact);
        gd.playerGraveyards.get(player2.getId()).add(artifact.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Planar Bridge");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    private void castDispersalTechnician() {
        harness.castFromHand(player1, new DispersalTechnician(), "{4}{U}");
        harness.passBothPriorities();
    }
}
