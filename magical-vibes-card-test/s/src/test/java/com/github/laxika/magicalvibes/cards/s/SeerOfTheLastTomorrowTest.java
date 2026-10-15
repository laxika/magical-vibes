package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeerOfTheLastTomorrow.class, GrizzlyBears.class})
class SeerOfTheLastTomorrowTest extends BaseCardTest {

    @Test
    @DisplayName("Target player mills three cards; the Seer is tapped and a card is discarded")
    void millsThreeCards() {
        Permanent seer = addCreatureReady(player1, new SeerOfTheLastTomorrow());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 6) {
            deck.removeFirst();
        }
        int deckSizeBefore = deck.size();
        List<Card> topThree = List.of(deck.get(0), deck.get(1), deck.get(2));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(seer.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 3);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(topThree);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can target yourself with the mill ability")
    void canTargetSelf() {
        addCreatureReady(player1, new SeerOfTheLastTomorrow());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        List<Card> deck = gd.playerDecks.get(player1.getId());
        while (deck.size() > 6) {
            deck.removeFirst();
        }
        int deckSizeBefore = deck.size();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
    }

    @Test
    @DisplayName("Cannot activate with no card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addCreatureReady(player1, new SeerOfTheLastTomorrow());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, new ArrayList<>());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate ability with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent seer = harness.addToBattlefieldAndReturn(player1, new SeerOfTheLastTomorrow());
        seer.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("Mills all remaining cards when the library has fewer than three")
    void millsShortLibrary() {
        addCreatureReady(player1, new SeerOfTheLastTomorrow());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new SeerOfTheLastTomorrow()));
        List<Card> library = List.of(new SeerOfTheLastTomorrow(), new SeerOfTheLastTomorrow());
        harness.setLibrary(player2, library);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(library);
    }

    @Test
    @DisplayName("Can activate targeting an empty library")
    void canMillEmptyLibrary() {
        Permanent seer = addCreatureReady(player1, new SeerOfTheLastTomorrow());
        harness.addMana(player1, ManaColor.BLUE, 1);
        Card discarded = new SeerOfTheLastTomorrow();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(seer.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
    }

    @Test
    @DisplayName("Tap and discard are paid before the mill ability resolves")
    void paysCostsBeforeResolution() {
        Permanent seer = addCreatureReady(player1, new SeerOfTheLastTomorrow());
        harness.addMana(player1, ManaColor.BLUE, 1);
        Card discarded = new SeerOfTheLastTomorrow();
        harness.setHand(player1, List.of(discarded));
        List<Card> library = List.of(new SeerOfTheLastTomorrow(), new SeerOfTheLastTomorrow(),
                new SeerOfTheLastTomorrow());
        harness.setLibrary(player2, library);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(seer.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(library);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(library);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent seer = addCreatureReady(player1, new SeerOfTheLastTomorrow());
        seer.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        Card discard = new SeerOfTheLastTomorrow();
        harness.setHand(player1, List.of(discard));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot pay the activation with mana of the wrong color")
    void cannotActivateWithoutBlueMana() {
        Permanent seer = addCreatureReady(player1, new SeerOfTheLastTomorrow());
        harness.addMana(player1, ManaColor.GREEN, 1);
        Card discard = new SeerOfTheLastTomorrow();
        harness.setHand(player1, List.of(discard));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(seer.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discard);
        assertThat(gd.stack).isEmpty();
    }
}
