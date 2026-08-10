package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GoblinWarWagonTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped Goblin War Wagon does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent wagon = addGoblinWarWagon(player1, true);

        advanceToNextTurn(player2);

        assertThat(wagon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying {2} during upkeep untaps Goblin War Wagon")
    void payingTwoUntapsGoblinWarWagon() {
        Permanent wagon = addGoblinWarWagon(player1, true);

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(wagon.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the upkeep payment leaves Goblin War Wagon tapped")
    void decliningLeavesGoblinWarWagonTapped() {
        Permanent wagon = addGoblinWarWagon(player1, true);

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(wagon.isTapped()).isTrue();
    }

    private Permanent addGoblinWarWagon(Player player, boolean tapped) {
        Permanent permanent = new Permanent(new GoblinWarWagon());
        permanent.setSummoningSick(false);
        if (tapped) {
            permanent.tap();
        }
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
