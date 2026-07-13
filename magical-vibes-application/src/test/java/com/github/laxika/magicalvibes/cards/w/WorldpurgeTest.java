package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WorldpurgeTest extends BaseCardTest {

    private void keepEntireHand(com.github.laxika.magicalvibes.model.Player player) {
        List<UUID> handIds = gd.playerHands.get(player.getId()).stream().map(Card::getId).toList();
        harness.handleMultipleCardsChosen(player, handIds);
    }

    @Test
    @DisplayName("Returns all permanents to their owners' hands; kept cards stay in hand")
    void returnsAllPermanentsAndPlayersKeepHands() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new Worldpurge()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        // Active player (player1) chooses first.
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.KeepCardsInHandChoice.class);
        keepEntireHand(player1);
        keepEntireHand(player2);

        // Both creatures are back in their owners' hands; battlefields are empty.
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cards not kept are shuffled into the owner's library")
    void unkeptCardsAreShuffledIntoLibrary() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card kept = new GrizzlyBears();
        Card shuffled = new LlanowarElves();
        harness.setHand(player1, List.of(new Worldpurge(), kept, shuffled));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        // Keep only the Grizzly Bears; the Llanowar Elves goes into the library.
        harness.handleMultipleCardsChosen(player1, List.of(kept.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player1.getId())).contains(shuffled);
    }

    @Test
    @DisplayName("Keeping zero cards shuffles the whole hand into the library")
    void keepingZeroShufflesWholeHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Card a = new GrizzlyBears();
        Card b = new LlanowarElves();
        harness.setHand(player1, List.of(new Worldpurge(), a, b));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 8);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).contains(a, b);
    }

    @Test
    @DisplayName("Each player loses all unspent mana")
    void eachPlayerLosesUnspentMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player1, List.of(new Worldpurge()));
        harness.setHand(player2, List.of());
        // 8 to pay the spell + 2 left floating; player2 has floating mana too.
        harness.addMana(player1, ManaColor.WHITE, 10);
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();

        // No player has cards to keep, so resolution runs straight through to the mana-loss step.
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
    }
}
