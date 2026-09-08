package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ManaLeechTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability taps the target land")
    void resolvingAbilityTapsTargetLand() {
        addReadyManaLeech(player1);
        Permanent targetLand = addReadyLand(player2);

        harness.activateAbility(player1, 0, null, targetLand.getId());
        harness.passBothPriorities();

        assertThat(targetLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating the ability taps Mana Leech")
    void activatingAbilityTapsManaLeech() {
        Permanent manaLeech = addReadyManaLeech(player1);
        Permanent targetLand = addReadyLand(player2);

        harness.activateAbility(player1, 0, null, targetLand.getId());

        assertThat(manaLeech.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target a non-land permanent")
    void cannotTargetNonLand() {
        addReadyManaLeech(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("The target land remains tapped while Mana Leech remains tapped")
    void targetLandDoesNotUntapWhileManaLeechRemainsTapped() {
        Permanent manaLeech = addReadyManaLeech(player1);
        Permanent targetLand = addReadyLand(player2);

        harness.activateAbility(player1, 0, null, targetLand.getId());
        harness.passBothPriorities();

        advanceToNextTurn(player1);

        assertThat(manaLeech.isTapped()).isTrue();
        assertThat(targetLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The target land untaps after Mana Leech untaps")
    void targetLandUntapsAfterManaLeechUntaps() {
        Permanent manaLeech = addReadyManaLeech(player1);
        Permanent targetLand = addReadyLand(player2);

        harness.activateAbility(player1, 0, null, targetLand.getId());
        harness.passBothPriorities();
        advanceToNextTurnWithMayChoice(player2, true);
        advanceToNextTurn(player1);

        assertThat(manaLeech.isTapped()).isFalse();
        assertThat(targetLand.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The controller may choose not to untap Mana Leech")
    void mayChooseNotToUntap() {
        Permanent manaLeech = addReadyManaLeech(player1);
        manaLeech.tap();

        advanceToNextTurnWithMayChoice(player2, false);

        assertThat(manaLeech.isTapped()).isTrue();
    }

    private Permanent addReadyManaLeech(Player player) {
        Permanent permanent = new Permanent(new ManaLeech());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyLand(Player player) {
        Permanent permanent = new Permanent(new Forest());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
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
