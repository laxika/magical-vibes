package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PullingTeethTest extends BaseCardTest {

    private void prepare() {
        harness.setHand(player1, List.of(new PullingTeeth()));
        harness.addMana(player1, ManaColor.BLACK, 2); // {1}{B}
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new SerraAngel(), new LightningBolt())));
    }

    @Test
    @DisplayName("Winning the clash makes the target player discard two cards")
    void winningDiscardsTwo() {
        prepare();
        // Caster reveals Grizzly Bears (MV 2), opponent reveals Forest (MV 0) → caster wins.
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Won clash: an extra discard is queued on top of the guaranteed one — two discards total.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Losing the clash makes the target player discard only one card")
    void losingDiscardsOne() {
        prepare();
        // Both reveal Forest (MV 0) → tie, caster does not win.
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Target player with an empty hand discards nothing even on a won clash")
    void emptyHandDiscardsNothing() {
        harness.setHand(player1, List.of(new PullingTeeth()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player2, new ArrayList<>());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
