package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinFirebug.class, Forest.class, Shock.class})
class GoblinFirebugTest extends BaseCardTest {

    @Test
    @DisplayName("When Goblin Firebug leaves the battlefield, its controller sacrifices a land")
    void sacrificesLandWhenLeavingBattlefield() {
        Permanent firebug = addCreatureReady(player1, new GoblinFirebug());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, firebug.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Goblin Firebug");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("The leave trigger does nothing when its controller controls no lands")
    void doesNothingWithoutAControllerLand() {
        Permanent firebug = addCreatureReady(player1, new GoblinFirebug());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, firebug.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Goblin Firebug");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("The leave trigger also fires when Goblin Firebug returns to its owner's hand")
    void sacrificesLandWhenReturningToHand() {
        Permanent firebug = addCreatureReady(player1, new GoblinFirebug());
        harness.addToBattlefield(player1, new Forest());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, firebug));
        resolveAllTriggers();

        harness.assertInHand(player1, "Goblin Firebug");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("The controller chooses exactly one land when multiple lands are available")
    void controllerChoosesOneLandToSacrifice() {
        Permanent firebug = addCreatureReady(player1, new GoblinFirebug());
        Permanent firstForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent secondForest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, firebug));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(secondForest.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firstForest);
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(secondForest);
    }

    @Test
    @DisplayName("Exiling Goblin Firebug also causes its controller to sacrifice a land")
    void sacrificesLandWhenExiled() {
        Permanent firebug = addCreatureReady(player1, new GoblinFirebug());
        harness.addToBattlefield(player1, new Forest());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, firebug));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Goblin Firebug");
        harness.assertNotInGraveyard(player1, "Goblin Firebug");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("The controller before leaving sacrifices a land, even when someone else owns Goblin Firebug")
    void controllerRatherThanOwnerSacrificesLand() {
        GoblinFirebug card = new GoblinFirebug();
        card.setOwnerId(player1.getId());
        Permanent firebug = addCreatureReady(player2, card);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, firebug));
        resolveAllTriggers();

        harness.assertInHand(player1, "Goblin Firebug");
        harness.assertInGraveyard(player2, "Forest");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("The trigger sacrifices a land acquired after Goblin Firebug leaves the battlefield")
    void checksAvailableLandsAtResolution() {
        Permanent firebug = addCreatureReady(player1, new GoblinFirebug());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, firebug));
        harness.addToBattlefield(player1, new Forest());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Goblin Firebug");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
    }
}
