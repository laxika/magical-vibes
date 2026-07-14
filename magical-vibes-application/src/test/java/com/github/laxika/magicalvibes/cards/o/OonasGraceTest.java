package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OonasGraceTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws a card")
    void targetPlayerDrawsACard() {
        harness.setHand(player1, List.of(new OonasGrace()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.castInstant(player1, 0, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
        harness.assertInGraveyard(player1, "Oona's Grace");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OonasGrace()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID bearId = bear.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, bearId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Retrace draws a card and discards a land, returning Oona's Grace to the graveyard")
    void retraceDrawsAndDiscardsLand() {
        harness.setGraveyard(player1, List.of(new OonasGrace()));
        harness.setHand(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.castRetrace(player1, 0, 0, player1.getId());
        harness.passBothPriorities();

        // Discarded the Island, drew one card: net zero.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getName().equals("Island"));
        // Retrace returns the spell to the graveyard, not exile.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Oona's Grace"));
    }

    @Test
    @DisplayName("Retrace requires discarding a land card")
    void retraceRequiresLandDiscard() {
        harness.setGraveyard(player1, List.of(new OonasGrace()));
        harness.setHand(player1, List.of(new OonasGrace()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
