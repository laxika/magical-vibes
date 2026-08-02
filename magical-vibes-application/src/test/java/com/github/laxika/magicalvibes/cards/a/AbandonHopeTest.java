package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AbandonHopeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting discards X cards as an additional cost and puts the spell on the stack")
    void castingDiscardsXCardsAsAdditionalCost() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new SerraAngel())));
        harness.setHand(player1, new ArrayList<>(
                List.of(new AbandonHope(), new GrizzlyBears(), new GiantGrowth(), new LightningBolt())));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithDiscards(player1, 0, 2, player2.getId(), List.of(1, 2));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getXValue()).isEqualTo(2);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Lightning Bolt");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Giant Growth");
    }

    @Test
    @DisplayName("Resolving lets the caster choose X cards from the opponent's hand to be discarded")
    void resolvingDiscardsChosenCardsFromOpponentHand() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new SerraAngel(), new LightningBolt())));
        harness.setHand(player1, new ArrayList<>(List.of(new AbandonHope(), new GiantGrowth(), new GiantGrowth())));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithDiscards(player1, 0, 2, player2.getId(), List.of(1, 2));
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.remainingCount()).isEqualTo(2);
        assertThat(choice.discardMode()).isTrue();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Lightning Bolt");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Serra Angel");
        harness.assertInGraveyard(player1, "Abandon Hope");
    }

    @Test
    @DisplayName("X=0 discards nothing on either side and prompts no choice")
    void xZeroDiscardsNothing() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new SerraAngel())));
        harness.setHand(player1, new ArrayList<>(List.of(new AbandonHope(), new GiantGrowth())));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorceryWithDiscards(player1, 0, 0, player2.getId(), List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("X greater than the opponent's hand size discards their whole hand")
    void xGreaterThanOpponentHandDiscardsAll() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setHand(player1, new ArrayList<>(
                List.of(new AbandonHope(), new GiantGrowth(), new GiantGrowth(), new GiantGrowth())));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorceryWithDiscards(player1, 0, 3, player2.getId(), List.of(1, 2, 3));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).remainingCount())
                .isEqualTo(1);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cast is rejected, with no mana or cards spent, when the hand cannot cover X discards")
    void castRejectedWhenNotEnoughCardsToDiscard() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setHand(player1, new ArrayList<>(List.of(new AbandonHope(), new GiantGrowth())));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorceryWithDiscards(player1, 0, 2, player2.getId(), List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard 2 cards");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
    }

    @Test
    @DisplayName("Abandon Hope cannot be discarded to pay for itself")
    void cannotDiscardItselfForItsOwnCost() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setHand(player1, new ArrayList<>(List.of(new AbandonHope(), new GiantGrowth())));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorceryWithDiscards(player1, 0, 1, player2.getId(), List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot target yourself — the spell only targets an opponent")
    void cannotTargetSelf() {
        harness.setHand(player1, new ArrayList<>(List.of(new AbandonHope(), new GiantGrowth(), new GrizzlyBears())));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorceryWithDiscards(player1, 0, 1, player1.getId(), List.of(1)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }
}
