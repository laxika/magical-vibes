package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.ApothecaryGeist;
import com.github.laxika.magicalvibes.cards.b.BlessedBreath;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PsychicSpearTest extends BaseCardTest {

    @Test
    @DisplayName("Caster chooses a Spirit card and it is discarded")
    void choosingSpiritDiscardsIt() {
        harness.setHand(player2, new ArrayList<>(List.of(new ApothecaryGeist(), new GrizzlyBears())));

        harness.setHand(player1, List.of(new PsychicSpear()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).choosingPlayerId())
                .isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Apothecary Geist");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Only Spirit and Arcane cards are valid choices")
    void onlySpiritOrArcaneChoosable() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new BlessedBreath(), new ApothecaryGeist())));

        harness.setHand(player1, List.of(new PsychicSpear()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(1, 2);
    }

    @Test
    @DisplayName("Hand with no Spirit or Arcane card yields no valid choices")
    void noMatchingCardsNoChoice() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        harness.setHand(player1, List.of(new PsychicSpear()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no valid choices"));
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        harness.setHand(player1, new ArrayList<>(List.of(new PsychicSpear(), new ApothecaryGeist())));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Apothecary Geist");
    }
}
