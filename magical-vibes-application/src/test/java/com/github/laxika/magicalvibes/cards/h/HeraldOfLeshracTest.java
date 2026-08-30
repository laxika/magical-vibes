package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HeraldOfLeshracTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each land controlled but not owned")
    void boostsForControlledLandsNotOwned() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new HeraldOfLeshrac());
        harness.addToBattlefield(player1, new Island());
        Permanent stolenLand = harness.addToBattlefieldAndReturn(player1, new Island());
        gd.stolenCreatures.put(stolenLand.getId(), player2.getId());

        assertThat(gqs.getEffectivePower(gd, herald)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, herald)).isEqualTo(5);
    }

    @Test
    @DisplayName("Paying cumulative upkeep gains control of an opponent's land")
    void paysCumulativeUpkeepByGainingLand() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new HeraldOfLeshrac());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(herald.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(herald, island);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(island);
    }

    @Test
    @DisplayName("When Herald of Leshrac leaves, each stolen land returns to its owner")
    void returnsOwnedLandsWhenItLeaves() {
        harness.addToBattlefield(player1, new HeraldOfLeshrac());
        Permanent landOwnedByPlayer2 = harness.addToBattlefieldAndReturn(player1, new Island());
        gd.stolenCreatures.put(landOwnedByPlayer2.getId(), player2.getId());
        Permanent landOwnedByPlayer1 = harness.addToBattlefieldAndReturn(player2, new Island());
        gd.stolenCreatures.put(landOwnedByPlayer1.getId(), player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(landOwnedByPlayer1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(landOwnedByPlayer2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(landOwnedByPlayer2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(landOwnedByPlayer1);
    }
}
