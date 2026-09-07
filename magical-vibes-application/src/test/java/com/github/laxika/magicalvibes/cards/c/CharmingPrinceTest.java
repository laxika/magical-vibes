package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CharmingPrince.class, GrizzlyBears.class})
class CharmingPrinceTest extends BaseCardTest {

    @Test
    @DisplayName("Scry mode opens a two-card scry choice")
    void scryMode() {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.add(0, new GrizzlyBears());
        deck.add(1, new GrizzlyBears());

        castPrince(0, null);

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
    }

    @Test
    @DisplayName("Gain-life mode gains 3 life")
    void gainLifeMode() {
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        castPrince(1, null);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Flicker mode can target an owned creature controlled by an opponent")
    void flickerOwnedCreatureControlledByOpponent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.stolenCreatures.put(target.getId(), player1.getId());

        castPrince(2, target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Flicker mode rejects a creature the controller does not own")
    void flickerUnownedCreatureRejected() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CharmingPrince()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("own");
    }

    private void castPrince(int mode, UUID targetId) {
        harness.setHand(player1, List.of(new CharmingPrince()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0, mode, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
