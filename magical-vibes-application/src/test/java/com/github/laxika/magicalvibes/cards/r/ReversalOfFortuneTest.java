package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReversalOfFortuneTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a spell from the opponent's hand and may cast the copy for free")
    void copiesAndCastsSpellCopy() {
        Divination original = new Divination();
        castReversal(original, new GrizzlyBears());

        PendingInteraction.TargetHandSpellCopyChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.TargetHandSpellCopyChoice.class);
        assertThat(choice.validCardIds()).containsExactly(original.getId());

        harness.handleMultipleCardsChosen(player1, List.of(original.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        StackEntry copyEntry = gd.stack.getFirst();
        assertThat(copyEntry.getCard().getName()).isEqualTo("Divination");
        assertThat(copyEntry.getCard().getId()).isNotEqualTo(original.getId());
        assertThat(copyEntry.getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(original.getId()));
    }

    @Test
    @DisplayName("Declining the card choice leaves the revealed hand unchanged")
    void decliningCardChoiceLeavesHandUnchanged() {
        Divination original = new Divination();
        castReversal(original, new GrizzlyBears());

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(original.getId()));
    }

    @Test
    @DisplayName("Declining the free cast leaves the original card in the opponent's hand")
    void decliningFreeCastLeavesHandUnchanged() {
        Divination original = new Divination();
        castReversal(original, new GrizzlyBears());

        harness.handleMultipleCardsChosen(player1, List.of(original.getId()));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(original.getId()));
    }

    private void castReversal(Divination original, GrizzlyBears creature) {
        harness.setHand(player1, List.of(new ReversalOfFortune()));
        harness.setHand(player2, List.of(original, creature));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }
}
