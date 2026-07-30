package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MindclawShamanTest extends BaseCardTest {

    private void castShaman() {
        harness.setHand(player1, new ArrayList<>(List.of(new MindclawShaman())));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities(); // creature resolves -> ETB trigger on stack
        harness.passBothPriorities(); // ETB trigger resolves
    }

    @Test
    @DisplayName("ETB lets the controller cast an instant/sorcery from the opponent's hand for free")
    void castsOpponentSorceryForFree() {
        Divination stolen = new Divination();
        harness.setHand(player2, new ArrayList<>(List.of(stolen)));

        castShaman();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(stolen.getId());
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.playerHands.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(stolen.getId()));
    }

    @Test
    @DisplayName("Declining leaves the spell in the opponent's hand")
    void decliningLeavesSpellInHand() {
        Divination stolen = new Divination();
        harness.setHand(player2, new ArrayList<>(List.of(stolen)));

        castShaman();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(stolen.getId()));
    }

    @Test
    @DisplayName("Non-instant/sorcery cards in the opponent's hand are not offered")
    void creatureIsNotOffered() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        castShaman();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Empty opponent hand offers nothing")
    void emptyHandOffersNothing() {
        harness.setHand(player2, new ArrayList<>());

        castShaman();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
