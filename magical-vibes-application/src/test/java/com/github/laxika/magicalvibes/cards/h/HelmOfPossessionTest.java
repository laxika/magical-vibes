package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HelmOfPossessionTest extends BaseCardTest {

    @Test
    @DisplayName("{2}, {T}, Sacrifice a creature: gains control of target creature and stays tapped")
    void gainsControlWhileTapped() {
        Permanent helm = addHelm(player1);
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        Permanent stolen = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        activateHelm(helm, stolen);

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(stolen.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(stolen.getId()));
        assertThat(helm.isTapped()).isTrue();
        harness.assertInGraveyard(player1, fodder.getCard().getName());
        assertThat(gd.newestControlEffectFor(stolen.getId())).isNotNull();
        assertThat(gd.newestControlEffectFor(stolen.getId()).sourcePermanentId()).isEqualTo(helm.getId());
    }

    @Test
    @DisplayName("Control is lost when the Helm untaps during its controller's untap step")
    void controlLostWhenHelmUntaps() {
        Permanent helm = addHelm(player1);
        addCreatureReady(player1, new GrizzlyBears());
        Permanent stolen = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        activateHelm(helm, stolen);

        // player2's turn: the Helm stays tapped, so control is retained.
        advanceToNextTurn(player1);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(stolen.getId()));

        // player1's untap step: choose to untap the Helm. Control ends immediately.
        advanceToNextTurnWithMayChoice(player2, true);
        assertThat(helm.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(stolen.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(stolen.getId()));
        assertThat(gd.controlEffectsFor(stolen.getId())).isEmpty();
    }

    @Test
    @DisplayName("Choosing not to untap the Helm retains control across the untap step")
    void keepingTappedRetainsControl() {
        Permanent helm = addHelm(player1);
        addCreatureReady(player1, new GrizzlyBears());
        Permanent stolen = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        activateHelm(helm, stolen);

        advanceToNextTurn(player1);
        advanceToNextTurnWithMayChoice(player2, false);

        assertThat(helm.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(stolen.getId()));
    }

    @Test
    @DisplayName("Control is lost when the Helm leaves the battlefield")
    void controlLostWhenHelmLeaves() {
        Permanent helm = addHelm(player1);
        addCreatureReady(player1, new GrizzlyBears());
        Permanent stolen = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        activateHelm(helm, stolen);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(stolen.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(helm);
        advanceToNextTurn(player1);

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(stolen.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(stolen.getId()));
    }

    private Permanent addHelm(Player player) {
        Permanent perm = new Permanent(new HelmOfPossession());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void activateHelm(Permanent helm, Permanent target) {
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(helm);
        harness.activateAbility(player1, idx, null, target.getId());
        harness.passBothPriorities();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn
    }

    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
