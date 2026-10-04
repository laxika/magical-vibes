package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BloodMoon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SelesnyaGuildgate;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GuildSummit.class, SelesnyaGuildgate.class, Forest.class, BloodMoon.class})
class GuildSummitTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Gates on entry draws for each Gate tapped")
    void tapsGatesOnEntryAndDrawsForEach() {
        Permanent firstGate = harness.addToBattlefieldAndReturn(player1, new SelesnyaGuildgate());
        Permanent secondGate = harness.addToBattlefieldAndReturn(player1, new SelesnyaGuildgate());
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
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new SelesnyaGuildgate());
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
        harness.setHand(player1, List.of(new SelesnyaGuildgate()));

        harness.playLand(player1, 0);
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

        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only untapped Gates controlled by the Summit controller can be chosen")
    void onlyUntappedControlledGatesAreEligible() {
        Permanent eligible = harness.addToBattlefieldAndReturn(player1, new SelesnyaGuildgate());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player1, new SelesnyaGuildgate());
        Permanent tapped = harness.addToBattlefieldAndReturn(player1, new SelesnyaGuildgate());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new SelesnyaGuildgate());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        eligible.untap();
        unchosen.untap();
        tapped.tap();
        opposing.untap();
        forest.untap();

        castGuildSummit();
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(eligible.getId(), unchosen.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(eligible.getId()));

        assertThat(eligible.isTapped()).isTrue();
        assertThat(unchosen.isTapped()).isFalse();
        assertThat(tapped.isTapped()).isTrue();
        assertThat(opposing.isTapped()).isFalse();
        assertThat(forest.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Entry with no untapped Gates resolves without a choice or a draw")
    void noEligibleGatesOnEntry() {
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new SelesnyaGuildgate());
        gate.tap();

        castGuildSummit();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gate.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's Gate entering does not trigger Guild Summit")
    void opposingGateDoesNotTrigger() {
        harness.addToBattlefield(player1, new GuildSummit());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SelesnyaGuildgate()));

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each Guild Summit draws independently for a Gate entering tapped")
    void multipleSummitsDrawForTappedGate() {
        harness.addToBattlefield(player1, new GuildSummit());
        harness.addToBattlefield(player1, new GuildSummit());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SelesnyaGuildgate()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({GuildSummit.class, SelesnyaGuildgate.class, BloodMoon.class})
    @DisplayName("A Guildgate entering as a Mountain under Blood Moon does not trigger Guild Summit")
    void gateSubtypeRemovedBeforeEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new GuildSummit());
        harness.addToBattlefield(player2, new BloodMoon());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SelesnyaGuildgate()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void castGuildSummit() {
        harness.castFromHand(player1, new GuildSummit(), "{2}{U}");
    }
}
