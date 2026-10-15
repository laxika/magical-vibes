package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EagleOfTheGreatShelf.class, GrizzlyBears.class})
class EagleOfTheGreatShelfTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each other creature you control when it attacks")
    void boostsForOtherCreaturesYouControl() {
        Permanent eagle = addCreatureReady(player1, new EagleOfTheGreatShelf());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, eagle)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, eagle)).isEqualTo(7);
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent eagle = addCreatureReady(player1, new EagleOfTheGreatShelf());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, eagle)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, eagle)).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, eagle)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, eagle)).isEqualTo(5);
    }

    @Test
    @DisplayName("Attacking without other creatures gives no boost")
    void attackingAloneDoesNotCountItselfOrOpposingCreatures() {
        Permanent eagle = addCreatureReady(player1, new EagleOfTheGreatShelf());
        addCreatureReady(player2, new EagleOfTheGreatShelf());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        assertThat(gqs.getEffectivePower(gd, eagle)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, eagle)).isEqualTo(5);
    }

    @Test
    @DisplayName("Counts creatures that arrive before the attack trigger resolves")
    void countsCreaturesAtResolutionAndDoesNotBoostNonattackers() {
        Permanent eagle = addCreatureReady(player1, new EagleOfTheGreatShelf());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        Permanent other = addCreatureReady(player1, new EagleOfTheGreatShelf());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(gqs.getEffectivePower(gd, eagle)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, eagle)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId()).remove(other);
        assertThat(gqs.getEffectivePower(gd, eagle)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, eagle)).isEqualTo(6);
    }

    @Test
    @DisplayName("Does not count creatures that leave before the attack trigger resolves")
    void excludesCreaturesThatLeftBeforeResolution() {
        Permanent eagle = addCreatureReady(player1, new EagleOfTheGreatShelf());
        Permanent other = addCreatureReady(player1, new EagleOfTheGreatShelf());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(other);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(gqs.getEffectivePower(gd, eagle)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, eagle)).isEqualTo(5);
    }
}
