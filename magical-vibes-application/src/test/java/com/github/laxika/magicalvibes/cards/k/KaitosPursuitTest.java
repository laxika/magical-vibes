package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.SpiritedCompanion;
import com.github.laxika.magicalvibes.cards.n.NetworkDisruptor;
import com.github.laxika.magicalvibes.cards.m.MoonCircuitHacker;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KaitosPursuit.class, NetworkDisruptor.class, SpiritedCompanion.class, MoonCircuitHacker.class})
class KaitosPursuitTest extends BaseCardTest {

    @Test
    @DisplayName("Target player discards two cards and your Ninjas and Rogues gain menace")
    void discardsAndGrantsMenaceToNinjasAndRogues() {
        Permanent ninja = harness.addToBattlefieldAndReturn(player1, new MoonCircuitHacker());
        Permanent rogue = harness.addToBattlefieldAndReturn(player1, new NetworkDisruptor());
        Permanent nonmatching = harness.addToBattlefieldAndReturn(player1, new SpiritedCompanion());
        Permanent opponentNinja = harness.addToBattlefieldAndReturn(player2, new MoonCircuitHacker());
        harness.setHand(player2, new ArrayList<>(List.of(new SpiritedCompanion(), new KaitosPursuit(), new SpiritedCompanion())));
        harness.setHand(player1, List.of(new KaitosPursuit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gqs.hasKeyword(gd, ninja, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, rogue, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonmatching, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentNinja, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Menace lasts until end of turn")
    void menaceWearsOffAtEndOfTurn() {
        Permanent ninja = harness.addToBattlefieldAndReturn(player1, new MoonCircuitHacker());
        harness.setHand(player2, new ArrayList<>(List.of(new KaitosPursuit(), new SpiritedCompanion())));
        harness.setHand(player1, List.of(new KaitosPursuit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gqs.hasKeyword(gd, ninja, Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ninja, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Can target only a player")
    void cannotTargetPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoonCircuitHacker());
        harness.setHand(player1, List.of(new KaitosPursuit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("This spell can only target players");
    }

    @Test
    @DisplayName("An empty target hand does not prevent your Ninjas gaining menace")
    void emptyHandStillGrantsMenace() {
        Permanent ninja = harness.addToBattlefieldAndReturn(player1, new MoonCircuitHacker());
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new KaitosPursuit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.hasKeyword(gd, ninja, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("A player with one card discards it and the menace grant still resolves")
    void oneCardHandStillGrantsMenace() {
        Permanent ninja = harness.addToBattlefieldAndReturn(player1, new MoonCircuitHacker());
        KaitosPursuit discarded = new KaitosPursuit();
        harness.setHand(player2, List.of(discarded));
        harness.setHand(player1, List.of(new KaitosPursuit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.hasKeyword(gd, ninja, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("You can target yourself and discard your own cards")
    void canTargetSelf() {
        Permanent ninja = harness.addToBattlefieldAndReturn(player1, new MoonCircuitHacker());
        KaitosPursuit firstDiscard = new KaitosPursuit();
        KaitosPursuit secondDiscard = new KaitosPursuit();
        harness.setHand(player1, List.of(new KaitosPursuit(), firstDiscard, secondDiscard));
        harness.setHand(player2, List.of(new KaitosPursuit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstDiscard, secondDiscard);
        assertThat(gqs.hasKeyword(gd, ninja, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Ninjas entering after resolution do not gain menace")
    void laterNinjasDoNotGainMenace() {
        Permanent originalNinja = harness.addToBattlefieldAndReturn(player1, new MoonCircuitHacker());
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new KaitosPursuit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        Permanent laterNinja = harness.addToBattlefieldAndReturn(player1, new MoonCircuitHacker());

        assertThat(gqs.hasKeyword(gd, originalNinja, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, laterNinja, Keyword.MENACE)).isFalse();
    }
}
