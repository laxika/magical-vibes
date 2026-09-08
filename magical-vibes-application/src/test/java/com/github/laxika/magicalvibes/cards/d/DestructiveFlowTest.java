package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SavageLands;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DestructiveFlowTest extends BaseCardTest {

    @Test
    @DisplayName("At each player's upkeep that player sacrifices a nonbasic land")
    void sacrificesNonbasicLandAtEachPlayersUpkeep() {
        harness.addToBattlefield(player1, new DestructiveFlow());
        Permanent basicLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent nonbasicLand = harness.addToBattlefieldAndReturn(player2, new SavageLands());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(basicLand.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(nonbasicLand.getId()));
    }

    @Test
    @DisplayName("The controller also sacrifices a nonbasic land on their own upkeep")
    void triggersOnControllersUpkeep() {
        harness.addToBattlefield(player1, new DestructiveFlow());
        Permanent nonbasicLand = harness.addToBattlefieldAndReturn(player1, new SavageLands());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(nonbasicLand.getId()));
    }

    @Test
    @DisplayName("With multiple nonbasic lands the player chooses which one to sacrifice")
    void choosesAmongMultipleNonbasicLands() {
        harness.addToBattlefield(player1, new DestructiveFlow());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new SavageLands());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SavageLands());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(first.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(second.getId()));
    }
}
