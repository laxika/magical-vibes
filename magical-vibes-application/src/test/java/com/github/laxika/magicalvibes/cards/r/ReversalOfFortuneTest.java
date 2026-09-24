package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GoblinBrawler;
import com.github.laxika.magicalvibes.cards.m.ManaGeyser;
import com.github.laxika.magicalvibes.cards.m.MagmaJet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReversalOfFortune.class, ManaGeyser.class, MagmaJet.class, GoblinBrawler.class})
class ReversalOfFortuneTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a spell from the opponent's hand and may cast the copy for free")
    void copiesAndCastsSpellCopy() {
        ManaGeyser original = new ManaGeyser();
        MagmaJet instant = new MagmaJet();
        castReversal(original, instant, new GoblinBrawler());

        PendingInteraction.TargetHandSpellCopyChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.TargetHandSpellCopyChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(original.getId(), instant.getId());

        harness.handleMultipleCardsChosen(player1, List.of(original.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        StackEntry copyEntry = gd.stack.getFirst();
        assertThat(copyEntry.isCopy()).isTrue();
        assertThat(copyEntry.getCard().getName()).isEqualTo("Mana Geyser");
        assertThat(copyEntry.getCard().getId()).isNotEqualTo(original.getId());
        assertThat(copyEntry.getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(original.getId()));
    }

    @Test
    @DisplayName("Copies and casts an instant from the opponent's hand for free")
    void copiesAndCastsInstantCopy() {
        MagmaJet original = new MagmaJet();
        harness.setLibrary(player1, List.of(new GoblinBrawler(), new GoblinBrawler()));
        castReversal(original, new GoblinBrawler());

        harness.handleMultipleCardsChosen(player1, List.of(original.getId()));
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(original.getId()));
    }

    @Test
    @DisplayName("Declining the card choice leaves the revealed hand unchanged")
    void decliningCardChoiceLeavesHandUnchanged() {
        ManaGeyser original = new ManaGeyser();
        castReversal(original, new GoblinBrawler());

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(original.getId()));
    }

    @Test
    @DisplayName("Declining the free cast leaves the original card in the opponent's hand")
    void decliningFreeCastLeavesHandUnchanged() {
        ManaGeyser original = new ManaGeyser();
        castReversal(original, new GoblinBrawler());

        harness.handleMultipleCardsChosen(player1, List.of(original.getId()));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(original.getId()));
    }

    @Test
    @DisplayName("Finishes without a card choice when the opponent has no instant or sorcery")
    void finishesWithoutEligibleCard() {
        GoblinBrawler creature = new GoblinBrawler();
        castReversal(creature);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(creature);
    }

    @Test
    @DisplayName("Cannot target its controller")
    void cannotTargetItsController() {
        harness.setHand(player1, List.of(new ReversalOfFortune()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private void castReversal(Card... opponentHand) {
        harness.setHand(player1, List.of(new ReversalOfFortune()));
        harness.setHand(player2, List.of(opponentHand));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
