package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TamiyoCollectorOfTales;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({Balance.class, Bloodbriar.class, Forest.class, GrizzlyBears.class, TamiyoCollectorOfTales.class})
class BalanceTest extends BaseCardTest {

    private List<UUID> landIds(Player player, int limit) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().hasType(CardType.LAND))
                .limit(limit)
                .map(Permanent::getId)
                .toList();
    }

    private List<UUID> creatureIds(Player player, int limit) {
        return findPermanents(player, "Grizzly Bears").stream()
                .limit(limit)
                .map(Permanent::getId)
                .toList();
    }

    private long creatureCount(Player player) {
        return countPermanents(player, "Grizzly Bears");
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
                new Balance(), new GrizzlyBears(), new Forest(), new Forest())));
        harness.setHand(player2, new ArrayList<>(List.of(new Forest())));
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
        harness.setHand(player1, new ArrayList<>(List.of(new Balance(), new GrizzlyBears(), new Forest())));
        harness.setHand(player2, new ArrayList<>(List.of(new Forest())));
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

    @Test
    @DisplayName("Lets the nonactive player choose excess lands, cards, and creatures")
    void letsNonactivePlayerChooseEachExcessCategory() {
        harness.setHand(player1, List.of(new Balance(), new Forest()));
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears(), new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addToBattlefield(player1, new Forest());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player2, new Forest());
        }
        harness.addToBattlefield(player1, new GrizzlyBears());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
        }

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice landChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(landChoice).isNotNull();
        assertThat(landChoice.playerId()).isEqualTo(player2.getId());
        assertThat(landChoice.maxCount()).isEqualTo(2);
        harness.handleMultiplePermanentsChosen(player2, landIds(player2, 2));

        PendingInteraction.DiscardChoice discardChoice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(discardChoice).isNotNull();
        assertThat(discardChoice.playerId()).isEqualTo(player2.getId());
        assertThat(discardChoice.remainingCount()).isEqualTo(2);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        PendingInteraction.MultiPermanentChoice creatureChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(creatureChoice).isNotNull();
        assertThat(creatureChoice.playerId()).isEqualTo(player2.getId());
        assertThat(creatureChoice.maxCount()).isEqualTo(2);
        harness.handleMultiplePermanentsChosen(player2, creatureIds(player2, 2));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(landCount(player1)).isEqualTo(1);
        assertThat(landCount(player2)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(creatureCount(player1)).isEqualTo(1);
        assertThat(creatureCount(player2)).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers a sacrifice ability when a chosen permanent is sacrificed")
    void triggersSacrificeAbilityForChosenPermanent() {
        harness.setHand(player1, List.of(new Balance()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 2);
        Permanent bloodbriar = addCreatureReady(player1, new Bloodbriar());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultiplePermanentsChosen(player1, landIds(player1, 1));
        harness.passBothPriorities();

        assertThat(bloodbriar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not force an opponent with Tamiyo to sacrifice permanents")
    void respectsOpponentEffectsCantCauseSacrifice() {
        harness.setHand(player1, List.of(new Balance()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 2);
        Permanent tamiyo = harness.addToBattlefieldAndReturn(player2, new TamiyoCollectorOfTales());
        tamiyo.setCounterCount(CounterType.LOYALTY, 5);
        harness.addToBattlefield(player2, new Forest());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Tamiyo, Collector of Tales");
        harness.assertOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Does not force an opponent with Tamiyo to discard cards")
    void respectsOpponentEffectsCantCauseDiscard() {
        harness.setHand(player1, List.of(new Balance()));
        harness.setHand(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        Permanent tamiyo = harness.addToBattlefieldAndReturn(player2, new TamiyoCollectorOfTales());
        tamiyo.setCounterCount(CounterType.LOYALTY, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.assertOnBattlefield(player2, "Tamiyo, Collector of Tales");
    }

    @Test
    @DisplayName("Triggers for another permanent sacrificed at the same time")
    void triggersForAnotherPermanentSacrificedSimultaneously() {
        harness.setHand(player1, List.of(new Balance()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 2);
        addCreatureReady(player1, new Bloodbriar());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Bloodbriar");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gameLogContains("Bloodbriar's ability triggers.")).isTrue();
    }
}
