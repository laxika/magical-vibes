package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TyrannizeTest extends BaseCardTest {

    private void castTyrannizeOn(int targetLife) {
        harness.setLife(player2, targetLife);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek())));

        harness.setHand(player1, List.of(new Tyrannize()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Target player pays 7 life to keep their hand")
    void paysLifeKeepsHand() {
        castTyrannizeOn(20);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 13);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Target player declines and discards their whole hand")
    void declinesDiscardsHand() {
        castTyrannizeOn(20);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                .anyMatch(c -> c.getName().equals("Peek"));
    }

    @Test
    @DisplayName("A target with too little life can't pay and discards automatically")
    void cannotPayDiscardsAutomatically() {
        castTyrannizeOn(5);

        // No choice offered — the target can't pay 7 life, so the hand is discarded outright.
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 5);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }
}
