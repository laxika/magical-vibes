package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeepfathomEcho.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class DeepfathomEchoTest extends BaseCardTest {

    @Test
    @DisplayName("Explores and may become a copy of another creature you control")
    void exploresAndCopiesAnotherCreature() {
        Permanent echo = addEcho();
        harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToCombat(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof Forest);
        assertThat(gqs.getEffectivePower(gd, echo)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, echo)).isEqualTo(3);
    }

    @Test
    @DisplayName("A nonland explore puts a counter on Deepfathom Echo before copying")
    void nonlandExploreAddsCounterBeforeCopying() {
        Permanent echo = addEcho();
        harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToCombat(player1);

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(echo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, echo)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, echo)).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    @DisplayName("Declining the copy leaves the explored creature unchanged")
    void decliningCopyLeavesCreatureUnchanged() {
        Permanent echo = addEcho();
        harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(echo.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, echo)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, echo)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof Forest);
    }

    @Test
    @DisplayName("The copy wears off at end of turn")
    void copyWearsOffAtEndOfTurn() {
        Permanent echo = addEcho();
        harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, echo)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, echo)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, echo)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, echo)).isEqualTo(4);
    }

    @Test
    @DisplayName("The copy choice offers only another creature controlled by Deepfathom Echo's controller")
    void choiceOffersOnlyAnotherOwnCreature() {
        Permanent echo = addEcho();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondOwnCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToCombat(player1);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(ownCreature.getId(), secondOwnCreature.getId())
                .doesNotContain(echo.getId(), opponentCreature.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());
    }

    @Test
    @DisplayName("The ability does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentsCombat() {
        addEcho();
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addEcho() {
        return harness.addToBattlefieldAndReturn(player1, new DeepfathomEcho());
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
