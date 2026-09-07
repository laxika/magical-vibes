package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VulturousAven.class, GrizzlyBears.class, Forest.class, Island.class})
class VulturousAvenTest extends BaseCardTest {

    @Test
    @DisplayName("Declining exploit does not draw cards or cause life loss")
    void decliningExploitDoesNothing() {
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        castVulturousAven();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Vulturous Aven");
    }

    @Test
    @DisplayName("Exploiting a creature draws two cards and causes 2 life loss")
    void exploitDrawsCardsAndLosesLife() {
        Permanent sacrifice = addCreatureReady(player1, new GrizzlyBears());
        Forest drawnForest = new Forest();
        Island drawnIsland = new Island();
        harness.setLibrary(player1, List.of(drawnForest, drawnIsland));

        castVulturousAven();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(drawnForest, drawnIsland);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Vulturous Aven");
    }

    private void castVulturousAven() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new VulturousAven()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
