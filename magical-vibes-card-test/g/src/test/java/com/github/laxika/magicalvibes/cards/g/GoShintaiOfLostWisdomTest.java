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
        harness.passBothPriorities();

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

    @Test
    @DisplayName("Payment creates a separate milling trigger that players can respond to")
    void paymentCreatesSeparateMillingTrigger() {
        harness.addToBattlefield(player1, new GoShintaiOfLostWisdom());
        advanceToEndStep();
        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.handlePermanentChosen(player1, player2.getId());

            assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeBefore);
            assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
            assertThat(gd.stack).hasSize(1);
            harness.passBothPriorities();

            assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeBefore - 1);
            assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        });
    }

    @Test
    @DisplayName("Opponent's Shrines do not increase the milling amount")
    void opponentShrinesDoNotCount() {
        harness.addToBattlefield(player1, new GoShintaiOfLostWisdom());
        harness.addToBattlefield(player2, new GoShintaiOfLostWisdom());
        advanceToEndStep();
        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeBefore - 1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The Shrine count is evaluated when the milling trigger resolves")
    void shrineCountIsEvaluatedAtResolution() {
        harness.addToBattlefield(player1, new GoShintaiOfLostWisdom());
        advanceToEndStep();
        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.handlePermanentChosen(player1, player2.getId());
            gd.playerBattlefields.get(player1.getId()).clear();
            harness.passBothPriorities();

            assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeBefore);
            assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        });
    }

    @Test
    @DisplayName("The ability does not trigger during the opponent's end step")
    void doesNotTriggerDuringOpponentEndStep() {
        harness.addToBattlefield(player1, new GoShintaiOfLostWisdom());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Accepting without enough mana does not mill or ask for a target")
    void cannotMillWithoutPaying() {
        harness.addToBattlefield(player1, new GoShintaiOfLostWisdom());
        advanceToEndStep();
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
