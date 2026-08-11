package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TomeboundLichTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws a card, then discards a card")
    void entersDrawsThenDiscards() {
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, new ArrayList<>(List.of(new TomeboundLich(), new GrizzlyBears())));
        addManaToCast();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getName().equals("Island"));

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Combat damage to a player draws a card, then discards a card")
    void combatDamageDrawsThenDiscards() {
        Permanent lich = addCreatureReady(player1, new TomeboundLich());
        lich.setAttacking(true);
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, new ArrayList<>(List.of(new Forest())));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getName().equals("Island"));

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Island");
    }

    private void addManaToCast() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
