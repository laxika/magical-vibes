package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HolographicDouble.class, GrizzlyBears.class, Shock.class})
class HolographicDoubleTest extends BaseCardTest {

    @Test
    void exilesItselfAndConjuresASelectedCreatureDuplicateIntoHand() {
        Card holographicDouble = new HolographicDouble();
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        harness.setHand(player1, List.of(holographicDouble, bears, shock));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(holographicDouble);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        PendingInteraction.RevealedHandChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.validIndices()).containsExactly(0);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Shock", "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .doesNotHaveDuplicates();
    }

    @Test
    void doesNotOfferNoncreatureCards() {
        Card holographicDouble = new HolographicDouble();
        Card shock = new Shock();
        harness.setHand(player1, List.of(holographicDouble, shock));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(shock);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(holographicDouble);
    }

    @Test
    void canActivateAsTheLastCardInHandWithoutConjuringItself() {
        Card source = new HolographicDouble();
        harness.setHand(player1, List.of(source));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(source);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPayTheBlueCostWithRedMana() {
        Card source = new HolographicDouble();
        harness.setHand(player1, List.of(source));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(source);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void conjuredDuplicateRetainsItsHandAbility() {
        Card source = new HolographicDouble();
        Card original = new HolographicDouble();
        harness.setHand(player1, List.of(source, original));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2).contains(original);
        Card duplicate = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> !card.getId().equals(original.getId()))
                .findFirst().orElseThrow();
        int duplicateIndex = gd.playerHands.get(player1.getId()).indexOf(duplicate);

        harness.activateHandAbility(player1, duplicateIndex, null);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(source, duplicate);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(original);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2).contains(original);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .doesNotHaveDuplicates();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
