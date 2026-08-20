package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class BraidOfFireTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep adds one red mana")
    void addsOneRedMana() {
        Permanent braid = harness.addToBattlefieldAndReturn(player1, new BraidOfFire());
        gd.playerAutoStopSteps.put(player1.getId(), Set.of(TurnStep.UPKEEP));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(braid.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(braid);
    }

    @Test
    @DisplayName("Braid of Fire adds one red mana per age counter")
    void addsRedManaForEachAgeCounter() {
        Permanent braid = harness.addToBattlefieldAndReturn(player1, new BraidOfFire());
        braid.setCounterCount(CounterType.AGE, 1);
        gd.playerAutoStopSteps.put(player1.getId(), Set.of(TurnStep.UPKEEP));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(braid.getCounterCount(CounterType.AGE)).isEqualTo(2);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining Braid of Fire's cumulative upkeep sacrifices it")
    void decliningCumulativeUpkeepSacrifices() {
        Permanent braid = harness.addToBattlefieldAndReturn(player1, new BraidOfFire());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(braid);
        harness.assertInGraveyard(player1, "Braid of Fire");
    }
}
