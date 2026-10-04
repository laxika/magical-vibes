package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.h.HeadlessSkaab;
import com.github.laxika.magicalvibes.cards.t.ThrabenHeretic;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FalkenrathAristocrat.class, HeadlessSkaab.class, ThrabenHeretic.class})
class FalkenrathAristocratTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Human grants indestructible and a +1/+1 counter")
    void sacrificeHumanGivesIndestructibleAndCounter() {
        Permanent aristocrat = addCreatureReady(player1, new FalkenrathAristocrat());
        harness.addToBattlefield(player1, new ThrabenHeretic());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Thraben Heretic");

        assertThat(aristocrat.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
        assertThat(aristocrat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing a non-Human creature grants indestructible but no counter")
    void sacrificeNonHumanGivesIndestructibleNoCounter() {
        Permanent aristocrat = addCreatureReady(player1, new FalkenrathAristocrat());
        Permanent fodder = addCreatureReady(player1, new HeadlessSkaab());

        harness.activateAbility(player1, 0, 1, null, null);
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, fodder.getId());
        }
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Headless Skaab");

        assertThat(aristocrat.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
        assertThat(aristocrat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The non-Human ability sacrifices the source without sacrificing the Human")
    void nonHumanAbilityLeavesHumanAlone() {
        addCreatureReady(player1, new FalkenrathAristocrat());
        harness.addToBattlefield(player1, new ThrabenHeretic());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Falkenrath Aristocrat");
        harness.assertOnBattlefield(player1, "Thraben Heretic");
    }

    @Test
    @DisplayName("Can sacrifice itself with no other creatures")
    void canSacrificeSelfToNonHumanAbility() {
        addCreatureReady(player1, new FalkenrathAristocrat());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertInGraveyard(player1, "Falkenrath Aristocrat");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Human-sacrifice ability cannot be activated without a Human")
    void humanAbilityRequiresHuman() {
        addCreatureReady(player1, new FalkenrathAristocrat());
        harness.addToBattlefield(player1, new HeadlessSkaab());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Granted indestructible is removed at end of turn")
    void indestructibleResetsAtEndOfTurn() {
        Permanent aristocrat = addCreatureReady(player1, new FalkenrathAristocrat());
        Permanent fodder = addCreatureReady(player1, new HeadlessSkaab());

        harness.activateAbility(player1, 0, 1, null, null);
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, fodder.getId());
        }
        harness.passBothPriorities();
        assertThat(aristocrat.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(aristocrat.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("The Human is sacrificed as a cost before the ability resolves")
    void humanSacrificeIsPaidBeforeResolution() {
        Permanent aristocrat = addCreatureReady(player1, new FalkenrathAristocrat());
        harness.addToBattlefield(player1, new ThrabenHeretic());

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertInGraveyard(player1, "Thraben Heretic");
        assertThat(aristocrat.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
        assertThat(aristocrat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(aristocrat.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
        assertThat(aristocrat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each Human sacrifice adds a counter, and counters survive cleanup")
    void repeatedHumanSacrificesGivePermanentCounters() {
        Permanent aristocrat = addCreatureReady(player1, new FalkenrathAristocrat());
        harness.addToBattlefield(player1, new ThrabenHeretic());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new ThrabenHeretic());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(aristocrat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(aristocrat.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(aristocrat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(aristocrat.getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's Human to pay the ability cost")
    void cannotSacrificeOpponentsHuman() {
        addCreatureReady(player1, new FalkenrathAristocrat());
        harness.addToBattlefield(player2, new ThrabenHeretic());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Thraben Heretic");
        assertThat(gd.stack).isEmpty();
    }

}
