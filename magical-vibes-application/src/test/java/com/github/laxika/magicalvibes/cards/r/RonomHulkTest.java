package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SnowCoveredMountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RonomHulk.class, SnowCoveredMountain.class})
class RonomHulkTest extends BaseCardTest {

    @Test
    @DisplayName("Protection from snow applies to snow permanents only")
    void hasProtectionFromSnow() {
        Permanent hulk = harness.addToBattlefieldAndReturn(player1, new RonomHulk());
        Permanent snowSource = harness.addToBattlefieldAndReturn(player2, new SnowCoveredMountain());
        Permanent nonSnowSource = harness.addToBattlefieldAndReturn(player2, new RonomHulk());

        assertThat(gqs.hasProtectionFromSource(gd, hulk, snowSource)).isTrue();
        assertThat(gqs.hasProtectionFromSource(gd, hulk, nonSnowSource)).isFalse();
    }

    @Test
    @DisplayName("Paying cumulative upkeep keeps Ronom Hulk and adds an age counter")
    void paysCumulativeUpkeep() {
        Permanent hulk = harness.addToBattlefieldAndReturn(player1, new RonomHulk());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(hulk.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hulk);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Cumulative upkeep costs two mana on the second upkeep")
    void cumulativeUpkeepScalesWithAgeCounters() {
        Permanent hulk = harness.addToBattlefieldAndReturn(player1, new RonomHulk());

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();

        assertThat(hulk.getCounterCount(CounterType.AGE)).isEqualTo(2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hulk);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Ronom Hulk")
    void declineSacrifices() {
        Permanent hulk = harness.addToBattlefieldAndReturn(player1, new RonomHulk());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(hulk);
        harness.assertInGraveyard(player1, "Ronom Hulk");
    }

    @Test
    @DisplayName("Cumulative upkeep triggers only during Ronom Hulk's controller's upkeep")
    void triggersOnlyDuringControllersUpkeep() {
        Permanent hulk = harness.addToBattlefieldAndReturn(player1, new RonomHulk());

        advanceToUpkeep(player2);

        assertThat(hulk.getCounterCount(CounterType.AGE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hulk);
    }

    @Test
    @DisplayName("Accepting an unpayable cumulative upkeep sacrifices Ronom Hulk")
    void unpayableCumulativeUpkeepSacrifices() {
        Permanent hulk = harness.addToBattlefieldAndReturn(player1, new RonomHulk());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(hulk);
        harness.assertInGraveyard(player1, "Ronom Hulk");
    }
}
