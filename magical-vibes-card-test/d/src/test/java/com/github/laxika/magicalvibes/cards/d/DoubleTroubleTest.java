package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoubleTrouble.class, GrizzlyBears.class, GiantGrowth.class})
class DoubleTroubleTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles the power of your creatures without changing toughness")
    void doublesOwnCreaturePowerOnly() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castDoubleTrouble();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Doubles each creature's current power")
    void doublesCurrentPower() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, ownCreature.getId());
        harness.passBothPriorities();

        castDoubleTrouble();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(5);
    }

    @Test
    @DisplayName("The power doubling wears off at end of turn")
    void powerDoublingWearsOffAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castDoubleTrouble();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
    }

    private void castDoubleTrouble() {
        harness.castFromHand(player1, new DoubleTrouble(), "{4}{R}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Doubles every creature controlled when the spell resolves")
    void doublesMultipleCreaturesAtResolution() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new DoubleTrouble(), "{4}{R}");
        Permanent second = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering after resolution do not receive the bonus")
    void doesNotAffectLaterCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castDoubleTrouble();

        Permanent later = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(2);
    }

    @Test
    @DisplayName("A second resolution doubles the already modified power")
    void repeatedDoublingUsesCurrentPower() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castDoubleTrouble();
        castDoubleTrouble();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("A later power boost is not itself doubled")
    void doublingBonusIsFixedAtResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castDoubleTrouble();

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Resolves without creatures or targets")
    void resolvesWithNoCreatures() {
        castDoubleTrouble();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Double Trouble");
    }
}
