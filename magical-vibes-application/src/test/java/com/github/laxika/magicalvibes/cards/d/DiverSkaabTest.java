package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.PersistentSpecimen;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiverSkaab.class, PersistentSpecimen.class, Island.class})
class DiverSkaabTest extends BaseCardTest {

    @Test
    @DisplayName("Declining exploit leaves Diver Skaab on the battlefield")
    void decliningExploitDoesNothing() {
        castDiverSkaab();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Diver Skaab");
    }

    @Test
    @DisplayName("Exploit sacrifices a creature and lets its owner put a target creature on top")
    void exploitPutsTargetCreatureOnTop() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new PersistentSpecimen());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PersistentSpecimen());
        Card libraryCard = new Island();
        harness.setLibrary(player2, List.of(libraryCard));

        castDiverSkaab();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.TargetLibraryDestinationChoice.class)
                .playerId()).isEqualTo(player2.getId());

        harness.handleListChoice(player2, "Top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard(), libraryCard);
        harness.assertNotOnBattlefield(player1, "Persistent Specimen");
        harness.assertOnBattlefield(player1, "Diver Skaab");
        harness.assertInGraveyard(player1, "Persistent Specimen");
    }

    @Test
    @DisplayName("Exploit cannot target a land")
    void exploitCannotTargetLand() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new PersistentSpecimen());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PersistentSpecimen());

        castDiverSkaab();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).contains(target.getId());
        assertThat(choice.validIds()).doesNotContain(land.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Top");
    }

    @Test
    @DisplayName("The target's owner may choose the bottom of their library")
    void exploitPutsTargetCreatureOnBottom() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new PersistentSpecimen());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PersistentSpecimen());
        Card first = new Island();
        Card second = new Island();
        harness.setLibrary(player2, List.of(first, second));

        castDiverSkaab();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(first, second, target.getCard());
        harness.assertNotOnBattlefield(player2, "Persistent Specimen");
    }

    @Test
    @DisplayName("Diver Skaab can exploit itself and still put another creature into its library")
    void sacrificingItselfStillTriggersLibraryPlacement() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PersistentSpecimen());
        harness.setLibrary(player2, List.of());
        castDiverSkaab();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Diver Skaab"));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Bottom");

        harness.assertInGraveyard(player1, "Diver Skaab");
        harness.assertNotOnBattlefield(player1, "Diver Skaab");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard());
    }

    @Test
    @DisplayName("A creature's owner chooses its destination even when an opponent controls it")
    void ownerRatherThanControllerChoosesDestination() {
        PersistentSpecimen stolenCard = new PersistentSpecimen();
        stolenCard.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, stolenCard);
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new PersistentSpecimen());
        Card libraryCard = new Island();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setLibrary(player2, List.of());

        castDiverSkaab();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.TargetLibraryDestinationChoice.class)
                .playerId()).isEqualTo(player1.getId());
        assertThatThrownBy(() -> harness.handleListChoice(player2, "Bottom"))
                .isInstanceOf(IllegalStateException.class);
        harness.handleListChoice(player1, "Bottom");

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard, stolenCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertNotOnBattlefield(player2, "Persistent Specimen");
    }

    private void castDiverSkaab() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DiverSkaab()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

}
