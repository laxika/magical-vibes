package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BaneAlleyBrokerTest extends BaseCardTest {

    @Test
    @DisplayName("First ability draws a card, then exiles a chosen card from hand face down")
    void drawsThenExilesFaceDown() {
        Permanent broker = harness.addToBattlefieldAndReturn(player1, new BaneAlleyBroker());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Ornithopter())));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        var exiled = gd.exiledCards.stream()
                .filter(e -> broker.getId().equals(e.sourcePermanentId()))
                .toList();
        assertThat(exiled).singleElement().satisfies(e -> {
            assertThat(e.card().getName()).isEqualTo("Grizzly Bears");
            assertThat(e.faceDown()).isTrue();
            assertThat(e.ownerId()).isEqualTo(player1.getId());
        });
    }

    @Test
    @DisplayName("Second ability returns a card exiled with the Broker to its owner's hand")
    void returnsExiledCardToHand() {
        Permanent broker = harness.addToBattlefieldAndReturn(player1, new BaneAlleyBroker());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Ornithopter())));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        assertThat(gd.exiledCards).anyMatch(e -> broker.getId().equals(e.sourcePermanentId()));

        broker.untap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.exiledCards).noneMatch(e -> broker.getId().equals(e.sourcePermanentId()));
    }

    @Test
    @DisplayName("Cards exiled with the Broker stay exiled when it leaves the battlefield")
    void exiledCardsStayExiledWhenBrokerLeaves() {
        Permanent broker = harness.addToBattlefieldAndReturn(player1, new BaneAlleyBroker());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLibrary(player1, new ArrayList<>(List.of(new Ornithopter())));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.setHand(player2, new ArrayList<>(List.of(new DoomBlade())));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, broker.getId());
        harness.passBothPriorities();

        assertThat(gd.exiledCards).anyMatch(e -> broker.getId().equals(e.sourcePermanentId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
