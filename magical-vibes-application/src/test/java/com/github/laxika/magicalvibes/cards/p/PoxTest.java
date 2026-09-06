package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.t.TajuruPreserver;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Pox.class, PaleBears.class, Forest.class})
class PoxTest extends BaseCardTest {

    private List<UUID> landIds(Player player, int limit) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .limit(limit)
                .map(Permanent::getId)
                .toList();
    }

    private List<UUID> creatureIds(Player player, int limit) {
        return findPermanents(player, "Pale Bears").stream()
                .limit(limit)
                .map(Permanent::getId)
                .toList();
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
                new Pox(), new PaleBears(), new Forest(), new Forest(), new Forest())));
        // Two cards -> ceil(2/3) = 1 discard.
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new Forest())));
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
            harness.addToBattlefield(player1, new PaleBears());
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
        assertThat(countPermanents(player1, "Pale Bears")).isEqualTo(2);
    }

    @Test
    @DisplayName("Rounds each non-divisible creature and land count up independently")
    void roundsEachNonDivisiblePermanentCountUp() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Pox()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 3);
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new PaleBears());
            harness.addToBattlefield(player1, new Forest());
        }

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice creatureChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(creatureChoice).isNotNull();
        assertThat(creatureChoice.maxCount()).isEqualTo(2);
        harness.handleMultiplePermanentsChosen(player1, creatureIds(player1, 2));

        PendingInteraction.MultiPermanentChoice landChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(landChoice).isNotNull();
        assertThat(landChoice.maxCount()).isEqualTo(2);
        harness.handleMultiplePermanentsChosen(player1, landIds(player1, 2));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player1, "Pale Bears")).isEqualTo(2);
        assertThat(landCount(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each player chooses creatures to sacrifice in active-player order")
    void eachPlayerChoosesCreaturesInActivePlayerOrder() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Pox()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 3);
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new PaleBears());
            harness.addToBattlefield(player2, new PaleBears());
        }

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice player1Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player1Choice).isNotNull();
        assertThat(player1Choice.playerId()).isEqualTo(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, creatureIds(player1, 1));

        PendingInteraction.MultiPermanentChoice player2Choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(player2Choice).isNotNull();
        assertThat(player2Choice.playerId()).isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, creatureIds(player2, 1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player1, "Pale Bears")).isEqualTo(2);
        assertThat(countPermanents(player2, "Pale Bears")).isEqualTo(2);
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
        harness.addToBattlefield(player1, new PaleBears());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player1, "Pale Bears")).isEqualTo(0);
    }

    @Test
    @CardUsed(TajuruPreserver.class)
    @DisplayName("Does not make an opponent sacrifice permanents protected by Tajuru Preserver")
    void respectsOpponentSacrificePrevention() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Pox()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addToBattlefield(player2, new TajuruPreserver());
        harness.addToBattlefield(player2, new Forest());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countPermanents(player2, "Tajuru Preserver")).isEqualTo(1);
        assertThat(landCount(player2)).isEqualTo(1);
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
                new Pox(), new PaleBears(), new Forest(), new Forest())));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 3);
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new PaleBears()); // -> sacrifice 1
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
        assertThat(countPermanents(player1, "Pale Bears")).isEqualTo(2);
        assertThat(landCount(player1)).isEqualTo(2);
    }
}
