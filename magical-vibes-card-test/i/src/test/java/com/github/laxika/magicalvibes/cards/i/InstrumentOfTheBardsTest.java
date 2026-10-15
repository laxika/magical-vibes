package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CircleOfTheMoonDruid;
import com.github.laxika.magicalvibes.cards.g.GnollHunter;
import com.github.laxika.magicalvibes.cards.v.VarisSilverymoonRanger;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InstrumentOfTheBards.class, VarisSilverymoonRanger.class, CircleOfTheMoonDruid.class,
        GnollHunter.class})
class InstrumentOfTheBardsTest extends BaseCardTest {

    @Test
    void upkeepMayAddHarmonyCounter() {
        Permanent instrument = addInstrument(0);

        runUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(instrument.getCounterCount(CounterType.HARMONY)).isEqualTo(1);
    }

    @Test
    void searchesForCreatureWithExactHarmonyManaValue() {
        Permanent instrument = addInstrument(2);
        Card higherManaValue = new VarisSilverymoonRanger();
        Card matchingCreature = new GnollHunter();
        harness.setLibrary(player1, List.of(higherManaValue, matchingCreature));

        activate(instrument);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(matchingCreature);
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(matchingCreature);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void legendaryCreatureSearchCreatesTreasure() {
        Permanent instrument = addInstrument(3);
        Card legendaryCreature = new VarisSilverymoonRanger();
        harness.setLibrary(player1, List.of(legendaryCreature));

        activate(instrument);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactlyInAnyOrder(legendaryCreature);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(legendaryCreature);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void nonlegendaryCreatureSearchDoesNotCreateTreasure() {
        Permanent instrument = addInstrument(3);
        Card nonlegendaryCreature = new CircleOfTheMoonDruid();
        harness.setLibrary(player1, List.of(nonlegendaryCreature));

        activate(instrument);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(nonlegendaryCreature);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void upkeepCounterCanBeDeclined() {
        Permanent instrument = addInstrument(2);

        runUpkeep(player1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(instrument.getCounterCount(CounterType.HARMONY)).isEqualTo(2);
    }

    @Test
    void opponentUpkeepDoesNotAddCounter() {
        Permanent instrument = addInstrument(2);

        runUpkeep(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(instrument.getCounterCount(CounterType.HARMONY)).isEqualTo(2);
    }

    @Test
    void mayFailToFindEvenWhenLegendaryCreatureMatches() {
        Permanent instrument = addInstrument(3);
        Card creature = new VarisSilverymoonRanger();
        harness.setLibrary(player1, List.of(creature));

        activate(instrument);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void zeroCountersDoesNotFindPositiveManaValueCreature() {
        Permanent instrument = addInstrument(0);
        Card creature = new GnollHunter();
        harness.setLibrary(player1, List.of(creature));

        activate(instrument);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void matchingManaValueNoncreatureCannotBeFound() {
        Permanent instrument = addInstrument(1);
        Card artifact = new InstrumentOfTheBards();
        harness.setLibrary(player1, List.of(artifact));

        activate(instrument);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(artifact);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void searchUsesHarmonyCountersAtResolution() {
        Permanent instrument = addInstrument(2);
        Card oldMatch = new GnollHunter();
        Card newMatch = new VarisSilverymoonRanger();
        harness.setLibrary(player1, List.of(oldMatch, newMatch));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(instrument.isTapped()).isTrue();
        instrument.setCounterCount(CounterType.HARMONY, 3);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(newMatch);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(newMatch).doesNotContain(oldMatch);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    private Permanent addInstrument(int harmonyCounters) {
        Permanent instrument = harness.addToBattlefieldAndReturn(player1, new InstrumentOfTheBards());
        instrument.setCounterCount(CounterType.HARMONY, harmonyCounters);
        return instrument;
    }

    private void activate(Permanent instrument) {
        harness.addMana(player1, ManaColor.GREEN, 4);
        int permanentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(instrument);
        harness.activateAbility(player1, permanentIndex, 0, null, null);
        harness.passBothPriorities();
    }

    private void runUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.UPKEEP);
        harness.passBothPriorities();
    }
}
