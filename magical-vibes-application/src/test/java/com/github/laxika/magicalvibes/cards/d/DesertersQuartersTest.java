package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.m.ManaConfluence;
import com.github.laxika.magicalvibes.cards.g.GoldenHind;
import com.github.laxika.magicalvibes.cards.c.ColossalHeroics;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DesertersQuarters.class, GoldenHind.class, ManaConfluence.class, ColossalHeroics.class})
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
        addReadyDesertersQuarters(player1);
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

    @Test
    void alreadyTappedCreatureIsStillLocked() {
        addReadyDesertersQuarters(player1);
        Permanent target = addReadyCreature(player2);
        target.tap();
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.performUntapStep(player2);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWithLessThanSixMana() {
        Permanent source = addReadyDesertersQuarters(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(source.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void decliningSourceUntapKeepsCreatureLockedAcrossTurns() {
        Permanent source = addReadyDesertersQuarters(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        advanceToNextTurn(player1);
        advanceToNextTurnWithMayChoice(player2, false);
        advanceToNextTurn(player1);

        assertThat(source.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void creatureCanUntapBySpellButRemainsLockedDuringItsUntapStep() {
        Permanent source = addReadyDesertersQuarters(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new ColossalHeroics()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.isTapped()).isFalse();
        assertThat(source.isTapped()).isTrue();
        target.tap();
        harness.performUntapStep(player2);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void sourceUntappedBeforeResolutionStillTapsCreatureWithoutLockingIt() {
        Permanent source = addReadyDesertersQuarters(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.activateAbility(player1, 0, null, target.getId());
        source.untap();

        harness.passBothPriorities();
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void sourceUntappedAndRetappedBeforeResolutionDoesNotCreateLock() {
        Permanent source = addReadyDesertersQuarters(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.activateAbility(player1, 0, null, target.getId());
        source.untap();
        source.tap();

        harness.passBothPriorities();
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);

        assertThat(source.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void ownCreatureStaysTappedDuringTheStepInWhichSourceUntaps() {
        Permanent source = addReadyDesertersQuarters(player1);
        Permanent target = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(source.isTapped()).isFalse();
        assertThat(target.isTapped()).isTrue();
        advanceToNextTurn(player1);
        advanceToNextTurn(player2);

        assertThat(target.isTapped()).isFalse();
    }

    private Permanent addReadyDesertersQuarters(Player player) {
        return addCreatureReady(player, new DesertersQuarters());
    }

    private Permanent addReadyCreature(Player player) {
        return addCreatureReady(player, new GoldenHind());
    }

    private Permanent addReadyLand(Player player) {
        return addCreatureReady(player, new ManaConfluence());
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
