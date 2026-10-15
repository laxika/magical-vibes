package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HuntForSpecimens;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OriqLoremage.class, Fog.class, GrizzlyBears.class, HuntForSpecimens.class})
class OriqLoremageTest extends BaseCardTest {

    @Test
    @DisplayName("Searching an instant puts it into the graveyard and adds a +1/+1 counter")
    void instantGetsCounter() {
        Permanent loremage = setUpLoremage(List.of(new Fog(), new GrizzlyBears()));

        activateAndResolve();
        harness.handleCardChosen(player1, 0);

        assertThat(loremage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Fog");
    }

    @Test
    @DisplayName("Searching a non-instant or non-sorcery card does not add a counter")
    void nonSpellDoesNotGetCounter() {
        Permanent loremage = setUpLoremage(List.of(new GrizzlyBears(), new Fog()));

        activateAndResolve();
        harness.handleCardChosen(player1, 0);

        assertThat(loremage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The conditional counter checks the searched card, not another spell already in the graveyard")
    void existingSpellInGraveyardDoesNotGrantCounter() {
        Permanent loremage = setUpLoremage(List.of(new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(new Fog()));

        activateAndResolve();
        harness.handleCardChosen(player1, 0);

        assertThat(loremage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Searching a sorcery puts it into the graveyard and adds one counter")
    void sorceryGetsCounter() {
        Permanent loremage = setUpLoremage(List.of(new HuntForSpecimens()));

        activateAndResolve();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Hunt for Specimens");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(loremage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(loremage.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty library resolves without a choice or a counter")
    void emptyLibraryDoesNotGetCounter() {
        Permanent loremage = setUpLoremage(List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(loremage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(loremage.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An unrestricted search cannot decline to find a card in a nonempty library")
    void nonemptyLibraryRequiresAChoice() {
        Permanent loremage = setUpLoremage(List.of(new HuntForSpecimens()));

        activateAndResolve();
        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player1, "Hunt for Specimens");
        assertThat(loremage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void summoningSicknessPreventsActivation() {
        Permanent loremage = setUpLoremage(List.of(new HuntForSpecimens()));
        loremage.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(loremage.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent setUpLoremage(List<Card> library) {
        Permanent loremage = harness.addToBattlefieldAndReturn(player1, new OriqLoremage());
        loremage.setSummoningSick(false);
        harness.setLibrary(player1, library);
        return loremage;
    }

    private void activateAndResolve() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
    }
}
