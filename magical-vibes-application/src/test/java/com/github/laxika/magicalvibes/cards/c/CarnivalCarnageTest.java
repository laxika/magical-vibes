package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CarnivalCarnageTest extends BaseCardTest {

    private static final int CARNIVAL = 0;
    private static final int CARNAGE = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Carnival damages a creature and its controller")
    void carnivalDamagesCreatureAndController() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CarnivalCarnage()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castModalInstant(player1, 0, CARNIVAL, List.of(bears.getId(), bears.getId()));
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Carnival can target a planeswalker")
    void carnivalDamagesPlaneswalker() {
        Permanent planeswalker = addPlaneswalker(player2, 4);

        harness.setHand(player1, List.of(new CarnivalCarnage()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castModalInstant(player1, 0, CARNIVAL,
                List.of(planeswalker.getId(), planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Carnage damages the opponent and makes them discard two cards")
    void carnageDamagesAndDiscards() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek(), new Forest())));
        harness.setHand(player1, List.of(new CarnivalCarnage()));
        addCarnageMana();

        harness.castModalInstant(player1, 0, CARNAGE, List.of(player2.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Fuse resolves Carnival before Carnage")
    void fuseResolvesBothHalves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek(), new Forest())));
        harness.setHand(player1, List.of(new CarnivalCarnage()));
        addFuseMana();

        harness.castModalInstant(player1, 0, FUSE,
                List.of(bears.getId(), bears.getId(), player2.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 16);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("Carnival and Carnage reject the wrong target types")
    void modesRejectIllegalTargets() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CarnivalCarnage()));
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.castModalInstant(
                player1, 0, CARNIVAL, List.of(player2.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new CarnivalCarnage()));
        addCarnageMana();
        assertThatThrownBy(() -> harness.castModalInstant(
                player1, 0, CARNAGE, List.of(bears.getId(), bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addCarnageMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void addFuseMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private Permanent addPlaneswalker(com.github.laxika.magicalvibes.model.Player player, int loyalty) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        Permanent planeswalker = new Permanent(card);
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        gd.playerBattlefields.get(player.getId()).add(planeswalker);
        return planeswalker;
    }
}
