package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.cards.i.Ichthyomorphosis;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SionaCaptainOfThePyleas.class, SentinelsEyes.class, NyxbornCourser.class,
        Ichthyomorphosis.class})
class SionaCaptainOfThePyleasTest extends BaseCardTest {

    @Test
    @DisplayName("Siona may put an Aura from the top seven cards into its controller's hand")
    void entersAndOffersAuraFromTopSeven() {
        Card aura = new SentinelsEyes();
        Card creature1 = new NyxbornCourser();
        Card creature2 = new NyxbornCourser();
        Card creature3 = new NyxbornCourser();
        Card creature4 = new NyxbornCourser();
        Card creature5 = new NyxbornCourser();
        Card creature6 = new NyxbornCourser();
        harness.setLibrary(player1, List.of(aura, creature1, creature2, creature3, creature4, creature5, creature6));
        harness.castFromHand(player1, new SionaCaptainOfThePyleas(), "{1}{G}{W}");
        resolveAllTriggers();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactly(
                aura, creature1, creature2, creature3, creature4, creature5, creature6);
        assertThat(choice.validCardIds()).containsExactly(aura.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(aura);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(creature1, creature2, creature3, creature4, creature5, creature6);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An Aura you control attaching to a creature you control creates a Human Soldier")
    void alliedAuraAttachmentCreatesSoldier() {
        Permanent siona = addCreatureReady(player1, new SionaCaptainOfThePyleas());

        enchantWithSentinelsEyes(player1, siona);

        List<Permanent> tokens = findPermanents(player1, "Human Soldier");
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.SOLDIER);
    }

    @Test
    @DisplayName("An opponent's Aura attaching to Siona does not create a token")
    void opponentsAuraDoesNotCreateSoldier() {
        Permanent siona = addCreatureReady(player1, new SionaCaptainOfThePyleas());

        enchantWithSentinelsEyes(player2, siona);

        assertThat(findPermanents(player1, "Human Soldier")).isEmpty();
        assertThat(findPermanents(player2, "Human Soldier")).isEmpty();
    }

    @Test
    @DisplayName("An Aura you control attaching to an opponent's creature does not create a token")
    void auraOnOpponentsCreatureDoesNotCreateSoldier() {
        addCreatureReady(player1, new SionaCaptainOfThePyleas());
        Permanent bears = addCreatureReady(player2, new NyxbornCourser());

        enchantWithSentinelsEyes(player1, bears);

        assertThat(findPermanents(player1, "Human Soldier")).isEmpty();
    }

    @Test
    void mayDeclineAuraFromShortLibrary() {
        Card aura = new SentinelsEyes();
        Card creature = new NyxbornCourser();
        harness.setLibrary(player1, List.of(aura, creature));

        harness.castFromHand(player1, new SionaCaptainOfThePyleas(), "{1}{G}{W}");
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(aura, creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayChooseAuraFromShortLibrary() {
        Card aura = new SentinelsEyes();
        harness.setLibrary(player1, List.of(aura));

        harness.castFromHand(player1, new SionaCaptainOfThePyleas(), "{1}{G}{W}");
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(aura);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void noAuraInTopSevenLeavesEighthCardOnTop() {
        List<Card> topSeven = List.of(new NyxbornCourser(), new NyxbornCourser(),
                new NyxbornCourser(), new NyxbornCourser(), new NyxbornCourser(),
                new NyxbornCourser(), new NyxbornCourser());
        Card eighthCard = new SentinelsEyes();
        java.util.ArrayList<Card> library = new java.util.ArrayList<>(topSeven);
        library.add(eighthCard);
        harness.setLibrary(player1, library);

        harness.castFromHand(player1, new SionaCaptainOfThePyleas(), "{1}{G}{W}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(eighthCard);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 8))
                .containsExactlyInAnyOrderElementsOf(topSeven);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryDoesNotRequireChoice() {
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new SionaCaptainOfThePyleas(), "{1}{G}{W}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void eachAuraAttachedToAnotherAlliedCreatureCreatesSoldier() {
        addCreatureReady(player1, new SionaCaptainOfThePyleas());
        Permanent creature = addCreatureReady(player1, new NyxbornCourser());

        enchantWithSentinelsEyes(player1, creature);
        assertThat(findPermanents(player1, "Human Soldier")).hasSize(1);

        enchantWithSentinelsEyes(player1, creature);
        assertThat(findPermanents(player1, "Human Soldier")).hasSize(2);
    }

    @Test
    void sionaWithNoAbilitiesDoesNotCreateSoldier() {
        Permanent siona = addCreatureReady(player1, new SionaCaptainOfThePyleas());
        Permanent creature = addCreatureReady(player1, new NyxbornCourser());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Ichthyomorphosis()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castEnchantment(player2, 0, siona.getId());
        resolveAllTriggers();
        harness.assertOnBattlefield(player2, "Ichthyomorphosis");

        enchantWithSentinelsEyes(player1, creature);

        assertThat(findPermanents(player1, "Human Soldier")).isEmpty();
    }

    private void enchantWithSentinelsEyes(Player controller, Permanent target) {
        harness.forceActivePlayer(controller);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(controller, List.of(new SentinelsEyes()));
        harness.addMana(controller, ManaColor.WHITE, 1);

        harness.castEnchantment(controller, 0, target.getId());
        resolveAllTriggers();
    }
}
