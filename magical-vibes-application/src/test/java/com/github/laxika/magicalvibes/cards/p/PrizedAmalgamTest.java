package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.m.MacabreWaltz;
import com.github.laxika.magicalvibes.cards.r.ReassemblingSkeleton;
import com.github.laxika.magicalvibes.cards.w.WorldheartPhoenix;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrizedAmalgam.class, ReassemblingSkeleton.class, WorldheartPhoenix.class, MacabreWaltz.class})
class PrizedAmalgamTest extends BaseCardTest {

    @Test
    @DisplayName("Returns tapped at the next end step when a creature enters from its graveyard")
    void returnsAfterCreatureEntersFromGraveyard() {
        harness.setGraveyard(player1, List.of(new PrizedAmalgam(), new ReassemblingSkeleton()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 1);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Prized Amalgam");
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        Permanent amalgam = findPermanent(player1, "Prized Amalgam");
        assertThat(amalgam).isNotNull();
        assertThat(amalgam.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Returns tapped when a creature is cast from its graveyard")
    void returnsAfterCreatureIsCastFromGraveyard() {
        harness.setGraveyard(player1, List.of(new PrizedAmalgam(), new WorldheartPhoenix()));
        addFiveColorsOfMana();

        harness.castFromGraveyard(player1, 1);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Prized Amalgam");
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        Permanent amalgam = findPermanent(player1, "Prized Amalgam");
        assertThat(amalgam).isNotNull();
        assertThat(amalgam.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not trigger when a creature is cast from hand")
    void doesNotTriggerForCreatureCastFromHand() {
        harness.setGraveyard(player1, List.of(new PrizedAmalgam()));
        harness.setHand(player1, List.of(new ReassemblingSkeleton()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Prized Amalgam");
        harness.assertInGraveyard(player1, "Prized Amalgam");
    }

    @Test
    @DisplayName("Does not trigger for a creature entering from the opponent's graveyard")
    void doesNotTriggerForOpponentsGraveyard() {
        harness.setGraveyard(player1, List.of(new PrizedAmalgam()));
        harness.setGraveyard(player2, List.of(new ReassemblingSkeleton()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player2);
        harness.activateGraveyardAbility(player2, 0);
        resolveAllTriggers();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Reassembling Skeleton");
        harness.assertNotOnBattlefield(player1, "Prized Amalgam");
        harness.assertInGraveyard(player1, "Prized Amalgam");
    }

    @Test
    @DisplayName("A creature returning during an end step waits for the following end step")
    void returnDuringEndStepWaitsForFollowingEndStep() {
        harness.setGraveyard(player1, List.of(new PrizedAmalgam(), new ReassemblingSkeleton()));
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.ensurePriority(player1);
        harness.activateGraveyardAbility(player1, 1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Prized Amalgam");
        harness.assertInGraveyard(player1, "Prized Amalgam");
        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        Permanent amalgam = findPermanent(player1, "Prized Amalgam");
        assertThat(amalgam).isNotNull();
        assertThat(amalgam.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An old delayed return cannot return an Amalgam that left and re-entered the graveyard")
    void doesNotReturnAfterLeavingAndReenteringGraveyard() {
        PrizedAmalgam amalgam = new PrizedAmalgam();
        harness.setGraveyard(player1, List.of(amalgam, new ReassemblingSkeleton()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateGraveyardAbility(player1, 1);
        resolveAllTriggers();

        harness.castFromHand(player1, new MacabreWaltz(), "{1}{B}");
        harness.handleMultipleCardsChosen(player1, List.of(amalgam.getId()));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Prized Amalgam");
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Prized Amalgam");
        harness.assertInGraveyard(player1, "Prized Amalgam");
    }

    private void addFiveColorsOfMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

}
