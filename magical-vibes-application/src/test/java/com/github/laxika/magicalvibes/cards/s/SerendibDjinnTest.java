package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SerendibDjinn.class, Island.class, Forest.class})
class SerendibDjinnTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an Island deals 3 damage to its controller")
    void sacrificingIslandDealsDamage() {
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new SerendibDjinn());
        UUID islandId = harness.getPermanentId(player1, "Island");

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, islandId);

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
        UUID forestId = harness.getPermanentId(player1, "Forest");

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, forestId);
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
}
