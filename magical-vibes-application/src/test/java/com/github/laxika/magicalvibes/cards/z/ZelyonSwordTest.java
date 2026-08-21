package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.i.IcatianPhalanx;
import com.github.laxika.magicalvibes.cards.i.ImplementsOfSacrifice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZelyonSword.class, IcatianPhalanx.class, ImplementsOfSacrifice.class})
class ZelyonSwordTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability gives the target creature +2/+0")
    void resolvingGrantsBoost() {
        addReadySword(player1);
        Permanent bear = addReadyBear(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost persists past end of turn while the artifact stays tapped")
    void boostSurvivesEndOfTurnWhileTapped() {
        addReadySword(player1);
        Permanent bear = addReadyBear(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost ends when the artifact becomes untapped")
    void boostEndsWhenSwordUntaps() {
        Permanent sword = addReadySword(player1);
        Permanent bear = addReadyBear(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);

        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(sword.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
    }

    @Test
    @DisplayName("The artifact may remain tapped during its controller's untap step")
    void swordCanRemainTapped() {
        Permanent sword = addReadySword(player1);
        Permanent bear = addReadyBear(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        advanceToNextTurnWithMayChoice(player2, false);

        assertThat(sword.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost ends when the artifact leaves the battlefield")
    void boostEndsWhenSwordLeavesBattlefield() {
        Permanent sword = addReadySword(player1);
        Permanent bear = addReadyBear(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, sword));

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost is not created if the artifact leaves before the ability resolves")
    void boostDoesNotApplyIfSwordLeavesBeforeResolution() {
        Permanent sword = addReadySword(player1);
        Permanent bear = addReadyBear(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, sword));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
    }

    @Test
    @DisplayName("The ability can target an opponent's creature")
    void canTargetOpponentsCreature() {
        addReadySword(player1);
        Permanent bear = addReadyBear(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
    }

    @Test
    @DisplayName("The ability cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addReadySword(player1);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ImplementsOfSacrifice());
        artifact.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addReadySword(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ZelyonSword());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addReadyBear(Player player) {
        return addCreatureReady(player, new IcatianPhalanx());
    }

    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        harness.forceActivePlayer(currentActivePlayer);
        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(newActivePlayer, TurnStep.UNTAP);
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
