package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WildCelebrantsTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the may destroys the chosen artifact")
    void acceptingDestroysTargetArtifact() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        UUID targetId = harness.getPermanentId(player2, "Leonin Scimitar");

        castWildCelebrants();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, targetId);

        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
        harness.assertOnBattlefield(player1, "Wild Celebrants");
    }

    @Test
    @DisplayName("Declining the may leaves the artifact on the battlefield")
    void decliningLeavesArtifact() {
        harness.addToBattlefield(player2, new LeoninScimitar());

        castWildCelebrants();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Leonin Scimitar");
        harness.assertOnBattlefield(player1, "Wild Celebrants");
    }

    @Test
    @DisplayName("No trigger is created when there is no artifact to destroy")
    void noTriggerWithoutArtifact() {
        castWildCelebrants();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wild Celebrants");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature cannot be chosen as the target")
    void creatureIsNotALegalTarget() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");

        castWildCelebrants();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creatureId))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void castWildCelebrants() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WildCelebrants()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
    }
}
