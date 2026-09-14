package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HondenOfSeeingWinds;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoShintaiOfSharedPurpose.class, HondenOfSeeingWinds.class})
class GoShintaiOfSharedPurposeTest extends BaseCardTest {

    @Test
    @DisplayName("At your end step, paying {1} creates a Spirit for each Shrine you control")
    void paysToCreateOneSpiritPerShrine() {
        harness.addToBattlefield(player1, new GoShintaiOfSharedPurpose());
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
    }

    @Test
    @DisplayName("Declining the payment creates no Spirits")
    void declinesPayment() {
        harness.addToBattlefield(player1, new GoShintaiOfSharedPurpose());
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("The ability triggers only during its controller's end step")
    void triggersOnlyOnControllersEndStep() {
        harness.addToBattlefield(player1, new GoShintaiOfSharedPurpose());

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNull();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
