package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AragornKingOfGondor;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoromirGondorsHope.class, AragornKingOfGondor.class, SolRing.class, SwordsToPlowshares.class})
class BoromirGondorsHopeTest extends BaseCardTest {

    @Test
    @DisplayName("On entering, offers one Human or artifact from the top six")
    void entersAndOffersHumanOrArtifact() {
        Card human = creature("Top Human", CardSubtype.HUMAN);
        Card artifact = artifact("Top Artifact");
        Card otherCreature = creature("Top Bear", CardSubtype.BEAR);
        Card instant = card("Top Instant", CardType.INSTANT);
        Card land = card("Top Land", CardType.LAND);
        Card enchantment = card("Top Enchantment", CardType.ENCHANTMENT);
        setLibrary(human, artifact, otherCreature, instant, land, enchantment);

        castBoromir();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(human.getId(), artifact.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(human.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(human);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(artifact, otherCreature, instant, land, enchantment);
    }

    @Test
    @DisplayName("The attack trigger offers the same library choice and may be declined")
    void attacksAndMayDecline() {
        addCreatureReady(player1, new BoromirGondorsHope());
        Card artifact = artifact("Top Artifact");
        setLibrary(artifact, card("Top One", CardType.INSTANT), card("Top Two", CardType.LAND),
                card("Top Three", CardType.ENCHANTMENT), card("Top Four", CardType.SORCERY),
                card("Top Five", CardType.PLANESWALKER));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
    }

    @Test
    void canChooseArtifactFromShortLibrary() {
        Card human = new AragornKingOfGondor();
        Card artifact = new SolRing();
        Card instant = new SwordsToPlowshares();
        setLibrary(human, artifact, instant);

        castBoromir();
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(human, instant);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotOfferEligibleSeventhCardAndBottomsOnlyTopSix() {
        Card seventh = new SolRing();
        List<Card> topSix = java.util.stream.IntStream.range(0, 6)
                .mapToObj(i -> (Card) new SwordsToPlowshares()).toList();
        List<Card> library = new java.util.ArrayList<>(topSix);
        library.add(seventh);
        harness.setLibrary(player1, library);

        castBoromir();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(seventh);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 7))
                .containsExactlyInAnyOrderElementsOf(topSix);
    }

    @Test
    void soleEligibleCardMayBeDeclined() {
        Card human = new AragornKingOfGondor();
        setLibrary(human);

        castBoromir();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(human);
    }

    @Test
    void emptyLibraryFinishesWithoutChoice() {
        setLibrary();

        castBoromir();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void castBoromir() {
        harness.setHand(player1, List.of(new BoromirGondorsHope()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.WHITE, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLUE, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }

    private static Card creature(String name, CardSubtype subtype) {
        Card card = card(name, CardType.CREATURE);
        card.setSubtypes(List.of(subtype));
        card.setPower(1);
        card.setToughness(1);
        return card;
    }

    private static Card artifact(String name) {
        return card(name, CardType.ARTIFACT);
    }

    private static Card card(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        return card;
    }
}
