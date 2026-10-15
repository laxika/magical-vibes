package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantKiller;
import com.github.laxika.magicalvibes.cards.g.GoldenEgg;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.o.Outmuscle;
import com.github.laxika.magicalvibes.cards.t.TrappedInTheTower;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArcanistsOwl.class, Forest.class, GiantKiller.class, GoldenEgg.class,
        Opt.class, Outmuscle.class, TrappedInTheTower.class})
class ArcanistsOwlTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers artifact and enchantment cards among the top four")
    void offersArtifactsAndEnchantments() {
        Card artifact = card("Artifact", CardType.ARTIFACT);
        Card enchantment = card("Enchantment", CardType.ENCHANTMENT);
        Card creature = card("Creature", CardType.CREATURE);
        Card instant = card("Instant", CardType.INSTANT);
        setUpAndResolve(List.of(artifact, enchantment, creature, instant));

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(artifact.getId(), enchantment.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing a card puts it into hand and the rest on the bottom")
    void choosingCardPutsItIntoHand() {
        Card artifact = card("Artifact", CardType.ARTIFACT);
        Card enchantment = card("Enchantment", CardType.ENCHANTMENT);
        Card creature = card("Creature", CardType.CREATURE);
        Card instant = card("Instant", CardType.INSTANT);
        setUpAndResolve(List.of(artifact, enchantment, creature, instant));

        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(enchantment);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                artifact, creature, instant);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no artifact or enchantment, the top four go straight to the bottom")
    void noMatchingCardNeedsNoChoice() {
        Card creature = card("Creature", CardType.CREATURE);
        Card instant = card("Instant", CardType.INSTANT);
        Card sorcery = card("Sorcery", CardType.SORCERY);
        Card land = card("Land", CardType.LAND);
        setUpAndResolve(List.of(creature, instant, sorcery, land));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                creature, instant, sorcery, land);
    }

    @Test
    @DisplayName("An artifact can be selected and only the top four cards move to the bottom")
    void selectingArtifactPreservesUnseenCards() {
        Card artifact = new GoldenEgg();
        Card enchantment = new TrappedInTheTower();
        Card creature = new GiantKiller();
        Card instant = new Opt();
        Card unseenFirst = new Forest();
        Card unseenSecond = new GoldenEgg();
        setUpAndResolve(List.of(artifact, enchantment, creature, instant, unseenFirst, unseenSecond));

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(artifact.getId(), enchantment.getId());
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerDecks.get(player1.getId()).subList(0, 2))
                .containsExactly(unseenFirst, unseenSecond);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 5))
                .containsExactlyInAnyOrder(enchantment, creature, instant);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The controller may decline even when eligible cards are available")
    void decliningPutsAllLookedAtCardsBelowUnseenCards() {
        Card artifact = new GoldenEgg();
        Card enchantment = new TrappedInTheTower();
        Card creature = new GiantKiller();
        Card instant = new Opt();
        Card unseen = new Forest();
        setUpAndResolve(List.of(artifact, enchantment, creature, instant, unseen));

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(unseen);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 5))
                .containsExactlyInAnyOrder(artifact, enchantment, creature, instant);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no eligible card, the top four are put below the untouched library")
    void noEligibleCardsPreservesUnseenCard() {
        Card creature = new GiantKiller();
        Card instant = new Opt();
        Card sorcery = new Outmuscle();
        Card land = new Forest();
        Card unseen = new GoldenEgg();
        setUpAndResolve(List.of(creature, instant, sorcery, land, unseen));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(unseen);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 5))
                .containsExactlyInAnyOrder(creature, instant, sorcery, land);
    }

    @Test
    @DisplayName("A library with fewer than four cards still allows selecting an enchantment")
    void shortLibraryAllowsSelection() {
        Card enchantment = new TrappedInTheTower();
        Card creature = new GiantKiller();
        setUpAndResolve(List.of(enchantment, creature));

        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(enchantment);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library needs no choice and does not draw a card")
    void emptyLibraryCompletesWithoutChoice() {
        setUpAndResolve(List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only one eligible card can be chosen and ineligible cards cannot be chosen")
    void rejectsInvalidSelectionsWithoutLosingChoice() {
        Card artifact = new GoldenEgg();
        Card enchantment = new TrappedInTheTower();
        Card creature = new GiantKiller();
        setUpAndResolve(List.of(artifact, enchantment, creature));

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(artifact.getId(), enchantment.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(enchantment, creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setUpAndResolve(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new ArcanistsOwl()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private static Card card(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        return card;
    }
}
