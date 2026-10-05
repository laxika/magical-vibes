package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FirstLittlePig;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PathsOfTuinvale.class, GrizzlyBears.class, FirstLittlePig.class})
class PathsOfTuinvaleTest extends BaseCardTest {

    @Test
    void returnsOwnCreatureAndReducesItsFutureCastCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castPaths(player1, 0, target);

        harness.setHand(player1, List.of(target.getCard()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getCard().getId()));
    }

    @Test
    void returnsOpposingCreatureAndIncreasesItsFutureCastCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castPaths(player1, 1, target);

        harness.setHand(player2, List.of(target.getCard()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getCard().getId()));
    }

    @Test
    void canChooseBothModes() {
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PathsOfTuinvale()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(ownTarget.getId(), opposingTarget.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(ownTarget.getCard());
        assertThat(gd.playerHands.get(player2.getId())).contains(opposingTarget.getCard());
    }

    @Test
    void eachModeRequiresTheMatchingController() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PathsOfTuinvale()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(opposingCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{1}, List.of(ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothModesApplyTheirCostChangesToTheCorrectCards() {
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new FirstLittlePig());
        Permanent opposingTarget = harness.addToBattlefieldAndReturn(player2, new FirstLittlePig());
        harness.setHand(player1, List.of(new PathsOfTuinvale()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(ownTarget.getId(), opposingTarget.getId()));
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(ownTarget.getCard().getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(opposingTarget.getCard().getId()));
    }

    @Test
    void reductionDoesNotApplyToAnotherCardWithTheSameName() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FirstLittlePig());
        castPaths(player1, 0, target);

        harness.setHand(player1, List.of(new FirstLittlePig(), target.getCard()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.castCreature(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getCard().getId()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void repeatedIncreasesAccumulateAfterRecastingTheCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FirstLittlePig());
        castPaths(player1, 1, target);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent recastTarget = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(target.getCard().getId()))
                .findFirst().orElseThrow();
        castPaths(player1, 1, recastTarget);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getCard().getId()));
    }

    @Test
    void remainingModeResolvesWhenTheOtherTargetLeavesTheBattlefield() {
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new FirstLittlePig());
        Permanent opposingTarget = harness.addToBattlefieldAndReturn(player2, new FirstLittlePig());
        harness.setHand(player1, List.of(new PathsOfTuinvale()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(ownTarget.getId(), opposingTarget.getId()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, ownTarget));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(ownTarget.getCard());
        assertThat(gd.playerHands.get(player2.getId())).contains(opposingTarget.getCard());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castPaths(Player caster, int mode, Permanent target) {
        harness.setHand(caster, List.of(new PathsOfTuinvale()));
        harness.addMana(caster, ManaColor.BLUE, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.castModalInstantWithModes(caster, 0, 1, 2, new int[]{mode}, List.of(target.getId()));
        harness.passBothPriorities();
    }
}
