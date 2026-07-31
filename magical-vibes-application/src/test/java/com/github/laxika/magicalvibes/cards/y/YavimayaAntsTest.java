package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class YavimayaAntsTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep keeps Yavimaya Ants")
    void paysCumulativeUpkeep() {
        Permanent ants = harness.addToBattlefieldAndReturn(player1, new YavimayaAnts());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(ants.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ants);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Yavimaya Ants")
    void declineSacrifices() {
        Permanent ants = harness.addToBattlefieldAndReturn(player1, new YavimayaAnts());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ants);
        harness.assertInGraveyard(player1, "Yavimaya Ants");
    }
}
