package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpportunityTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws four cards")
    void targetPlayerDrawsFourCards() {
        int opponentHandBefore = gd.playerHands.get(player2.getId()).size();

        castOpportunityTargeting(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandBefore + 4);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        castOpportunityTargeting(player1.getId());
        harness.passBothPriorities();

        // setHand sets hand to [Opportunity], casting removes it (0), then draws 4
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Does not affect non-targeted player")
    void doesNotAffectNonTargetedPlayer() {
        castOpportunityTargeting(player2.getId());
        harness.passBothPriorities();

        // Player 1's hand only loses the card that was cast
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        castOpportunityTargeting(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Opportunity");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent bear = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bear);

        harness.setHand(player1, List.of(new Opportunity()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castOpportunityTargeting(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new Opportunity()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castInstant(player1, 0, targetPlayerId);
    }
}
