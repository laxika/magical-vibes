package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.h.HandOfHonor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WineOfBloodAndIron.class, HandOfHonor.class})
class WineOfBloodAndIronTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles the target creature's power and sacrifices at the next end step")
    void boostsTargetAndSacrificesAtNextEndStep() {
        Permanent wine = harness.addToBattlefieldAndReturn(player1, new WineOfBloodAndIron());
        Permanent creature = addCreatureReady(player1, new HandOfHonor());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wine);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wine of Blood and Iron");
        harness.assertInGraveyard(player1, "Wine of Blood and Iron");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Uses the target creature's power when the ability resolves")
    void evaluatesTargetPowerAtResolution() {
        Permanent creature = addCreatureReady(player1, new HandOfHonor());
        Permanent wine = harness.addToBattlefieldAndReturn(player1, new WineOfBloodAndIron());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 1, null, creature.getId());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wine);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefieldAndReturn(player1, new WineOfBloodAndIron());
        Permanent otherWine = harness.addToBattlefieldAndReturn(player1, new WineOfBloodAndIron());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, otherWine.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
