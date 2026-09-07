package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlueDragon.class, GrizzlyBears.class})
class BlueDragonTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives three distinct opposing creatures -3/-0, -2/-0, and -1/-0")
    void etbAppliesDifferentReductionsToThreeCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

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
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castBlueDragon();
        chooseTarget(target);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat((gqs.getEffectivePower(gd, target) - target.getCard().getPower())).isEqualTo(-3);

        gd.expireFloatingEffectsAtTurnStart(player1.getId());
        assertThat((gqs.getEffectivePower(gd, target) - target.getCard().getPower())).isZero();
    }

    @Test
    @DisplayName("The ETB can target only creatures controlled by an opponent")
    void etbCannotTargetOwnCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castBlueDragon();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(opposingCreature.getId())
                .doesNotContain(ownCreature.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        chooseTarget(opposingCreature);
        harness.passBothPriorities();
        assertThat((gqs.getEffectivePower(gd, opposingCreature) - opposingCreature.getCard().getPower())).isEqualTo(-3);
    }

    private void castBlueDragon() {
        harness.setHand(player1, List.of(new BlueDragon()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void chooseTarget(Permanent target) {
        harness.handlePermanentChosen(player1, target.getId());
    }

    private void declineTarget() {
        harness.handlePermanentChosen(player1, player1.getId());
    }
}
