package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Doomgape.class, GrizzlyBears.class})
class DoomgapeTest extends BaseCardTest {

    @Test
    @DisplayName("As the only creature, Doomgape sacrifices itself and controller gains life equal to its toughness")
    void sacrificesItselfWhenOnlyCreature() {
        harness.addToBattlefield(player1, new Doomgape());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve the upkeep trigger

        harness.assertNotOnBattlefield(player1, "Doomgape");
        harness.assertInGraveyard(player1, "Doomgape");
        // Doomgape is 10/10
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 10);
    }

    @Test
    @DisplayName("With multiple creatures, controller is prompted to choose which to sacrifice")
    void promptsChoiceWithMultipleCreatures() {
        harness.addToBattlefield(player1, new Doomgape());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve the upkeep trigger -> prompts choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.SacrificeCreatureControllerGainsLifeEqualToToughness.class);
    }

    @Test
    @DisplayName("Choosing Grizzly Bears sacrifices it and controller gains 2 life (toughness 2)")
    void choosingBearsGainsTwoLife() {
        harness.addToBattlefield(player1, new Doomgape());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Doomgape");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new Doomgape());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Doomgape");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Doomgape may be chosen for sacrifice even when another creature is available")
    void mayChooseDoomgapeWithOtherCreatureAvailable() {
        Permanent doomgape = harness.addToBattlefieldAndReturn(player1, new Doomgape());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, doomgape.getId());

        harness.assertInGraveyard(player1, "Doomgape");
        harness.assertNotOnBattlefield(player1, "Doomgape");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 30);
    }

    @Test
    @DisplayName("Life gain uses the sacrificed creature's toughness including counters")
    void usesModifiedToughnessOfChosenCreature() {
        harness.addToBattlefield(player1, new Doomgape());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Doomgape");
        harness.assertLife(player1, 25);
    }

    @Test
    @DisplayName("Automatic sacrifice uses Doomgape's toughness immediately before it leaves")
    void automaticSacrificeUsesModifiedToughness() {
        Permanent doomgape = harness.addToBattlefieldAndReturn(player1, new Doomgape());
        doomgape.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 3);
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Doomgape");
        harness.assertNotOnBattlefield(player1, "Doomgape");
        harness.assertLife(player1, 27);
    }

    @Test
    @DisplayName("An opponent's creature cannot be sacrificed to satisfy Doomgape's upkeep")
    void ignoresOpponentsCreatures() {
        harness.addToBattlefield(player1, new Doomgape());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Doomgape");
        harness.assertNotOnBattlefield(player1, "Doomgape");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 30);
        harness.assertLife(player2, 20);
    }
}
