package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PoxTest extends BaseCardTest {

    private List<UUID> landIds(Player player, int limit) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .limit(limit)
                .map(Permanent::getId)
                .toList();
    }

    private List<UUID> creatureIds(Player player, int limit) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .limit(limit)
                .map(Permanent::getId)
                .toList();
    }

    private long creatureCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .count();
    }

    private long landCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .count();
    }

    // ===== Life loss =====

    @Test
    @DisplayName("Each player loses a third of their life, rounded up")
    void eachPlayerLosesThirdOfLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 10);
        harness.setHand(player1, List.of(new Pox()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // ceil(20/3) = 7 -> 13; ceil(10/3) = 4 -> 6
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(6);
        // Nothing else to do — resolution finished with no pending choice.
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    // ===== Discard =====

    @Test
    @DisplayName("Each player discards a third of their hand, rounded up, computed per player")
    void eachPlayerDiscardsThirdOfHand() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        // After casting Pox the caster's hand holds four cards -> ceil(4/3) = 2 discards.
        harness.setHand(player1, new ArrayList<>(List.of(
                new Pox(), new GrizzlyBears(), new Peek(), new Forest(), new Forest())));
        // Two cards -> ceil(2/3) = 1 discard.
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Active player (caster) discards first: two cards.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        // Then player2 discards one.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    // ===== Sacrifice creatures =====

    @Test
    @DisplayName("Each player sacrifices a third of their creatures, rounded up, of their choice")
    void eachPlayerSacrificesThirdOfCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Pox()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 3);
        // Player1: three creatures -> ceil(3/3) = 1 to sacrifice (choice, since 3 > 1).
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultiplePermanentsChosen(player1, creatureIds(player1, 1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(creatureCount(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("A player sacrifices all their creatures with no choice when the third rounds to their whole board")
    void autoSacrificesWhenMatchesDoNotExceedCount() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Pox()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 3);
        // One creature -> ceil(1/3) = 1, so the whole board is sacrificed with no prompt.
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(creatureCount(player1)).isEqualTo(0);
    }

    // ===== Sacrifice lands =====

    @Test
    @DisplayName("Each player sacrifices a third of their lands, rounded up, of their choice")
    void eachPlayerSacrificesThirdOfLands() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Pox()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 3);
        // Player1: six lands -> ceil(6/3) = 2 to sacrifice (choice, since 6 > 2).
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player1, new Forest());
        }

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultiplePermanentsChosen(player1, landIds(player1, 2));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(landCount(player1)).isEqualTo(4);
    }

    // ===== Full sequence =====

    @Test
    @DisplayName("Runs all four steps in order for the caster")
    void runsAllFourStepsInOrder() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        // After casting Pox the caster holds three cards -> ceil(3/3) = 1 discard.
        harness.setHand(player1, new ArrayList<>(List.of(
                new Pox(), new GrizzlyBears(), new Peek(), new Forest())));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 3);
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears()); // -> sacrifice 1
        }
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new Forest()); // -> sacrifice 1
        }

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // 1) Life: ceil(20/3) = 7 -> 13.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);

        // 2) Discard one card.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        // 3) Sacrifice one creature (3 > 1 -> choice).
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, creatureIds(player1, 1));

        // 4) Sacrifice one land (3 > 1 -> choice).
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, landIds(player1, 1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(creatureCount(player1)).isEqualTo(2);
        assertThat(landCount(player1)).isEqualTo(2);
    }
}
