package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HondenOfSeeingWinds;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoShintaiOfAncientWars.class, HondenOfSeeingWinds.class})
class GoShintaiOfAncientWarsTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1} deals damage equal to the number of Shrines to a target player")
    void paysToDealDamageForEachShrine() {
        harness.addToBattlefield(player1, new GoShintaiOfAncientWars());
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());
        harness.setLife(player2, 20);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Declining the payment deals no damage")
    void declinesPayment() {
        harness.addToBattlefield(player1, new GoShintaiOfAncientWars());
        harness.setLife(player2, 20);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The ability triggers only during its controller's end step")
    void triggersOnlyOnControllersEndStep() {
        harness.addToBattlefield(player1, new GoShintaiOfAncientWars());

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
