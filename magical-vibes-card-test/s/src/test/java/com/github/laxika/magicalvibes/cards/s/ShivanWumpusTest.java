package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.ReveredDead;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShivanWumpus.class, Forest.class, ReveredDead.class})
class ShivanWumpusTest extends BaseCardTest {

    private void castAndResolveToChoice() {
        harness.addToBattlefield(player2, new Forest());
        castAndResolveWumpus();
    }

    private void castAndResolveWumpus() {
        harness.castFromHand(player1, new ShivanWumpus(), "{3}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Declining leaves the Wumpus and land on the battlefield")
    void decliningKeepsBothPermanents() {
        castAndResolveToChoice();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player1, "Shivan Wumpus");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Sacrificing a land puts the Wumpus on top of its owner's library")
    void sacrificingLandTucksWumpus() {
        castAndResolveToChoice();

        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player2, "Forest");
        harness.assertNotOnBattlefield(player1, "Shivan Wumpus");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName())
                .isEqualTo("Shivan Wumpus");
    }

    @Test
    @DisplayName("Remaining players still receive the choice after a land is sacrificed")
    void remainingPlayersStillGetChoice() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        castAndResolveWumpus();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName())
                .isEqualTo("Shivan Wumpus");
    }

    @Test
    @DisplayName("An accepting player with multiple lands chooses which land to sacrifice")
    void choosesLandWhenSeveralAreControlled() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        castAndResolveWumpus();

        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("Sacrificing the chosen land puts the Wumpus on top of its owner's library")
    void sacrificingChosenLandTucksWumpus() {
        Permanent firstLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        castAndResolveWumpus();

        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, secondLand.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(firstLand);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(secondLand.getCard());
        harness.assertNotOnBattlefield(player1, "Shivan Wumpus");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName())
                .isEqualTo("Shivan Wumpus");
    }

    @Test
    @DisplayName("All players can decline the land sacrifice")
    void allPlayersDecline() {
        harness.addToBattlefield(player1, new Forest());
        castAndResolveToChoice();

        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player1, "Shivan Wumpus");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Forest"))
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Forest"))
                .hasSize(1);
    }

    @Test
    @DisplayName("No player is offered the choice when no player controls a land")
    void noChoiceWithoutLands() {
        castAndResolveWumpus();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Shivan Wumpus");
    }

    @Test
    @DisplayName("Both players may sacrifice even after the Wumpus has left the battlefield")
    void bothPlayersCanSacrifice() {
        harness.addToBattlefield(player1, new Forest());
        castAndResolveToChoice();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        harness.assertNotOnBattlefield(player1, "Shivan Wumpus");
        assertThat(gd.playerDecks.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Shivan Wumpus"))
                .hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName())
                .isEqualTo("Shivan Wumpus");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The opponent may sacrifice after the active player declines")
    void opponentSacrificesAfterActivePlayerDeclines() {
        harness.addToBattlefield(player1, new Forest());
        castAndResolveToChoice();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        harness.assertNotOnBattlefield(player1, "Shivan Wumpus");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName())
                .isEqualTo("Shivan Wumpus");
    }

    @Test
    @DisplayName("A land sacrifice tucks only the Wumpus whose entry triggered the ability")
    void sacrificeTucksOnlyTheEnteringWumpus() {
        Permanent earlierWumpus = harness.addToBattlefieldAndReturn(player1, new ShivanWumpus());
        castAndResolveToChoice();
        Permanent enteringWumpus = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(earlierWumpus.getId()))
                .findFirst().orElseThrow();

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(earlierWumpus);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(enteringWumpus.getCard());
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Trample assigns excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new ShivanWumpus());
        Permanent blocker = addCreatureReady(player2, new ReveredDead());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 5
        ));

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        harness.assertInGraveyard(player2, "Revered Dead");
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }
}
