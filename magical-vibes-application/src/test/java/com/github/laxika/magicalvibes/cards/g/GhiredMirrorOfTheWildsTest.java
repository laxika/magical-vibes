package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhiredMirrorOfTheWilds.class, RaiseTheAlarm.class})
class GhiredMirrorOfTheWildsTest extends BaseCardTest {

    @Test
    @DisplayName("A nontoken creature copies a token that entered this turn")
    void createsTokenCopyOfTokenEnteredThisTurn() {
        Permanent ghired = addReadyGhired();
        List<Permanent> soldiers = createSoldiers();

        harness.activateAbility(player1, permanentIndex(ghired), null, soldiers.getFirst().getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(3);
    }

    @Test
    @DisplayName("A token from an earlier turn is not a legal target")
    void cannotTargetTokenThatEnteredEarlier() {
        Permanent ghired = addReadyGhired();
        List<Permanent> soldiers = createSoldiers();
        endTurn(player1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, permanentIndex(ghired), null, soldiers.getFirst().getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyGhired() {
        Permanent ghired = harness.addToBattlefieldAndReturn(player1, new GhiredMirrorOfTheWilds());
        ghired.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return ghired;
    }

    private List<Permanent> createSoldiers() {
        harness.setHand(player1, List.of(new RaiseTheAlarm()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }

    private void endTurn(Player activePlayer) {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
    }

    private int permanentIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
