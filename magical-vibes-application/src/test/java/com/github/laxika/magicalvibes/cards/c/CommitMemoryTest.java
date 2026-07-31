package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommitMemoryTest extends BaseCardTest {

    @Test
    @DisplayName("Commit puts target nonland permanent second from the top of its owner's library")
    void commitTucksNonlandPermanentSecondFromTop() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = new Island();
        setDeck(player2, List.of(topCard, new Island(), new Island()));

        harness.setHand(player1, List.of(new CommitMemory()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

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
        setDeck(player2, List.of(topCard, new Island()));

        harness.setHand(player2, List.of(new Shock()));
        harness.setHand(player1, List.of(new CommitMemory()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        UUID shockId = gd.stack.getFirst().getCard().getId();

        harness.forceActivePlayer(player1);
        harness.castInstant(player1, 0, shockId);
        harness.passBothPriorities();

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
        fillDeck(player1, 20);
        fillDeck(player2, 20);

        Card gyCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new CommitMemory(), gyCard));
        harness.setHand(player1, List.of(new Island(), new Island()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

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

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }

    private void fillDeck(Player player, int count) {
        List<Card> deck = gd.playerDecks.get(player.getId());
        if (deck == null) {
            deck = new ArrayList<>();
            gd.playerDecks.put(player.getId(), deck);
        }
        for (int i = 0; i < count; i++) {
            deck.add(new Island());
        }
    }
}
