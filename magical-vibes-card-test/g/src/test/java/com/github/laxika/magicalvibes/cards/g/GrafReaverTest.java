package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SorinTheMirthless;
import com.github.laxika.magicalvibes.cards.t.TravelingMinister;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrafReaver.class, TravelingMinister.class, SorinTheMirthless.class})
class GrafReaverTest extends BaseCardTest {

    @Test
    @DisplayName("Declining exploit leaves Graf Reaver and the other creature on the battlefield")
    void decliningExploitDoesNothing() {
        harness.addToBattlefield(player1, new TravelingMinister());
        castGrafReaverToExploitPrompt();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Graf Reaver");
        harness.assertOnBattlefield(player1, "Traveling Minister");
    }

    @Test
    @DisplayName("Exploiting a creature destroys a target planeswalker")
    void exploitDestroysTargetPlaneswalker() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new TravelingMinister());
        Permanent planeswalker = addPlaneswalker(player2);
        castGrafReaverToExploitPrompt();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Graf Reaver");
        harness.assertNotOnBattlefield(player1, "Traveling Minister");
        harness.assertNotOnBattlefield(player2, "Sorin the Mirthless");
    }

    @Test
    @DisplayName("Exploit cannot target a creature")
    void exploitCannotTargetCreature() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new TravelingMinister());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new TravelingMinister());
        Permanent planeswalker = addPlaneswalker(player2);
        castGrafReaverToExploitPrompt();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).doesNotContain(creature.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Graf Reaver deals 1 damage to its controller during their upkeep")
    void dealsDamageDuringControllerUpkeep() {
        harness.addToBattlefield(player1, new GrafReaver());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Graf Reaver does not deal damage during an opponent's upkeep")
    void doesNotDealDamageDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new GrafReaver());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Graf Reaver can exploit itself and still destroy a planeswalker")
    void selfExploitDestroysPlaneswalker() {
        Permanent planeswalker = addPlaneswalker(player2);
        castGrafReaverToExploitPrompt();
        Permanent reaver = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof GrafReaver)
                .findFirst().orElseThrow();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, reaver.getId());
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Graf Reaver");
        harness.assertInGraveyard(player1, "Graf Reaver");
        harness.assertNotOnBattlefield(player2, "Sorin the Mirthless");
        harness.assertInGraveyard(player2, "Sorin the Mirthless");
    }

    @Test
    @DisplayName("A creature can be exploited even with no planeswalker to target")
    void exploitWithoutPlaneswalkerStillSacrificesCreature() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new TravelingMinister());
        castGrafReaverToExploitPrompt();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());

        harness.assertOnBattlefield(player1, "Graf Reaver");
        harness.assertInGraveyard(player1, "Traveling Minister");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The exploit trigger can destroy its controller's planeswalker")
    void exploitCanTargetOwnPlaneswalker() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new TravelingMinister());
        Permanent planeswalker = addPlaneswalker(player1);
        castGrafReaverToExploitPrompt();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Graf Reaver");
        harness.assertInGraveyard(player1, "Traveling Minister");
        harness.assertInGraveyard(player1, "Sorin the Mirthless");
    }

    @Test
    @DisplayName("Graf Reaver on the second player's battlefield damages only that player")
    void secondPlayerControllerTakesUpkeepDamage() {
        harness.addToBattlefield(player2, new GrafReaver());
        int firstLife = gd.playerLifeTotals.get(player1.getId());
        int secondLife = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, firstLife);
        harness.assertLife(player2, secondLife - 1);
    }

    private void castGrafReaverToExploitPrompt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new GrafReaver(), "{1}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addPlaneswalker(Player player) {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player, new SorinTheMirthless());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        return planeswalker;
    }
}
