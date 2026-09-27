package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GilraenDNedainProtector.class, GrizzlyBears.class})
class GilraenDNedainProtectorTest extends BaseCardTest {

    @Test
    @DisplayName("Ability may immediately return another creature under its owner's control")
    void mayReturnCreatureImmediately() {
        addCreatureReady(player1, new GilraenDNedainProtector());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        activate(target);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(target.getId());
        assertThat(returned.getCounterCount(CounterType.VIGILANCE)).isZero();
        assertThat(returned.getCounterCount(CounterType.LIFELINK)).isZero();
    }

    @Test
    @DisplayName("Declining immediate return schedules the creature with vigilance and lifelink counters")
    void returnsAtNextEndStepWithKeywordCounters() {
        addCreatureReady(player1, new GilraenDNedainProtector());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        activate(target);

        harness.handleMayAbilityChosen(player1, false);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");

        advanceToEndStep();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
        assertThat(returned.getCounterCount(CounterType.LIFELINK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target Gilraen itself")
    void cannotTargetSelf() {
        Permanent gilraen = addCreatureReady(player1, new GilraenDNedainProtector());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, gilraen.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void activate(Permanent target) {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
