package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DeathWind;
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

@CardUsed({VulturousAven.class, GrizzlyBears.class, Forest.class, Island.class, DeathWind.class})
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

    @Test
    @DisplayName("Exploiting itself still draws two cards and loses 2 life")
    void exploitingItselfDrawsCardsAndLosesLife() {
        Forest forest = new Forest();
        Island island = new Island();
        harness.setLibrary(player1, List.of(forest, island));
        castVulturousAven();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Vulturous Aven"));
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(forest, island);
        harness.assertInGraveyard(player1, "Vulturous Aven");
        harness.assertNotOnBattlefield(player1, "Vulturous Aven");
    }

    @Test
    @DisplayName("Removing the source before exploit resolves prevents its draw and life loss")
    void removedSourceDoesNotReceiveExploitBonus() {
        Permanent sacrifice = addCreatureReady(player1, new VulturousAven());
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new VulturousAven()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> !p.getId().equals(sacrifice.getId()))
                .findFirst().orElseThrow();
        harness.setHand(player2, List.of(new DeathWind()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castInstant(player2, 0, 3, source.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Vulturous Aven");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof VulturousAven).hasSize(2);
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
