package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DesertersQuartersTest extends BaseCardTest {

    @Test
    void resolvingAbilityTapsTargetCreature() {
        addReadyDesertersQuarters(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void activatingAbilityTapsDesertersQuarters() {
        Permanent source = addReadyDesertersQuarters(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(source.isTapped()).isTrue();
    }

    @Test
    void cannotTargetNonCreature() {
        addReadyDesertersQuarters(player1);
        Permanent land = addReadyLand(player2);
        harness.addMana(player1, ManaColor.WHITE, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void lockedCreatureDoesNotUntapWhileSourceIsTapped() {
        addReadyDesertersQuarters(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        advanceToNextTurn(player1);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void lockedCreatureUntapsAfterSourceUntaps() {
        Permanent source = addReadyDesertersQuarters(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        advanceToNextTurn(player1);

        advanceToNextTurnWithMayChoice(player2, true);
        advanceToNextTurn(player1);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void choosingNotToUntapSourceKeepsItTapped() {
        Permanent source = addReadyDesertersQuarters(player1);
        source.tap();

        advanceToNextTurnWithMayChoice(player2, false);

        assertThat(source.isTapped()).isTrue();
    }

    private Permanent addReadyDesertersQuarters(Player player) {
        return addReady(player, new Permanent(new DesertersQuarters()));
    }

    private Permanent addReadyCreature(Player player) {
        return addReady(player, new Permanent(new GrizzlyBears()));
    }

    private Permanent addReadyLand(Player player) {
        return addReady(player, new Permanent(new Forest()));
    }

    private Permanent addReady(Player player, Permanent permanent) {
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

    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean untapSource) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.handleMayAbilityChosen(newActivePlayer, untapSource);
    }
}
