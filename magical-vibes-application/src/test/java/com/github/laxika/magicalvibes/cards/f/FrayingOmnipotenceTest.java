package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
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

class FrayingOmnipotenceTest extends BaseCardTest {

    private List<UUID> creatureIds(Player player, int limit) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .limit(limit)
                .map(Permanent::getId)
                .toList();
    }

    @Test
    @DisplayName("Each player loses half their life, rounded up")
    void eachPlayerLosesHalfTheirLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 9);
        harness.setHand(player1, List.of(new FrayingOmnipotence()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // ceil(20/2) = 10 -> 10; ceil(9/2) = 5 -> 4
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Each player discards half their hand, rounded up, computed per player")
    void eachPlayerDiscardsHalfTheirHand() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        // After casting, the caster's hand holds four cards -> ceil(4/2) = 2 discards.
        harness.setHand(player1, new ArrayList<>(List.of(
                new FrayingOmnipotence(), new GrizzlyBears(), new Peek(), new Forest(), new Forest())));
        // Three cards -> ceil(3/2) = 2 discards.
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek(), new Forest())));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each player sacrifices half the creatures they control, rounded up, of their choice")
    void eachPlayerSacrificesHalfTheirCreatures() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FrayingOmnipotence()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 5);
        // Player1: five creatures -> ceil(5/2) = 3 to sacrifice (choice, since 5 > 3).
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(3);

        harness.handleMultiplePermanentsChosen(player1, creatureIds(player1, 3));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
    }

    @Test
    @DisplayName("A lone creature is sacrificed with no prompt")
    void loneCreatureSacrificedWithoutPrompt() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FrayingOmnipotence()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 5);
        // One creature -> ceil(1/2) = 1, so the whole board goes with no choice.
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(0);
    }

    @Test
    @DisplayName("Runs life loss, discard and sacrifice in order for the caster")
    void runsAllThreeStepsInOrder() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        // After casting, the caster holds two cards -> ceil(2/2) = 1 discard.
        harness.setHand(player1, new ArrayList<>(List.of(
                new FrayingOmnipotence(), new GrizzlyBears(), new Peek())));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 5);
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears()); // ceil(3/2) = 2
        }

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, creatureIds(player1, 2));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
    }
}
