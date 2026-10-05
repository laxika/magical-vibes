package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.b.BloodscaleProwler;
import com.github.laxika.magicalvibes.cards.c.CullingSun;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Quicken.class, BloodscaleProwler.class, CullingSun.class})
class QuickenTest extends BaseCardTest {

    private void resolveQuicken() {
        harness.setLibrary(player1, List.of(new BloodscaleProwler()));
        harness.castFromHand(player1, new Quicken(), "{U}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Draws a card on resolution")
    void drawsACard() {
        resolveQuicken();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Bloodscale Prowler");
    }

    @Test
    @DisplayName("The next sorcery can be cast at instant speed")
    void grantsFlashToNextSorcery() {
        resolveQuicken();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new CullingSun(), "{2}{W}{W}{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Culling Sun");
    }

    @Test
    @DisplayName("Only the first sorcery gains flash")
    void grantIsConsumedByTheFirstSorcery() {
        resolveQuicken();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setLibrary(player1, List.of(new BloodscaleProwler(), new BloodscaleProwler()));

        harness.castFromHand(player1, new CullingSun(), "{2}{W}{W}{B}");
        harness.passBothPriorities();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromHand(player1, new CullingSun(), "{2}{W}{W}{B}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Creature spells are not granted flash")
    void doesNotGrantFlashToOtherTypes() {
        resolveQuicken();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromHand(player1, new BloodscaleProwler(), "{2}{R}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.castFromHand(player1, new CullingSun(), "{2}{W}{W}{B}");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Culling Sun");
    }

    @Test
    @DisplayName("Flash permission wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        resolveQuicken();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromHand(player1, new CullingSun(), "{2}{W}{W}{B}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Multiple Quickens all apply to the same next sorcery")
    void multipleGrantsAreConsumedTogether() {
        resolveQuicken();
        resolveQuicken();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new CullingSun(), "{2}{W}{W}{B}");
        assertThat(gd.stack).hasSize(1);

        assertThatThrownBy(() -> harness.castFromHand(player1, new CullingSun(), "{2}{W}{W}{B}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Casting a sorcery at normal timing still consumes the permission")
    void normalSorceryCastConsumesGrant() {
        resolveQuicken();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new CullingSun(), "{2}{W}{W}{B}");
        assertThat(gd.stack).hasSize(1);

        assertThatThrownBy(() -> harness.castFromHand(player1, new CullingSun(), "{2}{W}{W}{B}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Permission works on the opponent's turn and belongs only to the controller")
    void worksOnOpponentsTurnOnlyForController() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        resolveQuicken();

        assertThatThrownBy(() -> harness.castFromHand(player2, new CullingSun(), "{2}{W}{W}{B}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.castFromHand(player1, new CullingSun(), "{2}{W}{W}{B}");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
    }
}
