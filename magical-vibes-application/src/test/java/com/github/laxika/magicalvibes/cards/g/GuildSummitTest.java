package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RakdosGuildgate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GuildSummitTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Gates on entry draws for each Gate tapped")
    void tapsGatesOnEntryAndDrawsForEach() {
        Permanent firstGate = harness.addToBattlefieldAndReturn(player1, new RakdosGuildgate());
        Permanent secondGate = harness.addToBattlefieldAndReturn(player1, new RakdosGuildgate());
        firstGate.untap();
        secondGate.untap();

        castGuildSummit();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();
        harness.handleMultiplePermanentsChosen(player1, List.of(firstGate.getId(), secondGate.getId()));

        assertThat(firstGate.isTapped()).isTrue();
        assertThat(secondGate.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Choosing no Gates taps nothing and draws nothing")
    void choosesNoGates() {
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new RakdosGuildgate());
        gate.untap();

        castGuildSummit();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gate.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A Gate entering under the controller's control draws a card")
    void drawsWhenGateEnters() {
        harness.addToBattlefield(player1, new GuildSummit());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new RakdosGuildgate()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A non-Gate land entering does not draw a card")
    void doesNotDrawWhenNonGateLandEnters() {
        harness.addToBattlefield(player1, new GuildSummit());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Forest()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void castGuildSummit() {
        harness.setHand(player1, List.of(new GuildSummit()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
    }
}
