package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LilianaVess;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaestrosCharm.class, GrizzlyBears.class, Island.class, LilianaVess.class})
class MaestrosCharmTest extends BaseCardTest {

    @Test
    @DisplayName("Puts one of the top five cards into hand and the rest into the graveyard")
    void choosesOneCardForHand() {
        Card card0 = new GrizzlyBears();
        Card card1 = new GrizzlyBears();
        Card card2 = new GrizzlyBears();
        Card card3 = new GrizzlyBears();
        Card card4 = new GrizzlyBears();
        harness.setLibrary(player1, List.of(card0, card1, card2, card3, card4));
        castMaestrosCharm(0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(card2.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(card2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(card0, card1, card3, card4)
                .noneMatch(card -> card.getId().equals(card2.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each opponent loses 3 life and the controller gains 3 life")
    void drainsEachOpponent() {
        castMaestrosCharm(1);

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals 5 damage to a target creature")
    void damagesCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castMaestrosCharm(2, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 5 damage to a target planeswalker")
    void damagesPlaneswalker() {
        Permanent liliana = harness.addToBattlefieldAndReturn(player2, new LilianaVess());
        liliana.setCounterCount(CounterType.LOYALTY, 8);
        castMaestrosCharm(2, liliana.getId());

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a land with the damage mode")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Island());
        harness.setHand(player1, List.of(new MaestrosCharm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2,
                harness.getPermanentId(player2, "Island")))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castMaestrosCharm(int mode) {
        castMaestrosCharm(mode, null);
    }

    private void castMaestrosCharm(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new MaestrosCharm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, mode, targetId);
        harness.passBothPriorities();
    }
}
