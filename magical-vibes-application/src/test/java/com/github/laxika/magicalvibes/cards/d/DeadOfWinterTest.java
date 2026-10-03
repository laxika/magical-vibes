package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcehideGolem;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredSwamp;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeadOfWinter.class, GrizzlyBears.class, IcehideGolem.class, SnowCoveredSwamp.class})
class DeadOfWinterTest extends BaseCardTest {

    @Test
    @DisplayName("Gives nonsnow creatures -X/-X based on the snow permanents its controller controls")
    void givesNonsnowCreaturesMinusX() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new SnowCoveredSwamp());
        harness.addToBattlefield(player2, new SnowCoveredSwamp());
        Permanent snowCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        TestCards.mutableCard(snowCreature).setSupertypes(EnumSet.of(CardSupertype.SNOW));

        castDeadOfWinter();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(1);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(1);
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(1);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(1);
        assertThat(snowCreature.getEffectivePower()).isEqualTo(2);
        assertThat(snowCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts multiple snow permanents and kills creatures with zero toughness")
    void countsMultipleSnowPermanents() {
        harness.addToBattlefield(player1, new SnowCoveredSwamp());
        harness.addToBattlefield(player1, new SnowCoveredSwamp());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castDeadOfWinter();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The temporary debuff wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SnowCoveredSwamp());

        castDeadOfWinter();
        assertThat(creature.getEffectivePower()).isEqualTo(1);
        assertThat(creature.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's snow permanents do not cause a debuff when the controller has none")
    void zeroSnowPermanentsDoesNotDebuffCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new SnowCoveredSwamp());

        castDeadOfWinter();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Snow artifact creatures count once toward X and are themselves unaffected")
    void countsSnowCreatureWithoutDebuffingIt() {
        Permanent snowCreature = harness.addToBattlefieldAndReturn(player1, new IcehideGolem());
        Permanent nonsnowCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castDeadOfWinter();

        assertThat(nonsnowCreature.getEffectivePower()).isEqualTo(1);
        assertThat(nonsnowCreature.getEffectiveToughness()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Icehide Golem");
        assertThat(snowCreature.getEffectivePower()).isEqualTo(2);
        assertThat(snowCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts snow permanents at resolution rather than when cast")
    void countsSnowPermanentsAtResolution() {
        harness.addToBattlefield(player1, new SnowCoveredSwamp());
        harness.addToBattlefield(player2, new GrizzlyBears());

        putDeadOfWinterOnStack();
        harness.addToBattlefield(player1, new IcehideGolem());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Icehide Golem");
    }

    @Test
    @DisplayName("Later snow permanents and creatures do not change the resolved debuff")
    void locksAmountAndAffectedCreaturesAtResolution() {
        Permanent originalCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new SnowCoveredSwamp());

        castDeadOfWinter();
        harness.enterBattlefieldAndReturn(player1, new IcehideGolem());
        Permanent laterCreature = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, originalCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, originalCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private void castDeadOfWinter() {
        putDeadOfWinterOnStack();
        harness.passBothPriorities();
    }

    private void putDeadOfWinterOnStack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new DeadOfWinter(), "{2}{B}");
    }
}
