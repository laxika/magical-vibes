package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CommitMemory.class, GrizzlyBears.class, Island.class, Shock.class})
class CommitMemoryTest extends BaseCardTest {

    @Test
    @DisplayName("Commit puts target nonland permanent second from the top of its owner's library")
    void commitTucksNonlandPermanentSecondFromTop() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard, new Island(), new Island()));

        harness.setHand(player1, List.of(new CommitMemory()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        List<Card> library = gd.playerDecks.get(player2.getId());
        assertThat(library.get(0)).isSameAs(topCard);
        assertThat(library.get(1).getName()).isEqualTo("Grizzly Bears");
        harness.assertInGraveyard(player1, "Commit");
    }

    @Test
    @DisplayName("Commit cannot target a land")
    void commitCannotTargetLand() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new CommitMemory()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Commit puts target spell second from the top of its owner's library")
    void commitTucksSpellSecondFromTop() {
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard, new Island()));

        harness.setHand(player2, List.of(new Shock()));
        harness.setHand(player1, List.of(new CommitMemory()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        UUID shockId = gd.stack.getFirst().getCard().getId();

        harness.forceActivePlayer(player1);
        harness.castAndResolveInstant(player1, 0, shockId);

        assertThat(gd.stack).isEmpty();
        List<Card> library = gd.playerDecks.get(player2.getId());
        assertThat(library.get(0)).isSameAs(topCard);
        assertThat(library.get(1).getName()).isEqualTo("Shock");
        harness.assertNotInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player1, "Commit");
    }

    @Test
    @DisplayName("Memory cast from graveyard shuffles hand and graveyard then draws seven, then exiles")
    void memoryShufflesDrawsAndExiles() {
        harness.setLibrary(player1, java.util.stream.Stream.concat(
                gd.playerDecks.get(player1.getId()).stream(),
                java.util.stream.IntStream.range(0, 20).mapToObj(i -> new Island())).toList());
        harness.setLibrary(player2, java.util.stream.Stream.concat(
                gd.playerDecks.get(player2.getId()).stream(),
                java.util.stream.IntStream.range(0, 20).mapToObj(i -> new Island())).toList());

        Card gyCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new CommitMemory(), gyCard));
        harness.setHand(player1, List.of(new Island(), new Island()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveFlashback(player1, 0, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Commit") || c.getName().equals("Memory"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Commit"));
    }

    @Test
    @DisplayName("Memory requires sorcery timing")
    void memoryRequiresSorceryTiming() {
        harness.setGraveyard(player1, List.of(new CommitMemory()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
    }

    @Test
    @DisplayName("Commit puts a permanent on top when its owner's library is empty")
    void commitTucksIntoEmptyLibrary() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new CommitMemory()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(bears.getCard());
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Memory shuffles both players' hands and graveyards into their libraries")
    void memoryRecyclesAllCardsBeforeDrawing() {
        List<Card> recycled1 = java.util.stream.IntStream.range(0, 7)
                .<Card>mapToObj(i -> new Island()).toList();
        List<Card> recycled2 = java.util.stream.IntStream.range(0, 7)
                .<Card>mapToObj(i -> new Island()).toList();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, recycled1.subList(0, 3));
        harness.setHand(player2, recycled2.subList(0, 3));
        harness.setGraveyard(player1, List.of(new CommitMemory(), recycled1.get(3),
                recycled1.get(4), recycled1.get(5), recycled1.get(6)));
        harness.setGraveyard(player2, recycled2.subList(3, 7));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrderElementsOf(recycled1);
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrderElementsOf(recycled2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Commit"));
    }

    @Test
    @DisplayName("Commit exiles an aftermath spell instead of putting it into a library")
    void commitExilesMemoryCastWithAftermath() {
        Card memory = new CommitMemory();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(memory));
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setHand(player2, List.of(new CommitMemory()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castFlashback(player1, 0);
        harness.castAndResolveInstant(player2, 0, memory.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(memory);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(memory);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
