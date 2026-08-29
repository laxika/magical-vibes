package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CruelGrimnarchTest extends BaseCardTest {

    @Test
    @DisplayName("When Cruel Grimnarch enters, an opponent discards a card")
    void opponentDiscardsACard() {
        GrizzlyBears discarded = new GrizzlyBears();
        harness.setHand(player1, new ArrayList<>(List.of(new CruelGrimnarch())));
        harness.setHand(player2, new ArrayList<>(List.of(discarded)));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("When an opponent has no cards, Cruel Grimnarch gains 4 life")
    void gainsLifeForOpponentUnableToDiscard() {
        harness.setHand(player1, new ArrayList<>(List.of(new CruelGrimnarch())));
        harness.setHand(player2, new ArrayList<>());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getLife(player1.getId())).isEqualTo(24);
    }
}
