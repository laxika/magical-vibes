package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HostileNegotiationsTest extends BaseCardTest {

    @Test
    void opponentChoosesBetweenOneFaceUpAndOneFaceDownPile() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card swamp = new Swamp();
        Card island = new Island();
        Card plains = new Plains();
        Card elves = new LlanowarElves();
        Card hostileNegotiations = new HostileNegotiations();
        harness.setLibrary(player1, List.of(forest, bears, swamp, island, plains, elves));
        harness.setHand(player1, List.of(hostileNegotiations));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).hasSize(6)
                .allMatch(ExiledCardEntry::faceDown);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.HostileNegotiationsFaceUpChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.HostileNegotiationsOpponentPileChoice opponentChoice =
                gd.interaction.activeInteraction(PendingInteraction.HostileNegotiationsOpponentPileChoice.class);
        assertThat(opponentChoice).isNotNull();
        assertThat(opponentChoice.pile1FaceUp()).isTrue();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(island.getId(), plains.getId(), elves.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> !card.getId().equals(hostileNegotiations.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(forest.getId(), bears.getId(), swamp.getId());
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    void anEmptyPileMayBeTurnedFaceUp() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card swamp = new Swamp();
        harness.setLibrary(player1, List.of(forest, bears, swamp));
        harness.setHand(player1, List.of(new HostileNegotiations()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(forest.getId(), bears.getId(), swamp.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(forest.getId()))
                .noneMatch(card -> card.getId().equals(bears.getId()))
                .noneMatch(card -> card.getId().equals(swamp.getId()));
        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }
}
