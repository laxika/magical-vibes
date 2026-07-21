package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecklessScholarTest extends BaseCardTest {

    private Permanent readyScholar() {
        Permanent scholar = addCreatureReady(player1, new RecklessScholar());
        return scholar;
    }

    @Test
    @DisplayName("Target opponent draws a card, then discards a card")
    void opponentLoots() {
        Permanent scholar = readyScholar();
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(new Forest());

        harness.activateAbility(player1, 0, null, player2.getId());
        assertThat(scholar.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Can target self to draw then discard")
    void controllerLoots() {
        readyScholar();
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        readyScholar();
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
