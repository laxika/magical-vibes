package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(AugurIlVec.class)
class AugurIlVecTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing it during upkeep gains 4 life")
    void sacrificeDuringUpkeepGainsFourLife() {
        addCreatureReady(player1, new AugurIlVec());
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Augur il-Vec");
        harness.assertInGraveyard(player1, "Augur il-Vec");
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Sacrifice ability cannot be activated outside its controller's upkeep")
    void sacrificeAbilityRequiresYourUpkeep() {
        addCreatureReady(player1, new AugurIlVec());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("Sacrifice ability cannot be activated during an opponent's upkeep")
    void sacrificeAbilityCannotBeActivatedDuringOpponentsUpkeep() {
        addCreatureReady(player1, new AugurIlVec());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }
}
