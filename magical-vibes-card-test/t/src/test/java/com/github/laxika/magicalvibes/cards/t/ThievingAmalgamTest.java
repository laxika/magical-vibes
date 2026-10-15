package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThievingAmalgam.class, GrizzlyBears.class})
class ThievingAmalgamTest extends BaseCardTest {

    @Test
    @DisplayName("Manifests the top card of each opponent's library under your control")
    void manifestsTopCardOfOpponentsLibrary() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));
        harness.addToBattlefield(player1, new ThievingAmalgam());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(topCard.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)).isNull();
    }

    @Test
    @DisplayName("A creature you control but do not own dying drains its owner")
    void stolenCreatureDeathDrainsItsOwner() {
        harness.addToBattlefield(player1, new ThievingAmalgam());
        Card stolenCard = new GrizzlyBears();
        stolenCard.setOwnerId(player2.getId());
        Permanent stolenCreature = harness.addToBattlefieldAndReturn(player1, stolenCard);
        gd.stolenCreatures.put(stolenCreature.getId(), player2.getId());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        stolenCreature.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 8);
    }

    @Test
    @DisplayName("A creature you both control and own dying does not trigger the drain")
    void ownCreatureDeathDoesNotDrain() {
        harness.addToBattlefield(player1, new ThievingAmalgam());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        ownCreature.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 10);
    }

    @Test
    @DisplayName("A stolen Thieving Amalgam drains its owner when it dies itself")
    void stolenAmalgamDeathDrainsItsOwner() {
        Card stolenCard = new ThievingAmalgam();
        stolenCard.setOwnerId(player2.getId());
        Permanent amalgam = harness.addToBattlefieldAndReturn(player1, stolenCard);
        gd.stolenCreatures.put(amalgam.getId(), player2.getId());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        amalgam.setMarkedDamage(7);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Thieving Amalgam");
        harness.assertLife(player1, 12);
        harness.assertLife(player2, 8);
    }

    @Test
    @DisplayName("A manifested opponent-owned creature dying drains its owner")
    void manifestedCreatureDeathDrainsItsOwner() {
        Card topCard = new ThievingAmalgam();
        topCard.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(topCard));
        harness.addToBattlefield(player1, new ThievingAmalgam());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        advanceToUpkeep(player2);
        resolveAllTriggers();
        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested).findFirst().orElseThrow();
        manifested.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Thieving Amalgam");
        harness.assertLife(player1, 12);
        harness.assertLife(player2, 8);
    }

    @Test
    @DisplayName("Your own upkeep does not manifest a card")
    void ownUpkeepDoesNotManifest() {
        Card topCard = new ThievingAmalgam();
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player1, new ThievingAmalgam());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isManifested);
    }

    @Test
    @DisplayName("An empty opponent library does not prevent the upkeep trigger resolving")
    void emptyOpponentLibraryDoesNothing() {
        harness.setLibrary(player2, List.of());
        harness.addToBattlefield(player1, new ThievingAmalgam());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The drain still triggers when Amalgam dies alongside a stolen creature")
    void simultaneousDeathStillDrainsStolenCreaturesOwner() {
        Permanent amalgam = harness.addToBattlefieldAndReturn(player1, new ThievingAmalgam());
        Card stolenCard = new ThievingAmalgam();
        stolenCard.setOwnerId(player2.getId());
        Permanent stolen = harness.addToBattlefieldAndReturn(player1, stolenCard);
        gd.stolenCreatures.put(stolen.getId(), player2.getId());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        amalgam.setMarkedDamage(7);
        stolen.setMarkedDamage(7);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 14);
        harness.assertLife(player2, 6);
    }

    @Test
    @DisplayName("An opponent-controlled creature dying does not trigger the drain")
    void opponentControlledCreatureDeathDoesNotDrain() {
        harness.addToBattlefield(player1, new ThievingAmalgam());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new ThievingAmalgam());
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        opposing.setMarkedDamage(7);
        harness.runStateBasedActions();
        resolveAllTriggers();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 10);
    }
}
