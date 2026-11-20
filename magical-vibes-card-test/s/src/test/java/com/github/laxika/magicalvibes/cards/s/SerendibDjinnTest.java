package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.PhantasmalTerrain;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SerendibDjinn.class, Island.class, Forest.class, PhantasmalTerrain.class})
class SerendibDjinnTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an Island deals 3 damage to its controller")
    void sacrificingIslandDealsDamage() {
        var island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new SerendibDjinn());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(island.getId()));

        harness.assertLife(player1, 17);
        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Serendib Djinn");
    }

    @Test
    @DisplayName("Sacrificing a non-Island land deals no damage")
    void sacrificingNonIslandDealsNoDamage() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new SerendibDjinn());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Serendib Djinn");
        harness.assertInGraveyard(player1, "Serendib Djinn");
    }

    @Test
    @DisplayName("The state trigger sacrifices Serendib Djinn when its controller has no lands")
    void noLandsSacrificesDjinn() {
        addCreatureReady(player1, new SerendibDjinn());

        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Serendib Djinn");
        harness.assertInGraveyard(player1, "Serendib Djinn");
    }

    @Test
    @DisplayName("Sacrificing the last Island deals 3 damage and then Serendib Djinn is sacrificed")
    void sacrificingLastIslandDealsDamageAndSacrificesDjinn() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new SerendibDjinn());

        advanceToUpkeep(player1);
        resolveAllTriggers();
        resolveAllTriggers();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Island");
        harness.assertInGraveyard(player1, "Serendib Djinn");
    }

    @Test
    @DisplayName("An opponent's lands do not stop the no-lands trigger")
    void opponentsLandsDoNotPreventStateTrigger() {
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player1, new SerendibDjinn());

        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Serendib Djinn");
        harness.assertOnBattlefield(player2, "Island");
    }

    @Test
    @DisplayName("An opponent's upkeep does not require a sacrifice")
    void opponentsUpkeepDoesNotRequireSacrifice() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new SerendibDjinn());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player1, "Serendib Djinn");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Sacrificing a Forest that is an Island deals damage")
    void sacrificingForestChangedToIslandDealsDamage() {
        var forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        var terrain = harness.addToBattlefieldAndReturn(player1, new PhantasmalTerrain());
        terrain.setAttachedTo(forest.getId());
        terrain.setChosenSubtype(CardSubtype.ISLAND);
        harness.addToBattlefield(player1, new SerendibDjinn());

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId()));
        resolveAllTriggers();

        harness.assertLife(player1, 17);
        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player1, "Serendib Djinn");
    }

    @Test
    @DisplayName("Sacrificing an Island that is now a Forest deals no damage")
    void sacrificingIslandChangedToForestDealsNoDamage() {
        var island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.addToBattlefield(player1, new Forest());
        var terrain = harness.addToBattlefieldAndReturn(player1, new PhantasmalTerrain());
        terrain.setAttachedTo(island.getId());
        terrain.setChosenSubtype(CardSubtype.FOREST);
        harness.addToBattlefield(player1, new SerendibDjinn());

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(island.getId()));
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Island");
        harness.assertOnBattlefield(player1, "Serendib Djinn");
    }
}
