package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BakuAltar;
import com.github.laxika.magicalvibes.cards.d.DisruptingShoal;
import com.github.laxika.magicalvibes.cards.k.KamiOfFalseHope;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PsychicSpear.class, KamiOfFalseHope.class, DisruptingShoal.class, BakuAltar.class})
class PsychicSpearTest extends BaseCardTest {

    @Test
    @DisplayName("Caster chooses a Spirit card and it is discarded")
    void choosingSpiritDiscardsIt() {
        harness.setHand(player2, List.of(new KamiOfFalseHope(), new BakuAltar()));

        harness.setHand(player1, List.of(new PsychicSpear()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).choosingPlayerId())
                .isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Kami of False Hope");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Caster chooses an Arcane card and it is discarded")
    void choosingArcaneDiscardsIt() {
        harness.setHand(player2, List.of(new DisruptingShoal(), new BakuAltar()));

        harness.setHand(player1, List.of(new PsychicSpear()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Disrupting Shoal");
        harness.assertInHand(player2, "Baku Altar");
    }

    @Test
    @DisplayName("Only Spirit and Arcane cards are valid choices")
    void onlySpiritOrArcaneChoosable() {
        harness.setHand(player2, List.of(new BakuAltar(), new DisruptingShoal(), new KamiOfFalseHope()));

        harness.setHand(player1, List.of(new PsychicSpear()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(1, 2);
    }

    @Test
    @DisplayName("Hand with no Spirit or Arcane card yields no valid choices")
    void noMatchingCardsNoChoice() {
        harness.setHand(player2, List.of(new BakuAltar()));

        harness.setHand(player1, List.of(new PsychicSpear()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no valid choices"));
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        harness.setHand(player1, List.of(new PsychicSpear(), new KamiOfFalseHope()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Kami of False Hope");
    }
}
