package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarVisionary;
import com.github.laxika.magicalvibes.cards.y.YisanTheWandererBard;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InstrumentOfTheBards.class, YisanTheWandererBard.class, LlanowarVisionary.class,
        GrizzlyBears.class})
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
        Card higherManaValue = new YisanTheWandererBard();
        Card matchingCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(higherManaValue, matchingCreature));

        activate(instrument);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(matchingCreature);
        assertThat(search.params().reveals()).isTrue();

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(matchingCreature);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void legendaryCreatureSearchCreatesTreasure() {
        Permanent instrument = addInstrument(3);
        Card legendaryCreature = new YisanTheWandererBard();
        harness.setLibrary(player1, List.of(legendaryCreature));

        activate(instrument);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactlyInAnyOrder(legendaryCreature);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(legendaryCreature);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void nonlegendaryCreatureSearchDoesNotCreateTreasure() {
        Permanent instrument = addInstrument(3);
        Card nonlegendaryCreature = new LlanowarVisionary();
        harness.setLibrary(player1, List.of(nonlegendaryCreature));

        activate(instrument);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).contains(nonlegendaryCreature);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
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
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
