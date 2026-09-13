package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HondenOfSeeingWinds;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoShintaiOfLostWisdom.class, HondenOfSeeingWinds.class})
class GoShintaiOfLostWisdomTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of your end step, paying {1} mills target player for each Shrine you control")
    void payingManaMillsForEachShrine() {
        harness.addToBattlefield(player1, new GoShintaiOfLostWisdom());
        harness.addToBattlefield(player1, new HondenOfSeeingWinds());

        advanceToEndStep();
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore - 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Declining the payment does not mill")
    void decliningPaymentDoesNotMill() {
        harness.addToBattlefield(player1, new GoShintaiOfLostWisdom());

        advanceToEndStep();
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The trigger targets a player")
    void triggerRequiresAPlayerTarget() {
        harness.addToBattlefield(player1, new GoShintaiOfLostWisdom());

        advanceToEndStep();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
