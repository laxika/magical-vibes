package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LotusBlossomTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger may put a petal counter on Lotus Blossom")
    void upkeepMayPutPetalCounter() {
        Permanent blossom = harness.addToBattlefieldAndReturn(player1, new LotusBlossom());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(blossom.getCounterCount(CounterType.PETAL)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the upkeep trigger does not add a petal counter")
    void upkeepMayBeDeclined() {
        Permanent blossom = harness.addToBattlefieldAndReturn(player1, new LotusBlossom());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(blossom.getCounterCount(CounterType.PETAL)).isZero();
    }

    @Test
    @DisplayName("Sacrificing Lotus Blossom adds mana equal to its petal counters")
    void sacrificeAddsManaEqualToPetalCounters() {
        Permanent blossom = harness.addToBattlefieldAndReturn(player1, new LotusBlossom());
        blossom.setCounterCount(CounterType.PETAL, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blossom);
        harness.assertInGraveyard(player1, "Lotus Blossom");
    }
}
