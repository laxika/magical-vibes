package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.h.HeadlessSkaab;
import com.github.laxika.magicalvibes.cards.s.ScornedVillager;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FalkenrathTorturer.class, HeadlessSkaab.class, ScornedVillager.class})
class FalkenrathTorturerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Human grants flying and a +1/+1 counter")
    void sacrificeHumanGivesFlyingAndCounter() {
        Permanent torturer = addCreatureReady(player1, new FalkenrathTorturer());
        harness.addToBattlefield(player1, new ScornedVillager());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Scorned Villager");

        assertThat(torturer.getGrantedKeywords()).contains(Keyword.FLYING);
        assertThat(torturer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing a non-Human creature grants flying but no counter")
    void sacrificeNonHumanGivesFlyingNoCounter() {
        Permanent torturer = addCreatureReady(player1, new FalkenrathTorturer());
        harness.addToBattlefield(player1, new HeadlessSkaab());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Headless Skaab");

        assertThat(torturer.getGrantedKeywords()).contains(Keyword.FLYING);
        assertThat(torturer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The non-Human ability cannot sacrifice a Human")
    void nonHumanAbilityCannotSacrificeHuman() {
        addCreatureReady(player1, new FalkenrathTorturer());
        harness.addToBattlefield(player1, new ScornedVillager());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Falkenrath Torturer can sacrifice itself")
    void canSacrificeSelfToNonHumanAbility() {
        harness.addToBattlefield(player1, new FalkenrathTorturer());

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Falkenrath Torturer");
        harness.assertInGraveyard(player1, "Falkenrath Torturer");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Human-sacrifice ability cannot be activated without a Human")
    void humanAbilityRequiresHuman() {
        addCreatureReady(player1, new FalkenrathTorturer());
        harness.addToBattlefield(player1, new HeadlessSkaab());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Granted flying is removed at end of turn")
    void flyingResetsAtEndOfTurn() {
        Permanent torturer = addCreatureReady(player1, new FalkenrathTorturer());
        harness.addToBattlefield(player1, new HeadlessSkaab());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(torturer.getGrantedKeywords()).contains(Keyword.FLYING);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(torturer.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
    }

    @Test
    @DisplayName("Sacrifice is paid immediately, while flying and the counter wait for resolution")
    void humanSacrificeIsACostAndCounterPersists() {
        Permanent torturer = harness.addToBattlefieldAndReturn(player1, new FalkenrathTorturer());
        harness.addToBattlefield(player1, new ScornedVillager());

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertInGraveyard(player1, "Scorned Villager");
        harness.assertNotOnBattlefield(player1, "Scorned Villager");
        assertThat(torturer.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
        assertThat(torturer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(torturer.getGrantedKeywords()).contains(Keyword.FLYING);
        assertThat(torturer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(torturer.getGrantedKeywords()).doesNotContain(Keyword.FLYING);
        assertThat(torturer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's Human cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsHuman() {
        harness.addToBattlefield(player1, new FalkenrathTorturer());
        harness.addToBattlefield(player2, new ScornedVillager());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Scorned Villager");
        assertThat(gd.stack).isEmpty();
    }
}
