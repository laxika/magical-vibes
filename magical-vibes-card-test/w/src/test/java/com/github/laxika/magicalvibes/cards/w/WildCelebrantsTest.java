package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WildCelebrants.class, LeoninScimitar.class, GrizzlyBears.class, BronzeSable.class})
class WildCelebrantsTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the may destroys the chosen artifact")
    void acceptingDestroysTargetArtifact() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        UUID targetId = harness.getPermanentId(player2, "Leonin Scimitar");

        castWildCelebrants();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
        harness.assertOnBattlefield(player1, "Wild Celebrants");
    }

    @Test
    @DisplayName("Declining the may leaves the artifact on the battlefield")
    void decliningLeavesArtifact() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        UUID targetId = harness.getPermanentId(player2, "Leonin Scimitar");

        castWildCelebrants();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
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

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creatureId))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The controller may destroy their own artifact")
    void canDestroyOwnArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar()).getId();

        castWildCelebrants();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Leonin Scimitar");
        harness.assertInGraveyard(player1, "Leonin Scimitar");
        harness.assertOnBattlefield(player1, "Wild Celebrants");
    }

    @Test
    @DisplayName("An artifact creature is a legal target")
    void canDestroyArtifactCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new BronzeSable()).getId();

        castWildCelebrants();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Bronze Sable");
        harness.assertInGraveyard(player2, "Bronze Sable");
        harness.assertOnBattlefield(player1, "Wild Celebrants");
    }

    private void castWildCelebrants() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new WildCelebrants(), "{3}{R}{R}");
    }
}
