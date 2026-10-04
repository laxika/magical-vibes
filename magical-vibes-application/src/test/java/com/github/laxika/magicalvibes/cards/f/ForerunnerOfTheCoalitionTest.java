package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.s.SailorOfMeans;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForerunnerOfTheCoalition.class, SailorOfMeans.class, ColossalDreadmaw.class})
class ForerunnerOfTheCoalitionTest extends BaseCardTest {

    @Test
    @DisplayName("May search for a Pirate and put it on top of the library")
    void maySearchForPirateToTopOfLibrary() {
        harness.setHand(player1, List.of(new ForerunnerOfTheCoalition()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.setLibrary(player1, List.of(new SailorOfMeans(), new ColossalDreadmaw()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .singleElement()
                .isInstanceOf(SailorOfMeans.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gameData.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(SailorOfMeans.class);
        assertThat(gameData.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("reveals Sailor of Means")).isTrue();
    }

    @Test
    @DisplayName("Another Pirate entering makes each opponent lose 1 life")
    void pirateEnteringMakesEachOpponentLoseLife() {
        harness.addToBattlefield(player1, new ForerunnerOfTheCoalition());
        harness.setHand(player1, List.of(new SailorOfMeans()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 1);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A non-Pirate creature entering does not trigger life loss")
    void nonPirateEnteringDoesNotMakeOpponentLoseLife() {
        harness.addToBattlefield(player1, new ForerunnerOfTheCoalition());
        harness.setHand(player1, List.of(new ColossalDreadmaw()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void decliningSearchLeavesLibraryUnchangedAndDoesNotTriggerSelf() {
        SailorOfMeans pirate = new SailorOfMeans();
        ColossalDreadmaw dinosaur = new ColossalDreadmaw();
        harness.setLibrary(player1, List.of(pirate, dinosaur));
        harness.setHand(player1, List.of(new ForerunnerOfTheCoalition()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(pirate, dinosaur);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayFailToFindEvenWhenPirateIsAvailable() {
        SailorOfMeans pirate = new SailorOfMeans();
        harness.setLibrary(player1, List.of(pirate, new ColossalDreadmaw()));
        harness.setHand(player1, List.of(new ForerunnerOfTheCoalition()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2).contains(pirate);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void acceptedSearchWithNoPiratesCompletesWithoutMovingCards() {
        ColossalDreadmaw dinosaur = new ColossalDreadmaw();
        harness.setLibrary(player1, List.of(dinosaur));
        harness.setHand(player1, List.of(new ForerunnerOfTheCoalition()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(dinosaur);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsPirateDoesNotTriggerLifeLoss() {
        harness.addToBattlefield(player2, new ForerunnerOfTheCoalition());
        harness.setHand(player1, List.of(new SailorOfMeans()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void acceptedSearchWithEmptyLibraryCompletes() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ForerunnerOfTheCoalition()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

}
