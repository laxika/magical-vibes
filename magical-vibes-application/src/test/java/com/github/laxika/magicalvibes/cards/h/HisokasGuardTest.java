package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HisokasGuardTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives the target creature shroud")
    void resolvingGrantsShroud() {
        addReadyGuard(player1);
        Permanent bear = addReadyBear(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bear, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Shroud persists past end of turn while the Guard stays tapped")
    void shroudSurvivesEndOfTurnWhileTapped() {
        addReadyGuard(player1);
        Permanent bear = addReadyBear(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bear, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Shroud ends when the Guard becomes untapped")
    void shroudEndsWhenGuardUntaps() {
        Permanent guard = addReadyGuard(player1);
        Permanent bear = addReadyBear(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.SHROUD)).isTrue();

        advanceToNextTurnWithMayChoice(player2, true);
        assertThat(guard.isTapped()).isFalse();

        assertThat(gqs.hasKeyword(gd, bear, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud persists when the controller keeps the Guard tapped")
    void shroudPersistsWhenKeptTapped() {
        Permanent guard = addReadyGuard(player1);
        Permanent bear = addReadyBear(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        advanceToNextTurnWithMayChoice(player2, false);
        assertThat(guard.isTapped()).isTrue();

        assertThat(gqs.hasKeyword(gd, bear, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Shroud ends when the Guard leaves the battlefield")
    void shroudEndsWhenGuardRemoved() {
        Permanent guard = addReadyGuard(player1);
        Permanent bear = addReadyBear(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.SHROUD)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, guard));

        assertThat(gqs.hasKeyword(gd, bear, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Cannot target the Guard itself")
    void cannotTargetItself() {
        Permanent guard = addReadyGuard(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, guard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("other than this creature");
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        addReadyGuard(player1);
        Permanent enemyBear = addReadyBear(player2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, enemyBear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyGuard(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new HisokasGuard());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addReadyBear(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        perm.setSummoningSick(false);
        return perm;
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
