package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DiscoveryDispersal;
import com.github.laxika.magicalvibes.cards.r.RottedHulk;
import com.github.laxika.magicalvibes.cards.h.Hubris;
import com.github.laxika.magicalvibes.cards.r.RiseOfEagles;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PullFromTheDeep.class, Hubris.class, RiseOfEagles.class, RottedHulk.class, DiscoveryDispersal.class})
class PullFromTheDeepTest extends BaseCardTest {

    @Test
    @CardUsed({DiscoveryDispersal.class})
    void canReturnAnInstantSorcerySplitCardAlongsideAnInstant() {
        Card splitCard = new DiscoveryDispersal();
        Card instant = new Hubris();
        Card spell = new PullFromTheDeep();
        harness.setGraveyard(player1, List.of(splitCard, instant));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId(), splitCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(instant, splitCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId())).contains(spell.getId());
    }

    @Test
    void canChooseZeroTargetsWhenEligibleCardsExist() {
        Card instant = new Hubris();
        Card sorcery = new RiseOfEagles();
        Card spell = new PullFromTheDeep();
        harness.setGraveyard(player1, List.of(instant, sorcery));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(instant, sorcery);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId())).contains(spell.getId());
    }

    @Test
    void canReturnOnlyAnInstant() {
        Card instant = new Hubris();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new PullFromTheDeep()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Hubris");
        harness.assertNotInGraveyard(player1, "Hubris");
        harness.assertNotInGraveyard(player1, "Pull from the Deep");
    }

    @Test
    void canReturnOnlyASorceryAndRejectsTwoSorceries() {
        Card sorcery = new RiseOfEagles();
        Card secondSorcery = new RiseOfEagles();
        Card spell = new PullFromTheDeep();
        harness.setGraveyard(player1, List.of(sorcery, secondSorcery));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(sorcery.getId(), secondSorcery.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one sorcery card");
        harness.handleMultipleCardsChosen(player1, List.of(sorcery.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sorcery);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(secondSorcery);
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId())).contains(spell.getId());
    }

    @Test
    void returnsRemainingLegalTargetAndExilesItself() {
        Card instant = new Hubris();
        Card sorcery = new RiseOfEagles();
        Card spell = new PullFromTheDeep();
        harness.setGraveyard(player1, List.of(instant, sorcery));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId(), sorcery.getId()));
        harness.setGraveyard(player1, List.of(sorcery));
        harness.setExile(player1, List.of(instant));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sorcery);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId()))
                .contains(instant.getId(), spell.getId());
    }

    @Test
    void goesToGraveyardWhenAllChosenTargetsBecomeIllegal() {
        Card instant = new Hubris();
        Card sorcery = new RiseOfEagles();
        Card spell = new PullFromTheDeep();
        harness.setGraveyard(player1, List.of(instant, sorcery));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId(), sorcery.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(instant, sorcery));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pull from the Deep");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId())).doesNotContain(spell.getId());
    }

    @Test
    void cannotTargetCardsInOpponentsGraveyard() {
        Card ownInstant = new Hubris();
        Card opponentsSorcery = new RiseOfEagles();
        harness.setGraveyard(player1, List.of(ownInstant));
        harness.setGraveyard(player2, List.of(opponentsSorcery));
        harness.setHand(player1, List.of(new PullFromTheDeep()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownInstant.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownInstant.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Hubris");
        harness.assertInGraveyard(player2, "Rise of Eagles");
        harness.assertNotInHand(player1, "Rise of Eagles");
    }

    @Test
    @DisplayName("Returns up to one instant and up to one sorcery from the graveyard")
    void returnsUpToOneInstantAndSorcery() {
        Card instant = new Hubris();
        Card secondInstant = new Hubris();
        Card sorcery = new RiseOfEagles();
        Card creature = new RottedHulk();
        Card spell = new PullFromTheDeep();
        harness.setGraveyard(player1, List.of(instant, secondInstant, sorcery, creature));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                instant.getId(), secondInstant.getId(), sorcery.getId());

        harness.handleMultipleCardsChosen(player1, List.of(instant.getId(), sorcery.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(instant.getId(), sorcery.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(secondInstant.getId(), creature.getId());
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId()))
                .contains(spell.getId());
    }

    @Test
    @DisplayName("Does not allow two instant cards to be chosen")
    void doesNotAllowTwoInstantCards() {
        Card firstInstant = new Hubris();
        Card secondInstant = new Hubris();
        Card sorcery = new RiseOfEagles();
        Card spell = new PullFromTheDeep();
        harness.setGraveyard(player1, List.of(firstInstant, secondInstant, sorcery));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(firstInstant.getId(), secondInstant.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one instant card");

        harness.handleMultipleCardsChosen(player1, List.of(firstInstant.getId(), sorcery.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(firstInstant.getId(), sorcery.getId());
    }

    @Test
    @DisplayName("Exiles itself even when no eligible cards are in the graveyard")
    void exilesItselfWithNoEligibleCards() {
        Card creature = new RottedHulk();
        Card spell = new PullFromTheDeep();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId()))
                .contains(spell.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(creature.getId());
    }
}
