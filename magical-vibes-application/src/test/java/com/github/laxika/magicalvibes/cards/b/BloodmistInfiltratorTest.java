package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BloodmistInfiltratorTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers sacrificing another creature")
    void attackingOffersSacrifice() {
        Permanent infiltrator = addCreatureReady(player1, new BloodmistInfiltrator());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(bears.getId());
        assertThat(choice.validIds()).doesNotContain(infiltrator.getId());
    }

    @Test
    @DisplayName("Sacrificing another creature makes Bloodmist Infiltrator unblockable")
    void sacrificingAnotherCreatureMakesItUnblockable() {
        Permanent infiltrator = addCreatureReady(player1, new BloodmistInfiltrator());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(infiltrator.isCantBeBlocked()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears.getCard());
    }

    @Test
    @DisplayName("Declining the sacrifice does not make Bloodmist Infiltrator unblockable")
    void decliningSacrificeDoesNothing() {
        Permanent infiltrator = addCreatureReady(player1, new BloodmistInfiltrator());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(infiltrator.isCantBeBlocked()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
    }

    @Test
    @DisplayName("The unblockable effect expires at end of turn")
    void unblockableExpiresAtEndOfTurn() {
        Permanent infiltrator = addCreatureReady(player1, new BloodmistInfiltrator());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(infiltrator.isCantBeBlocked()).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(infiltrator.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("With no other creature, accepting the may does nothing")
    void noOtherCreatureDoesNothing() {
        Permanent infiltrator = addCreatureReady(player1, new BloodmistInfiltrator());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(infiltrator.isCantBeBlocked()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
