package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThoughtcutterAgent.class, Forest.class, GrizzlyBears.class})
class ThoughtcutterAgentTest extends BaseCardTest {

    private void readyAgent() {
        addCreatureReady(player1, new ThoughtcutterAgent());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Target player loses 1 life; their hand is not disturbed")
    void targetLosesLifeHandUntouched() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new GrizzlyBears())));
        readyAgent();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
        // Reveal leaves the hand in place.
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target its own controller")
    void canTargetSelf() {
        harness.setHand(player1, new ArrayList<>(List.of(new Forest())));
        readyAgent();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Resolves with an empty hand; target still loses 1 life")
    void emptyHandStillLosesLife() {
        harness.setHand(player2, new ArrayList<>(List.of()));
        readyAgent();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }
    @Test
    @DisplayName("Reveals the targeted hand to both players only on resolution")
    void revealsHandToBothPlayersOnResolution() {
        ThoughtcutterAgent cardInHand = new ThoughtcutterAgent();
        harness.setHand(player2, List.of(cardInHand));
        readyAgent();
        harness.clearMessages();

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(harness.getConn1().getSentMessages()).noneMatch(msg -> msg.contains("REVEAL_HAND"));
        assertThat(harness.getConn2().getSentMessages()).noneMatch(msg -> msg.contains("REVEAL_HAND"));
        harness.passBothPriorities();

        assertThat(harness.getConn1().getSentMessages()).anyMatch(msg ->
                msg.contains("REVEAL_HAND") && msg.contains("Thoughtcutter Agent"));
        assertThat(harness.getConn2().getSentMessages()).anyMatch(msg ->
                msg.contains("REVEAL_HAND") && msg.contains("Thoughtcutter Agent"));
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(cardInHand);
    }

    @Test
    @DisplayName("Activation pays mana and taps the agent before resolving")
    void paysManaAndTapCostImmediately() {
        readyAgent();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        harness.passBothPriorities();
        harness.assertLife(player2, lifeBefore - 1);
    }

    @Test
    @DisplayName("A summoning-sick agent cannot pay the tap cost")
    void summoningSicknessPreventsActivation() {
        readyAgent();
        gd.playerBattlefields.get(player1.getId()).getFirst().setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isFalse();
    }

    @Test
    @DisplayName("Activated ability resolves after its source leaves the battlefield")
    void resolvesWithoutSource() {
        harness.setHand(player2, List.of(new ThoughtcutterAgent()));
        readyAgent();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.clearMessages();

        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 1);
        assertThat(harness.getConn1().getSentMessages()).anyMatch(msg ->
                msg.contains("REVEAL_HAND") && msg.contains("Thoughtcutter Agent"));
    }
}
