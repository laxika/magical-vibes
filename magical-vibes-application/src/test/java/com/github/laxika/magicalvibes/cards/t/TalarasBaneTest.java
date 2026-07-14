package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TalarasBaneTest extends BaseCardTest {

    @Test
    @DisplayName("Caster gains life equal to the chosen green creature's toughness, then it is discarded")
    void gainsLifeEqualToToughnessThenDiscards() {
        harness.setHand(player2, new ArrayList<>(List.of(new GiantSpider(), new HillGiant())));
        harness.setHand(player1, List.of(new TalarasBane()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        // Only the green creature (Giant Spider) is a legal choice; the red Hill Giant is filtered out.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        // Giant Spider is 2/4 — gain 4 life.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 4);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Giant Spider"));
        assertThat(gd.playerHands.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Giant Spider"));
    }

    @Test
    @DisplayName("White creatures are valid choices; non-green/white and noncreature cards are not")
    void whiteCreatureValidOthersFiltered() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new SerraAngel(), new FugitiveWizard(), new GrizzlyBears())));
        harness.setHand(player1, List.of(new TalarasBane()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Serra Angel (white) at index 0 and Grizzly Bears (green) at index 2 are legal; the blue
        // Fugitive Wizard at index 1 is not.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0, 2);
    }

    @Test
    @DisplayName("No green or white creature in hand: no life gain, no discard")
    void noValidCreatureDoesNothing() {
        harness.setHand(player2, new ArrayList<>(List.of(new HillGiant(), new FugitiveWizard())));
        harness.setHand(player1, List.of(new TalarasBane()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int startingLife = gd.playerLifeTotals.get(player1.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target self — must target an opponent")
    void cannotTargetSelf() {
        harness.setHand(player1, new ArrayList<>(List.of(new TalarasBane(), new GrizzlyBears())));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
