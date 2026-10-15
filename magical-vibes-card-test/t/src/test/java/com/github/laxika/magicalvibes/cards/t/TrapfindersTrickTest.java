package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArchiveTrap;
import com.github.laxika.magicalvibes.cards.a.ArrowVolleyTrap;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrapfindersTrick.class, ArrowVolleyTrap.class, ArchiveTrap.class, GrizzlyBears.class})
class TrapfindersTrickTest extends BaseCardTest {

    @Test
    @DisplayName("Target player discards all Trap cards and keeps other cards")
    void discardsAllTrapCards() {
        harness.setHand(player2, new ArrayList<>(List.of(
                new ArrowVolleyTrap(), new GrizzlyBears(), new ArchiveTrap())));
        harness.setHand(player1, List.of(new TrapfindersTrick()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

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

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Reveals a nonempty hand even when it contains no Traps")
    void revealsHandWithoutTraps() {
        TrapfindersTrick retained = new TrapfindersTrick();
        harness.setHand(player2, List.of(retained));
        harness.setHand(player1, List.of(new TrapfindersTrick()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(harness.getConn1().getMessagesContaining("REVEAL_HAND"))
                .anySatisfy(message -> assertThat(message).contains("Trapfinder's Trick"));
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_HAND"))
                .anySatisfy(message -> assertThat(message).contains("Trapfinder's Trick"));
        harness.assertInGraveyard(player1, "Trapfinder's Trick");
    }

    @Test
    @DisplayName("Can target its caster and discard only their Traps")
    void canTargetSelf() {
        ArrowVolleyTrap arrow = new ArrowVolleyTrap();
        ArchiveTrap archive = new ArchiveTrap();
        TrapfindersTrick retained = new TrapfindersTrick();
        ArchiveTrap opponentsTrap = new ArchiveTrap();
        harness.setHand(player1, List.of(new TrapfindersTrick(), arrow, retained, archive));
        harness.setHand(player2, List.of(opponentsTrap));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(arrow, archive);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentsTrap);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
