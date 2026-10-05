package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.SpiketailHatchling;
import com.github.laxika.magicalvibes.cards.w.WintermoonMesa;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ManaVapors.class, WintermoonMesa.class, SpiketailHatchling.class})
class ManaVaporsTest extends BaseCardTest {

    @Test
    @DisplayName("Lands target player controls do not untap during their next untap step")
    void landsDoNotUntapDuringNextUntapStep() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new WintermoonMesa());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SpiketailHatchling());
        land.tap();
        creature.tap();

        castAndResolve(player2.getId());
        advanceToNextTurn(player1);

        assertThat(land.isTapped()).isTrue();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Also affects lands that enter before the target's next untap step")
    void landsEnteringBeforeNextUntapStepDoNotUntap() {
        castAndResolve(player2.getId());

        Permanent land = harness.addToBattlefieldAndReturn(player2, new WintermoonMesa());
        land.tap();

        advanceToNextTurn(player1);

        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Lands untap normally on the following turn")
    void landsUntapOnFollowingTurn() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new WintermoonMesa());
        land.tap();

        castAndResolve(player2.getId());
        advanceToNextTurn(player1);
        advanceToNextTurn(player2);
        advanceToNextTurn(player1);

        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not affect lands controlled by another player")
    void doesNotAffectOtherPlayersLands() {
        Permanent casterLand = harness.addToBattlefieldAndReturn(player1, new WintermoonMesa());
        Permanent targetLand = harness.addToBattlefieldAndReturn(player2, new WintermoonMesa());
        casterLand.tap();
        targetLand.tap();

        castAndResolve(player2.getId());
        advanceToNextTurn(player1);
        advanceToNextTurn(player2);

        assertThat(casterLand.isTapped()).isFalse();
        assertThat(targetLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target the caster and waits for the caster's next untap step")
    void canTargetCaster() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new WintermoonMesa());
        land.tap();

        castAndResolve(player1.getId());
        advanceToNextTurn(player1);
        advanceToNextTurn(player2);

        assertThat(land.isTapped()).isTrue();

        advanceToNextTurn(player1);
        advanceToNextTurn(player2);

        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Multiple copies prevent untapping only during the same next untap step")
    void multipleCopiesDoNotExtendRestriction() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new WintermoonMesa());
        land.tap();

        castAndResolve(player2.getId());
        castAndResolve(player2.getId());
        advanceToNextTurn(player1);

        assertThat(land.isTapped()).isTrue();

        advanceToNextTurn(player2);
        advanceToNextTurn(player1);

        assertThat(land.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not tap untapped lands and expires even when no land needs to untap")
    void restrictionExpiresWithUntappedLands() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new WintermoonMesa());
        land.untap();

        castAndResolve(player2.getId());

        assertThat(land.isTapped()).isFalse();

        advanceToNextTurn(player1);

        assertThat(land.isTapped()).isFalse();

        land.tap();
        advanceToNextTurn(player2);
        advanceToNextTurn(player1);

        assertThat(land.isTapped()).isFalse();
    }

    private void castAndResolve(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new ManaVapors()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveSorcery(player1, 0, targetPlayerId);
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        Player nextActivePlayer = currentActivePlayer.getId().equals(player1.getId()) ? player2 : player1;
        harness.passUntil(nextActivePlayer, TurnStep.UNTAP);
    }
}
