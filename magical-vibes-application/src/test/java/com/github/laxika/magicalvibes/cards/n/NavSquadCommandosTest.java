package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NavSquadCommandos.class, GrizzlyBears.class})
class NavSquadCommandosTest extends BaseCardTest {

    @Test
    @DisplayName("Battalion gives Nav Squad Commandos +1/+1 and untaps it")
    void battalionBoostsAndUntapsSource() {
        Permanent commandos = addCreatureReady(player1, new NavSquadCommandos());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        assertThat(commandos.isTapped()).isTrue();

        resolveAllTriggers();

        assertThat(commandos.getPowerModifier()).isEqualTo(1);
        assertThat(commandos.getToughnessModifier()).isEqualTo(1);
        assertThat(commandos.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Battalion does not trigger without two other attacking creatures")
    void battalionDoesNotTriggerWithFewerThanTwoOtherAttackers() {
        Permanent commandos = addCreatureReady(player1, new NavSquadCommandos());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(commandos.getPowerModifier()).isZero();
        assertThat(commandos.getToughnessModifier()).isZero();
        assertThat(commandos.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Non-attacking creatures do not count toward battalion")
    void nonAttackingCreaturesDoNotCount() {
        Permanent commandos = addCreatureReady(player1, new NavSquadCommandos());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(commandos.getPowerModifier()).isZero();
        assertThat(commandos.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Battalion still resolves when another creature stops attacking")
    void battalionUsesTheDeclaredAttackerCount() {
        Permanent commandos = addCreatureReady(player1, new NavSquadCommandos());
        Permanent otherAttacker = addCreatureReady(player1, new NavSquadCommandos());
        addCreatureReady(player1, new NavSquadCommandos());

        declareAttackers(List.of(0, 1, 2));
        assertThat(gd.stack).isNotEmpty();
        otherAttacker.setAttacking(false);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, this::resolveAllTriggers);

        assertThat(commandos.getPowerModifier()).isEqualTo(1);
        assertThat(commandos.getToughnessModifier()).isEqualTo(1);
        assertThat(commandos.isTapped()).isFalse();
        assertThat(commandos.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Battalion does not boost or untap a source that did not attack")
    void sourceMustAttackToTriggerBattalion() {
        Permanent commandos = addCreatureReady(player1, new NavSquadCommandos());
        commandos.setTapped(true);
        addCreatureReady(player1, new NavSquadCommandos());
        addCreatureReady(player1, new NavSquadCommandos());
        addCreatureReady(player1, new NavSquadCommandos());

        declareAttackers(List.of(1, 2, 3));
        resolveAllTriggers();

        assertThat(commandos.getPowerModifier()).isZero();
        assertThat(commandos.getToughnessModifier()).isZero();
        assertThat(commandos.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Battalion boost expires at end of turn")
    void battalionBoostExpiresAtEndOfTurn() {
        Permanent commandos = addCreatureReady(player1, new NavSquadCommandos());
        addCreatureReady(player1, new NavSquadCommandos());
        addCreatureReady(player1, new NavSquadCommandos());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();
        assertThat(commandos.getPowerModifier()).isEqualTo(1);
        assertThat(commandos.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(commandos.getPowerModifier()).isZero();
        assertThat(commandos.getToughnessModifier()).isZero();
    }
}
