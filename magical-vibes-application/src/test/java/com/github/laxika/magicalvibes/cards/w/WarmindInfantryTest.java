package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RapidHybridization;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarmindInfantry.class, GrizzlyBears.class, RapidHybridization.class})
class WarmindInfantryTest extends BaseCardTest {

    @Test
    @DisplayName("Battalion gives this creature +2/+0 when it attacks with two other creatures")
    void battalionBoostsSelf() {
        Permanent infantry = addCreatureReady(player1, new WarmindInfantry());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(infantry.getEffectivePower()).isEqualTo(4);
        assertThat(infantry.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Battalion does not trigger with only one other attacker")
    void battalionDoesNotTriggerWithOneOtherAttacker() {
        Permanent infantry = addCreatureReady(player1, new WarmindInfantry());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(infantry.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Battalion boost wears off at end of turn")
    void battalionBoostWearsOff() {
        Permanent infantry = addCreatureReady(player1, new WarmindInfantry());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();
        assertThat(infantry.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(infantry.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking alone does not trigger battalion even with other creatures on the battlefield")
    void attackingAloneDoesNotTriggerBattalion() {
        Permanent infantry = addCreatureReady(player1, new WarmindInfantry());
        addCreatureReady(player1, new WarmindInfantry());
        addCreatureReady(player1, new WarmindInfantry());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).isEmpty();
        assertThat(infantry.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("A nonattacking infantry is not boosted when three other creatures attack")
    void nonattackingInfantryIsNotBoosted() {
        Permanent infantry = addCreatureReady(player1, new WarmindInfantry());
        Permanent firstAttacker = addCreatureReady(player1, new WarmindInfantry());
        Permanent secondAttacker = addCreatureReady(player1, new WarmindInfantry());
        Permanent thirdAttacker = addCreatureReady(player1, new WarmindInfantry());

        declareAttackers(player1, List.of(1, 2, 3));
        resolveAllTriggers();

        assertThat(infantry.getEffectivePower()).isEqualTo(2);
        assertThat(firstAttacker.getEffectivePower()).isEqualTo(4);
        assertThat(secondAttacker.getEffectivePower()).isEqualTo(4);
        assertThat(thirdAttacker.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("Battalion still resolves after another attacker leaves the battlefield")
    void battalionDoesNotRecheckAttackerCountOnResolution() {
        Permanent infantry = addCreatureReady(player1, new WarmindInfantry());
        Permanent otherAttacker = addCreatureReady(player1, new WarmindInfantry());
        addCreatureReady(player1, new WarmindInfantry());
        harness.setHand(player2, List.of(new RapidHybridization()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        declareAttackers(player1, List.of(0, 1, 2));
        assertThat(gd.stack).isNotEmpty();
        assertThat(infantry.getEffectivePower()).isEqualTo(2);
        harness.castAndResolveInstant(player2, 0, otherAttacker.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(otherAttacker);
        resolveAllTriggers();

        assertThat(infantry.getEffectivePower()).isEqualTo(4);
        assertThat(infantry.getEffectiveToughness()).isEqualTo(3);
    }
}
