package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CruelUltimatumTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack targeting the opponent")
    void castingPutsOnStack() {
        castUltimatum();

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new CruelUltimatum()));
        addUltimatumMana();

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, player1.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    // ===== Full resolution =====

    @Test
    @DisplayName("Opponent sacrifices, discards three, loses 5; controller returns a creature, draws three, gains 5")
    void fullResolution() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Opponent has a single creature (auto-sacrificed) and a hand to discard from.
        harness.getGameData().playerBattlefields.get(player2.getId()).add(new Permanent(new GrizzlyBears()));
        harness.setHand(player2, new ArrayList<>(List.of(new Plains(), new Plains(), new Plains(), new Plains())));

        // Controller has a creature to return and cards to draw.
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        setupDeck(player1, List.of(new Plains(), new Plains(), new Plains()));

        castUltimatum();
        harness.passBothPriorities();

        // Opponent discards three of their four cards.
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        // Controller returns the creature from their graveyard.
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        // Opponent lost its only creature and three cards, and lost 5 life.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(15);

        // Controller returned the creature to hand, drew three, and gained 5 life.
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4); // returned creature + three drawn
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getLife(player1.getId())).isEqualTo(25);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent chooses which creature to sacrifice when they control several")
    void opponentChoosesSacrifice() {
        Permanent bears = new Permanent(new GrizzlyBears());
        Permanent spider = new Permanent(new GiantSpider());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(spider);
        harness.setHand(player2, new ArrayList<>());
        setupDeck(player1, List.of(new Plains(), new Plains(), new Plains()));

        castUltimatum();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player2, bears.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Giant Spider"));
    }

    @Test
    @DisplayName("Controller half still resolves when opponent has no creature and no cards")
    void controllerHalfResolvesWithEmptyOpponent() {
        harness.setLife(player1, 20);
        harness.setHand(player2, new ArrayList<>());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        setupDeck(player1, List.of(new Plains(), new Plains(), new Plains()));

        castUltimatum();
        harness.passBothPriorities();

        // No sacrifice/discard prompt; the controller half (may-return, draw, gain) is the only interaction.
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.getLife(player1.getId())).isEqualTo(25);
        assertThat(gd.stack).isEmpty();
    }

    // ===== Helpers =====

    private void castUltimatum() {
        harness.setHand(player1, List.of(new CruelUltimatum()));
        addUltimatumMana();
        harness.castSorcery(player1, 0, player2.getId());
    }

    private void addUltimatumMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 2);
    }

    private void setupDeck(com.github.laxika.magicalvibes.model.Player player, List<Card> cards) {
        List<Card> deck = gd.playerDecks.get(player.getId());
        deck.clear();
        deck.addAll(cards);
    }
}
