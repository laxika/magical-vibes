package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArcticWolvesTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws a card")
    void enteringDrawsACard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ArcticWolves()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the creature spell
        harness.passBothPriorities(); // resolve the enters trigger

        // Cast one card out of hand, drew one back.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.assertOnBattlefield(player1, "Arctic Wolves");
    }

    @Test
    @DisplayName("Cumulative upkeep puts an age counter on and can be paid")
    void cumulativeUpkeepPaid() {
        Permanent wolves = harness.addToBattlefieldAndReturn(player1, new ArcticWolves());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(wolves.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wolves);
    }

    @Test
    @DisplayName("Declining the cumulative upkeep sacrifices it")
    void declineSacrifices() {
        Permanent wolves = harness.addToBattlefieldAndReturn(player1, new ArcticWolves());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(wolves);
        harness.assertInGraveyard(player1, "Arctic Wolves");
    }
}
