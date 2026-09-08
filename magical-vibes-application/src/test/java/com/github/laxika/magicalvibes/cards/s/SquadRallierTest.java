package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SquadRallierTest extends BaseCardTest {

    @Test
    @DisplayName("Offers only creature cards with power 2 or less from the top four")
    void offersSmallCreatureCards() {
        addCreatureReady(player1, new SquadRallier());
        Card smallCreature = new LlanowarElves();
        Card powerTwoCreature = new GrizzlyBears();
        setupTopFour(List.of(smallCreature, new HillGiant(), new Shock(), powerTwoCreature));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class).validCardIds())
                .containsExactlyInAnyOrder(smallCreature.getId(), powerTwoCreature.getId());
    }

    @Test
    @DisplayName("Putting an eligible creature into hand sends the rest to the bottom")
    void chosenCreatureGoesToHand() {
        addCreatureReady(player1, new SquadRallier());
        Card smallCreature = new LlanowarElves();
        setupTopFour(List.of(smallCreature, new HillGiant(), new Shock(), new GrizzlyBears()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.handleMultipleCardsChosen(player1, List.of(smallCreature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(smallCreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("With no eligible creature, all four cards go to the bottom")
    void noEligibleCreatureGoesToBottom() {
        addCreatureReady(player1, new SquadRallier());
        setupTopFour(List.of(new HillGiant(), new Shock(), new Plains(), new Shock()));
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    private void setupTopFour(List<Card> cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
