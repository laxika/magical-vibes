package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TerritorialDispute.class, Forest.class})
class TerritorialDisputeTest extends BaseCardTest {

    @Test
    @DisplayName("Without a land to sacrifice, the upkeep trigger sacrifices Territorial Dispute")
    void sacrificesItWithoutLand() {
        harness.addToBattlefield(player1, new TerritorialDispute());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Territorial Dispute");
    }

    @Test
    @DisplayName("The upkeep trigger offers a land sacrifice")
    void offersLandSacrifice() {
        harness.addToBattlefield(player1, new TerritorialDispute());
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Sacrificing a land keeps Territorial Dispute on the battlefield")
    void sacrificingLandKeepsIt() {
        harness.addToBattlefield(player1, new TerritorialDispute());
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Territorial Dispute");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Declining the land sacrifice sacrifices Territorial Dispute")
    void decliningLandSacrificeSacrificesIt() {
        harness.addToBattlefield(player1, new TerritorialDispute());
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Territorial Dispute");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("The upkeep trigger fires only during its controller's upkeep")
    void triggersOnlyDuringControllerUpkeep() {
        harness.addToBattlefield(player1, new TerritorialDispute());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Territorial Dispute");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's land cannot pay Territorial Dispute's upkeep cost")
    void onlyControllerCanPayUpkeepCost() {
        harness.addToBattlefield(player1, new TerritorialDispute());
        harness.addToBattlefield(player2, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Territorial Dispute");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("With multiple lands, the accepted upkeep cost lets the controller choose one")
    void choosesOneLandWhenSeveralAreAvailable() {
        harness.addToBattlefield(player1, new TerritorialDispute());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        var forestIds = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Forest"))
                .map(permanent -> permanent.getId())
                .toList();
        harness.handlePermanentChosen(player1, forestIds.getFirst());

        harness.assertOnBattlefield(player1, "Territorial Dispute");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Forest"))
                .hasSize(1);
    }

    @Test
    @CardUsed({Humility.class, Opalescence.class})
    @DisplayName("Removing Territorial Dispute's abilities restores land plays")
    void allowsLandPlaysWhenItsAbilitiesAreRemoved() {
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player1, new Humility());
        Permanent dispute = harness.addToBattlefieldAndReturn(player1, new TerritorialDispute());

        assertThat(gqs.isCreature(gd, dispute)).isTrue();
        assertThat(gqs.hasLostAllAbilities(gd, dispute)).isTrue();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Forest()));
        harness.clearPriorityPassed();
        harness.ensurePriority(player1);

        assertThat(harness.getGameActionAvailabilityService()
                .getPlayableCardIndices(gd, player1.getId())).containsExactly(0);
    }

    @Test
    @DisplayName("Territorial Dispute prevents every player from playing lands")
    void preventsAllLandPlays() {
        harness.addToBattlefield(player1, new TerritorialDispute());

        for (Player player : List.of(player1, player2)) {
            harness.forceActivePlayer(player);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.setHand(player, List.of(new Forest()));
            harness.clearPriorityPassed();
            harness.ensurePriority(player);

            assertThat(harness.getGameActionAvailabilityService()
                    .getPlayableCardIndices(gd, player.getId())).isEmpty();
        }
    }
}
