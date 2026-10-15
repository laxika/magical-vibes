package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.ElderDeepFiend;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FoulEmissary.class, ElderDeepFiend.class, GrizzlyBears.class, Plains.class, Fling.class})
class FoulEmissaryTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers a creature from the top four cards")
    void etbOffersCreatureFromTopFour() {
        Card creature = new GrizzlyBears();
        harness.setHand(player1, List.of(new FoulEmissary()));
        harness.setLibrary(player1, List.of(creature, new Plains(), new Plains(), new Plains()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(creature);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
    }

    @Test
    @DisplayName("Creates an Eldrazi Horror when sacrificed to cast an emerge spell")
    void createsTokenWhenSacrificedToCastEmergeSpell() {
        Permanent foulEmissary = addCreatureReady(player1, new FoulEmissary());
        harness.setHand(player1, List.of(new ElderDeepFiend()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(foulEmissary.getId()));
        assertThat(findPermanents(player1, "Foul Emissary")).isEmpty();

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Eldrazi Horror");
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    void choosingOneCreatureOrdersOnlyTheOtherTopFourCardsOnBottom() {
        Card first = new FoulEmissary();
        Card second = new FoulEmissary();
        Card third = new FoulEmissary();
        Card fourth = new FoulEmissary();
        Card untouched = new FoulEmissary();
        harness.setHand(player1, List.of(new FoulEmissary()));
        harness.setLibrary(player1, List.of(first, second, third, fourth, untouched));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(first, second, third, fourth);
        harness.handleCardChosen(player1, 1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactly(first, third, fourth);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, fourth, first, third);
    }

    @Test
    void mayDeclineCreatureEvenWithAShortLibrary() {
        Card first = new FoulEmissary();
        Card second = new FoulEmissary();
        harness.setHand(player1, List.of(new FoulEmissary()));
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
    }

    @Test
    void noCreaturesStillAllowsOrderingAllLookedAtCards() {
        Card first = new Plains();
        Card second = new Plains();
        harness.setHand(player1, List.of(new FoulEmissary()));
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
    }

    @Test
    void emptyLibraryDoesNotRequireAChoice() {
        harness.setHand(player1, List.of(new FoulEmissary()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Foul Emissary");
    }

    @Test
    @DisplayName("Does not create a token when sacrificed to cast a non-emerge spell")
    void doesNotCreateTokenForNonEmergeSpell() {
        Permanent foulEmissary = addCreatureReady(player1, new FoulEmissary());
        harness.setHand(player1, List.of(new Fling()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstantWithSacrifice(player1, 0, player2.getId(), foulEmissary.getId());
        assertThat(findPermanents(player1, "Foul Emissary")).isEmpty();
        assertThat(findPermanents(player1, "Eldrazi Horror")).isEmpty();
    }
}
