package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.g.GlacialWall;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlowOfMaggots.class, BalduvianBears.class, GlacialWall.class})
class FlowOfMaggotsTest extends BaseCardTest {

    @Test
    @DisplayName("Paying cumulative upkeep keeps Flow of Maggots")
    void paysCumulativeUpkeep() {
        Permanent maggots = harness.addToBattlefieldAndReturn(player1, new FlowOfMaggots());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(maggots.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(maggots);
    }

    @Test
    @DisplayName("Second cumulative upkeep costs two generic mana")
    void secondUpkeepCostsTwoMana() {
        Permanent maggots = harness.addToBattlefieldAndReturn(player1, new FlowOfMaggots());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(maggots.getCounterCount(CounterType.AGE)).isEqualTo(2);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(maggots);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Flow of Maggots")
    void declineSacrifices() {
        Permanent maggots = harness.addToBattlefieldAndReturn(player1, new FlowOfMaggots());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(maggots);
        harness.assertInGraveyard(player1, "Flow of Maggots");
    }

    @Test
    @DisplayName("Flow of Maggots is sacrificed when cumulative upkeep cannot be paid")
    void insufficientManaSacrifices() {
        Permanent maggots = harness.addToBattlefieldAndReturn(player1, new FlowOfMaggots());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(maggots);
        harness.assertInGraveyard(player1, "Flow of Maggots");
    }

    @Test
    @DisplayName("Flow of Maggots can't be blocked by a non-Wall creature")
    void cannotBeBlockedByNonWall() {
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());

        Permanent maggots = addCreatureReady(player1, new FlowOfMaggots());
        maggots.setAttacking(true);

        prepareDeclareBlockers(player1);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(maggots);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Walls");
    }

    @Test
    @DisplayName("Flow of Maggots can be blocked by a Wall")
    void canBeBlockedByWall() {
        Permanent wall = addCreatureReady(player2, new GlacialWall());

        Permanent maggots = addCreatureReady(player1, new FlowOfMaggots());
        maggots.setAttacking(true);

        prepareDeclareBlockers(player1);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(wall);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(maggots);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(wall.isBlocking()).isTrue();
    }
}
