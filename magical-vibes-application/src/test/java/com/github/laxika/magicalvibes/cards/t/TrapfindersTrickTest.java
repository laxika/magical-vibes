package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArchiveTrap;
import com.github.laxika.magicalvibes.cards.a.ArrowVolleyTrap;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TrapfindersTrickTest extends BaseCardTest {

    @Test
    @DisplayName("Target player discards all Trap cards and keeps other cards")
    void discardsAllTrapCards() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new ArrowVolleyTrap(), new GrizzlyBears(), new ArchiveTrap())));
        harness.setHand(player1, List.of(new TrapfindersTrick()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Arrow Volley Trap");
        harness.assertInGraveyard(player2, "Archive Trap");
        assertThat(gd.playerHands.get(player2.getId()))
                .singleElement()
                .matches(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("An empty hand has no cards to discard")
    void emptyHand() {
        harness.setHand(player2, new ArrayList<>());
        harness.setHand(player1, List.of(new TrapfindersTrick()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
