package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlueDragon.class, HillGiantHerdgorger.class})
class BlueDragonTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives three distinct opposing creatures -3/-0, -2/-0, and -1/-0")
    void etbAppliesDifferentReductionsToThreeCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());

        castBlueDragon();
        chooseTarget(first);
        chooseTarget(second);
        chooseTarget(third);
        harness.passBothPriorities();

        assertThat((gqs.getEffectivePower(gd, first) - first.getCard().getPower())).isEqualTo(-3);
        assertThat((gqs.getEffectivePower(gd, second) - second.getCard().getPower())).isEqualTo(-2);
        assertThat((gqs.getEffectivePower(gd, third) - third.getCard().getPower())).isEqualTo(-1);
        assertThat(first.getToughnessModifier()).isZero();
        assertThat(second.getToughnessModifier()).isZero();
        assertThat(third.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The second and third reductions are optional")
    void optionalTargetsMayBeDeclined() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());

        castBlueDragon();
        chooseTarget(first);
        declineTarget();
        chooseTarget(second);
        harness.passBothPriorities();

        assertThat((gqs.getEffectivePower(gd, first) - first.getCard().getPower())).isEqualTo(-3);
        assertThat((gqs.getEffectivePower(gd, second) - second.getCard().getPower())).isEqualTo(-1);
    }

    @Test
    @DisplayName("Reductions last through cleanup and expire at the controller's next turn")
    void reductionsLastUntilControllersNextTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        harness.addToBattlefield(player2, new HillGiantHerdgorger());

        castBlueDragon();
        chooseTarget(target);
        declineTarget();
        declineTarget();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat((gqs.getEffectivePower(gd, target) - target.getCard().getPower())).isEqualTo(-3);

        gd.expireFloatingEffectsAtTurnStart(player1.getId());
        assertThat((gqs.getEffectivePower(gd, target) - target.getCard().getPower())).isZero();
    }

    @Test
    @DisplayName("The required -3/-0 target must be controlled by an opponent")
    void requiredTargetCannotBeOwnCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        harness.addToBattlefield(player2, new HillGiantHerdgorger());

        castBlueDragon();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(opposingCreature.getId())
                .doesNotContain(ownCreature.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        chooseTarget(opposingCreature);
        declineTarget();
        declineTarget();
        harness.passBothPriorities();
        assertThat((gqs.getEffectivePower(gd, opposingCreature) - opposingCreature.getCard().getPower())).isEqualTo(-3);
    }

    @Test
    @DisplayName("The optional -2/-0 target may be a creature you control")
    void secondTargetMayBeOwnCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());
        harness.addToBattlefield(player2, new HillGiantHerdgorger());

        castBlueDragon();
        chooseTarget(first);
        chooseTarget(ownCreature);
        declineTarget();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature) - ownCreature.getCard().getPower()).isEqualTo(-2);
        assertThat(gqs.getEffectivePower(gd, first) - first.getCard().getPower()).isEqualTo(-3);
    }

    @Test
    @DisplayName("The optional -1/-0 target may be Blue Dragon itself")
    void thirdTargetMayBeBlueDragonItself() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        harness.addToBattlefield(player2, new HillGiantHerdgorger());

        castBlueDragon();
        Permanent dragon = findPermanent(player1, "Blue Dragon");
        chooseTarget(first);
        chooseTarget(second);
        chooseTarget(dragon);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dragon) - dragon.getCard().getPower()).isEqualTo(-1);
        assertThat(gqs.getEffectivePower(gd, second) - second.getCard().getPower()).isEqualTo(-2);
    }

    @Test
    @DisplayName("Each selected creature must be distinct")
    void optionalTargetsCannotRepeatEarlierTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());

        castBlueDragon();
        chooseTarget(first);
        assertThatThrownBy(() -> chooseTarget(first)).isInstanceOf(IllegalStateException.class);
        chooseTarget(second);
        assertThatThrownBy(() -> chooseTarget(first)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> chooseTarget(second)).isInstanceOf(IllegalStateException.class);
        chooseTarget(third);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first) - first.getCard().getPower()).isEqualTo(-3);
        assertThat(gqs.getEffectivePower(gd, second) - second.getCard().getPower()).isEqualTo(-2);
        assertThat(gqs.getEffectivePower(gd, third) - third.getCard().getPower()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Without an opposing creature there is no legal required target")
    void noOpponentCreatureLeavesOwnCreatureUnaffected() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());

        castBlueDragon();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, ownCreature) - ownCreature.getCard().getPower()).isZero();
        harness.assertOnBattlefield(player1, "Blue Dragon");
    }

    private void castBlueDragon() {
        harness.castFromHand(player1, new BlueDragon(), "{5}{U}{U}");
        harness.passBothPriorities();
    }

    private void chooseTarget(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
    }

    private void declineTarget() {
        harness.handlePermanentChosen(player1, player1.getId());
    }
}
