package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.cards.m.MistCloakedHerald;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForerunnerOfTheHeralds.class, MistCloakedHerald.class, RaptorCompanion.class})
class ForerunnerOfTheHeraldsTest extends BaseCardTest {

    @Test
    @DisplayName("May search for a Merfolk and put it on top of the library")
    void maySearchForMerfolkToTopOfLibrary() {
        harness.setHand(player1, List.of(new ForerunnerOfTheHeralds()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setLibrary(player1, List.of(new MistCloakedHerald(), new RaptorCompanion()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .singleElement()
                .isInstanceOf(MistCloakedHerald.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gameData.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(MistCloakedHerald.class);
        assertThat(gameData.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Another Merfolk entering puts a +1/+1 counter on this creature")
    void merfolkEnteringPutsCounterOnThisCreature() {
        Permanent forerunner = harness.addToBattlefieldAndReturn(player1, new ForerunnerOfTheHeralds());

        harness.setHand(player1, List.of(new MistCloakedHerald()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(forerunner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-Merfolk entering does not trigger the ability")
    void nonMerfolkEnteringDoesNotTrigger() {
        Permanent forerunner = harness.addToBattlefieldAndReturn(player1, new ForerunnerOfTheHeralds());

        harness.setHand(player1, List.of(new RaptorCompanion()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(forerunner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining the search leaves the library untouched and does not count itself")
    void mayDeclineSearchWithoutGettingCounter() {
        Card merfolk = new MistCloakedHerald();
        Card dinosaur = new RaptorCompanion();
        harness.setLibrary(player1, List.of(merfolk, dinosaur));
        harness.setHand(player1, List.of(new ForerunnerOfTheHeralds()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(merfolk, dinosaur);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst()
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Searching a library without Merfolk completes without moving a card")
    void searchWithoutMerfolkCompletes() {
        Card dinosaur = new RaptorCompanion();
        harness.setLibrary(player1, List.of(dinosaur));
        harness.setHand(player1, List.of(new ForerunnerOfTheHeralds()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(dinosaur);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Merfolk entering does not put a counter on this creature")
    void opponentMerfolkDoesNotTrigger() {
        Permanent forerunner = harness.addToBattlefieldAndReturn(player2, new ForerunnerOfTheHeralds());
        harness.setHand(player1, List.of(new MistCloakedHerald()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(forerunner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The search may fail to find even when a Merfolk is available")
    void mayFailToFindAvailableMerfolk() {
        Card merfolk = new MistCloakedHerald();
        harness.setLibrary(player1, List.of(merfolk));
        harness.setHand(player1, List.of(new ForerunnerOfTheHeralds()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(merfolk);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
