package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FathomSeer.class, Island.class})
class FathomSeerTest extends BaseCardTest {

    @Test
    void returnsTwoIslandsAndDrawsTwoCardsWhenTurnedFaceUp() {
        Permanent firstIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent secondIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Card firstDraw = new FathomSeer();
        Card secondDraw = new FathomSeer();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        Permanent fathomSeer = castFaceDown();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(firstDraw, secondDraw);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(fathomSeer),
                List.of(firstIsland.getId(), secondIsland.getId()));
        harness.passBothPriorities();

        assertThat(fathomSeer.isFaceDown()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(firstIsland, secondIsland);
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(firstIsland.getCard(), secondIsland.getCard(), firstDraw, secondDraw);
    }

    @Test
    void cannotTurnFaceUpByReturningNonIslandPermanent() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent nonIsland = harness.addToBattlefieldAndReturn(player1, new FathomSeer());
        Permanent fathomSeer = castFaceDown();

        assertThatThrownBy(() -> turnFaceUp(fathomSeer, List.of(island.getId(), nonIsland.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(fathomSeer.isFaceDown()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(island, nonIsland, fathomSeer);
        assertThat(gd.playerHands.get(player1.getId()))
                .doesNotContain(island.getCard(), nonIsland.getCard());
    }

    @Test
    void cannotTurnFaceUpByReturningOpponentControlledIsland() {
        Permanent ownIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent opponentIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent fathomSeer = castFaceDown();

        assertThatThrownBy(() -> turnFaceUp(fathomSeer, List.of(ownIsland.getId(), opponentIsland.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(fathomSeer.isFaceDown()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(ownIsland, fathomSeer);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(opponentIsland);
    }

    @Test
    void tappedIslandsAreReturnedImmediatelyButDrawingUsesTheStack() {
        Permanent firstIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent secondIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        firstIsland.tap();
        secondIsland.tap();
        Card firstDraw = new FathomSeer();
        Card secondDraw = new FathomSeer();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        Permanent fathomSeer = castFaceDown();

        turnFaceUp(fathomSeer, List.of(firstIsland.getId(), secondIsland.getId()));

        assertThat(fathomSeer.isFaceDown()).isFalse();
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(firstIsland.getCard(), secondIsland.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(firstIsland.getCard(), secondIsland.getCard(), firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotReturnTheSameIslandTwice() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent fathomSeer = castFaceDown();

        assertThatThrownBy(() -> turnFaceUp(fathomSeer, List.of(island.getId(), island.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(fathomSeer.isFaceDown()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(island, fathomSeer);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTurnFaceUpByReturningOnlyOneIsland() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent fathomSeer = castFaceDown();

        assertThatThrownBy(() -> turnFaceUp(fathomSeer, List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(fathomSeer.isFaceDown()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(island, fathomSeer);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void castingFaceUpDoesNotDrawCards() {
        Card firstDraw = new FathomSeer();
        Card secondDraw = new FathomSeer();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        harness.castFromHand(player1, new FathomSeer(), "{1}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new FathomSeer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isFaceDown)
                .findFirst()
                .orElseThrow();
    }

    private void turnFaceUp(Permanent fathomSeer, List<UUID> additionalCostPermanentIds) {
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(fathomSeer),
                additionalCostPermanentIds);
    }
}
