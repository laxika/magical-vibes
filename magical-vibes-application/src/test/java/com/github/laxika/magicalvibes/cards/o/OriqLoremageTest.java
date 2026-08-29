package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OriqLoremageTest extends BaseCardTest {

    @Test
    @DisplayName("Searching an instant puts it into the graveyard and adds a +1/+1 counter")
    void instantGetsCounter() {
        Permanent loremage = setUpLoremage(List.of(new Fog(), new GrizzlyBears()));

        activateAndResolve();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(loremage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Fog");
    }

    @Test
    @DisplayName("Searching a non-instant or non-sorcery card does not add a counter")
    void nonSpellDoesNotGetCounter() {
        Permanent loremage = setUpLoremage(List.of(new GrizzlyBears(), new Fog()));

        activateAndResolve();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(loremage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The conditional counter checks the searched card, not another spell already in the graveyard")
    void existingSpellInGraveyardDoesNotGrantCounter() {
        Permanent loremage = setUpLoremage(List.of(new GrizzlyBears()));
        harness.setGraveyard(player1, List.of(new Fog()));

        activateAndResolve();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(loremage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent setUpLoremage(List<Card> library) {
        harness.addToBattlefield(player1, new OriqLoremage());
        Permanent loremage = findPermanent(player1, "Oriq Loremage");
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
