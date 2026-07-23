package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfFire;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlowOfMaggotsTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

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
    @DisplayName("Declining cumulative upkeep sacrifices Flow of Maggots")
    void declineSacrifices() {
        Permanent maggots = harness.addToBattlefieldAndReturn(player1, new FlowOfMaggots());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(maggots);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Flow of Maggots"));
    }

    @Test
    @DisplayName("Flow of Maggots can't be blocked by a non-Wall creature")
    void cannotBeBlockedByNonWall() {
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        Permanent maggots = new Permanent(new FlowOfMaggots());
        maggots.setSummoningSick(false);
        maggots.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(maggots);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(maggots);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Walls");
    }

    @Test
    @DisplayName("Flow of Maggots can be blocked by a Wall")
    void canBeBlockedByWall() {
        Permanent wall = new Permanent(new WallOfFire());
        wall.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(wall);

        Permanent maggots = new Permanent(new FlowOfMaggots());
        maggots.setSummoningSick(false);
        maggots.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(maggots);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(wall);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(maggots);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(wall.isBlocking()).isTrue();
    }
}
