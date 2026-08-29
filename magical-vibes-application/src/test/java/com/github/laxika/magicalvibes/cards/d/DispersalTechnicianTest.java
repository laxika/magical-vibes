package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FireDiamond;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DispersalTechnicianTest extends BaseCardTest {

    @Test
    @DisplayName("ETB targets any artifact and returns it to its owner's hand")
    void etbReturnsTargetArtifactToOwnersHand() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new FireDiamond());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new FireDiamond());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castDispersalTechnician();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownArtifact.getId(), opponentArtifact.getId());
        assertThat(choice.validIds()).doesNotContain(harness.getPermanentId(player2, "Grizzly Bears"));

        harness.handlePermanentChosen(player1, opponentArtifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player2, "Fire Diamond");
        harness.assertNotOnBattlefield(player2, "Fire Diamond");
        harness.assertOnBattlefield(player1, "Dispersal Technician");
    }

    @Test
    @DisplayName("Declining the ETB may ability leaves the artifact on the battlefield")
    void decliningMayDoesNotReturnArtifact() {
        harness.addToBattlefield(player1, new FireDiamond());

        castDispersalTechnician();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Fire Diamond"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Fire Diamond");
        harness.assertOnBattlefield(player1, "Dispersal Technician");
    }

    @Test
    @DisplayName("ETB does not trigger when no artifact is on the battlefield")
    void noTriggerWithoutArtifactTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castDispersalTechnician();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Dispersal Technician");
    }

    private void castDispersalTechnician() {
        harness.setHand(player1, List.of(new DispersalTechnician()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
