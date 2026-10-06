package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BedlamReveler;
import com.github.laxika.magicalvibes.cards.c.ContingencyPlan;
import com.github.laxika.magicalvibes.cards.g.GalvanicBombardment;
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

@CardUsed({ShredsOfSanity.class, GalvanicBombardment.class, ContingencyPlan.class, BedlamReveler.class})
class ShredsOfSanityTest extends BaseCardTest {

    @Test
    @DisplayName("Returns one instant and one sorcery, then discards and exiles itself")
    void returnsInstantAndSorceryThenDiscardsAndExiles() {
        Card instant = new GalvanicBombardment();
        Card sorcery = new ContingencyPlan();
        harness.setGraveyard(player1, List.of(instant, sorcery));
        harness.setHand(player1, List.of(new ShredsOfSanity(), new BedlamReveler()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(instant.getId(), sorcery.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Galvanic Bombardment");
        harness.assertInHand(player1, "Contingency Plan");
        harness.assertInGraveyard(player1, "Bedlam Reveler");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Shreds of Sanity"));
    }

    @Test
    void returnsOnlyInstantAndCanDiscardReturnedCard() {
        Card instant = new GalvanicBombardment();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new ShredsOfSanity(), new BedlamReveler()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, List.of(instant.getId()));
        harness.handleCardChosen(player1, 1);

        harness.assertInHand(player1, "Bedlam Reveler");
        harness.assertNotInHand(player1, "Galvanic Bombardment");
        harness.assertInGraveyard(player1, "Galvanic Bombardment");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Shreds of Sanity"));
    }

    @Test
    void returnsOnlySorceryAndDiscardsItWhenItIsTheOnlyCardInHand() {
        Card sorcery = new ContingencyPlan();
        harness.setGraveyard(player1, List.of(sorcery));
        harness.setHand(player1, List.of(new ShredsOfSanity()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, List.of(sorcery.getId()));
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Contingency Plan");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Shreds of Sanity"));
    }

    @Test
    void resolvesWithNoTargetsAndNoCardsToDiscard() {
        harness.setHand(player1, List.of(new ShredsOfSanity()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Shreds of Sanity");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Shreds of Sanity"));
    }

    @Test
    void stillReturnsRemainingTargetWhenOneTargetLeavesGraveyard() {
        Card instant = new GalvanicBombardment();
        Card sorcery = new ContingencyPlan();
        harness.setGraveyard(player1, List.of(instant, sorcery));
        harness.setHand(player1, List.of(new ShredsOfSanity(), new BedlamReveler()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, List.of(instant.getId(), sorcery.getId()));
        harness.setGraveyard(player1, List.of(sorcery));
        harness.setExile(player1, List.of(instant));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Contingency Plan");
        harness.assertNotInHand(player1, "Galvanic Bombardment");
        harness.assertInGraveyard(player1, "Bedlam Reveler");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Shreds of Sanity"));
    }

    @Test
    void doesNotDiscardOrExileWhenAllTargetsBecomeIllegal() {
        Card instant = new GalvanicBombardment();
        Card sorcery = new ContingencyPlan();
        harness.setGraveyard(player1, List.of(instant, sorcery));
        harness.setHand(player1, List.of(new ShredsOfSanity(), new BedlamReveler()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, List.of(instant.getId(), sorcery.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(instant, sorcery));

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInHand(player1, "Bedlam Reveler");
        harness.assertInGraveyard(player1, "Shreds of Sanity");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Shreds of Sanity"));
    }

    @Test
    void doesNotAllowTwoSorceryTargets() {
        Card first = new ContingencyPlan();
        Card second = new ContingencyPlan();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new ShredsOfSanity()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("more than one sorcery");
    }

    @Test
    void doesNotAllowOpponentsGraveyardOrCreatureTargets() {
        Card opposingInstant = new GalvanicBombardment();
        Card creature = new BedlamReveler();
        harness.setGraveyard(player2, List.of(opposingInstant));
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new ShredsOfSanity()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(opposingInstant.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not allow two instant targets")
    void doesNotAllowTwoInstantTargets() {
        Card firstInstant = new GalvanicBombardment();
        Card secondInstant = new GalvanicBombardment();
        harness.setGraveyard(player1, List.of(firstInstant, secondInstant));
        harness.setHand(player1, List.of(new ShredsOfSanity()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(firstInstant.getId(), secondInstant.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("more than one instant");
    }

    @Test
    @DisplayName("Still discards and exiles when no graveyard target is chosen")
    void resolvesWithoutTargets() {
        harness.setGraveyard(player1, List.of(new BedlamReveler()));
        harness.setHand(player1, List.of(new ShredsOfSanity(), new ContingencyPlan()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Contingency Plan");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Shreds of Sanity"));
    }
}
