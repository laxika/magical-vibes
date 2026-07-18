package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
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

class BalanceTest extends BaseCardTest {

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

    // ===== Lands =====

    @Test
    @DisplayName("Each player keeps lands down to the fewest any player controls, of their choice")
    void balancesLandsDownToFewest() {
        harness.setHand(player1, List.of(new Balance()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 2);
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        for (int i = 0; i < 2; i++) {
            harness.addToBattlefield(player2, new Forest());
        }

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // fewest = 2, so player1 sacrifices 5 - 2 = 3 lands of their choice.
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(3);

        harness.handleMultiplePermanentsChosen(player1, landIds(player1, 3));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(landCount(player1)).isEqualTo(2);
        assertThat(landCount(player2)).isEqualTo(2);
    }

    @Test
    @DisplayName("When a player controls no lands, everyone sacrifices all their lands with no choice")
    void balancesLandsToZeroWithNoPrompt() {
        harness.setHand(player1, List.of(new Balance()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 2);
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        // player2 controls no lands -> fewest = 0.

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(landCount(player1)).isEqualTo(0);
    }

    // ===== Discard =====

    @Test
    @DisplayName("Each player discards down to the smallest hand size, of their choice")
    void balancesHandsDownToFewest() {
        // After casting Balance the caster's hand holds three cards; player2 holds one.
        harness.setHand(player1, new ArrayList<>(List.of(
                new Balance(), new GrizzlyBears(), new Peek(), new Forest())));
        harness.setHand(player2, new ArrayList<>(List.of(new Peek())));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // fewest hand = 1, so player1 discards 3 - 1 = 2 cards; player2 discards none.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    // ===== Creatures =====

    @Test
    @DisplayName("Each player keeps creatures down to the fewest any player controls, of their choice")
    void balancesCreaturesDownToFewest() {
        harness.setHand(player1, List.of(new Balance()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 2);
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // fewest = 1, so player1 sacrifices 3 - 1 = 2 creatures of their choice.
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultiplePermanentsChosen(player1, creatureIds(player1, 2));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(creatureCount(player1)).isEqualTo(1);
        assertThat(creatureCount(player2)).isEqualTo(1);
    }

    // ===== Full sequence =====

    @Test
    @DisplayName("Runs lands, discard, then creatures in order for the caster")
    void runsAllThreeStepsInOrder() {
        // After casting Balance the caster holds two cards; player2 holds one -> discard 1.
        harness.setHand(player1, new ArrayList<>(List.of(new Balance(), new GrizzlyBears(), new Peek())));
        harness.setHand(player2, new ArrayList<>(List.of(new Peek())));
        harness.addMana(player1, ManaColor.WHITE, 2);
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new Forest()); // player2 has 1 land -> sacrifice 2
        }
        harness.addToBattlefield(player2, new Forest());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears()); // player2 has 0 -> sacrifice all 3
        }

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // 1) Lands: fewest 1 -> sacrifice 2 (choice).
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, landIds(player1, 2));

        // 2) Discard one card (fewest hand 1, caster holds 2).
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        // 3) Creatures: fewest 0 -> sacrifice all three with no prompt.
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(landCount(player1)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(creatureCount(player1)).isEqualTo(0);
    }
}
