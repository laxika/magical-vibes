package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IntimidationTacticsTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals an opponent's hand and allows choosing an artifact or creature to exile")
    void choosesArtifactOrCreatureToExile() {
        Card creature = new GrizzlyBears();
        Card artifact = new AccordersShield();
        Card instant = new Peek();
        harness.setHand(player2, new ArrayList<>(List.of(creature, artifact, instant)));

        harness.setHand(player1, List.of(new IntimidationTactics()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0, 1);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Accorder's Shield"));
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears", "Peek");
    }

    @Test
    @DisplayName("Does nothing when the opponent has no artifact or creature card")
    void noValidCardDoesNothing() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new Peek())));
        harness.setHand(player1, List.of(new IntimidationTactics()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target the caster")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new IntimidationTactics()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards Intimidation Tactics and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new IntimidationTactics()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Intimidation Tactics");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
