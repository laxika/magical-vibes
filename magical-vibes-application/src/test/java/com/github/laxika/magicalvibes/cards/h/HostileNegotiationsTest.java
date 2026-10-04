package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HostileNegotiations.class, Forest.class, GrizzlyBears.class, Swamp.class,
        Island.class, Plains.class, LlanowarElves.class, LeylineOfTheVoid.class})
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
        harness.castFromHand(player1, hostileNegotiations, "{3}{B}");
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
        harness.castFromHand(player1, new HostileNegotiations(), "{3}{B}");
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

    @ParameterizedTest
    @ValueSource(ints = {0, 2, 4, 7})
    void opponentCanChooseFirstPileWithAnyLibrarySize(int librarySize) {
        List<Card> cards = java.util.stream.IntStream.range(0, librarySize)
                .mapToObj(i -> (Card) new Forest()).toList();
        harness.setLibrary(player1, cards);
        harness.castFromHand(player1, new HostileNegotiations(), "{3}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        int firstEnd = Math.min(3, librarySize);
        int secondEnd = Math.min(6, librarySize);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrderElementsOf(cards.subList(0, firstEnd).stream().map(Card::getId).toList());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> !(card instanceof HostileNegotiations)).extracting(Card::getId)
                .containsExactlyInAnyOrderElementsOf(cards.subList(firstEnd, secondEnd).stream().map(Card::getId).toList());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(cards.subList(secondEnd, librarySize));
        assertThat(gd.exiledCards).isEmpty();
        harness.assertLife(player1, 17);
    }

    @Test
    void opponentCanChooseEmptyPile() {
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.castFromHand(player1, new HostileNegotiations(), "{3}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId).contains(forest.getId());
        assertThat(gd.exiledCards).isEmpty();
        harness.assertLife(player1, 17);
    }

    @Test
    void graveyardPileRespectsLeylineOfTheVoid() {
        Card forest = new Forest();
        Card swamp = new Swamp();
        Card island = new Island();
        harness.addToBattlefield(player2, new LeylineOfTheVoid());
        harness.setLibrary(player1, List.of(forest, swamp, island));
        harness.castFromHand(player1, new HostileNegotiations(), "{3}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId())
                .contains(forest.getId(), swamp.getId(), island.getId());
        harness.assertLife(player1, 17);
    }
}
