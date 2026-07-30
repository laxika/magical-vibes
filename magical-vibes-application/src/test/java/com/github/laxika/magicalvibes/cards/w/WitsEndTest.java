package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WitsEndTest extends BaseCardTest {

    private void castWitsEndOn(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new WitsEnd()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Target player discards their entire hand")
    void discardsWholeHand() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));

        castWitsEndOn(player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Peek");
    }

    @Test
    @DisplayName("An empty hand simply discards nothing")
    void emptyHandDoesNothing() {
        harness.setHand(player2, new ArrayList<>());

        castWitsEndOn(player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target its own controller")
    void canTargetController() {
        harness.setHand(player1, new ArrayList<>(List.of(new WitsEnd(), new GrizzlyBears())));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }
}
