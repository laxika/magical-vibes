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

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, island.getId());

        harness.assertLife(player1, 17);
        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Serendib Djinn");
    }

    @Test
    @DisplayName("Sacrificing a non-Island land deals no damage")
    void sacrificingNonIslandDealsNoDamage() {
        var forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new SerendibDjinn());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, forest.getId());
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
    void sacrificingLastIslandDealsDamageAndSacrificesDjinn() {
        var island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.addToBattlefield(player1, new SerendibDjinn());

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, island.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Island");
        harness.assertInGraveyard(player1, "Serendib Djinn");
    }

    @Test
    void opponentsLandsDoNotPreventStateTrigger() {
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player1, new SerendibDjinn());

        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Serendib Djinn");
        harness.assertOnBattlefield(player2, "Island");
    }

    @Test
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
    void sacrificingForestChangedToIslandDealsDamage() {
        var forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        var terrain = harness.addToBattlefieldAndReturn(player1, new PhantasmalTerrain());
        terrain.setAttachedTo(forest.getId());
        terrain.setChosenSubtype(CardSubtype.ISLAND);
        harness.addToBattlefield(player1, new SerendibDjinn());

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, forest.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 17);
        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player1, "Serendib Djinn");
    }

    @Test
    void sacrificingIslandChangedToForestDealsNoDamage() {
        var island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.addToBattlefield(player1, new Forest());
        var terrain = harness.addToBattlefieldAndReturn(player1, new PhantasmalTerrain());
        terrain.setAttachedTo(island.getId());
        terrain.setChosenSubtype(CardSubtype.FOREST);
        harness.addToBattlefield(player1, new SerendibDjinn());

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, island.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Island");
        harness.assertOnBattlefield(player1, "Serendib Djinn");
    }
}
