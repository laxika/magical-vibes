package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RealityRipple;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TeferisImp.class, Island.class, RealityRipple.class})
class TeferisImpTest extends BaseCardTest {

    @Test
    @DisplayName("Phasing out during the controller's untap step makes them discard a card")
    void phasesOutAndDiscards() {
        Permanent imp = harness.addToBattlefieldAndReturn(player1, new TeferisImp());
        Card discardedCard = new Island();
        harness.setHand(player1, List.of(discardedCard));
        harness.setHand(player2, List.of());

        harness.passUntil(player2, TurnStep.UNTAP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(imp);
        harness.passUntil(player1, TurnStep.UNTAP);

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(imp);

        harness.passBothPriorities(); // resolve the phase-out trigger

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(discardedCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard);
    }

    @Test
    @DisplayName("Phasing back in makes the controller draw a card")
    void phasesInAndDraws() {
        Permanent imp = harness.addToBattlefieldAndReturn(player1, new TeferisImp());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island(), new Island(), new Island()));

        harness.passUntil(player2, TurnStep.UNTAP);
        harness.passUntil(player1, TurnStep.UNTAP);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(imp);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UNTAP);
        harness.passUntil(player1, TurnStep.UNTAP);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(imp);

        int handSizeBeforeTrigger = gd.playerHands.get(player1.getId()).size();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        // One card from the phase-in trigger, one from the draw step that follows.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeTrigger + 2);
    }

    @Test
    @DisplayName("Phasing out from an effect also makes the controller discard a card")
    void effectDrivenPhaseOutTriggersDiscard() {
        Permanent imp = harness.addToBattlefieldAndReturn(player1, new TeferisImp());
        Card discardedCard = new Island();
        harness.setHand(player1, List.of(new RealityRipple(), discardedCard));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, imp.getId());
        harness.passBothPriorities();
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(imp);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard);
    }
}
