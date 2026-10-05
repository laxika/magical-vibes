package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Planebound Accomplice")
@CardUsed({PlaneboundAccomplice.class, GarrukWildspeaker.class, GrizzlyBears.class, Mountain.class})
class PlaneboundAccompliceTest extends BaseCardTest {

    @Test
    @DisplayName("Offers only planeswalker cards from hand")
    void offersOnlyPlaneswalkers() {
        addReadyAccomplice();
        harness.setHand(player1, List.of(new Mountain(), new GrizzlyBears(), new GarrukWildspeaker()));
        addRedMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(2);
    }

    @Test
    @DisplayName("Puts the chosen planeswalker onto the battlefield and schedules its sacrifice")
    void putsPlaneswalkerOntoBattlefieldAndSchedulesSacrifice() {
        addReadyAccomplice();
        harness.setHand(player1, List.of(new GarrukWildspeaker()));
        addRedMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent planeswalker = findPermanent(player1, "Garruk Wildspeaker");
        assertThat(planeswalker).isNotNull();
        harness.assertNotInHand(player1, "Garruk Wildspeaker");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.assertOnBattlefield(player1, "Garruk Wildspeaker");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Garruk Wildspeaker");
    }

    @Test
    @DisplayName("Sacrifices the planeswalker at the next end step")
    void sacrificesPlaneswalkerAtNextEndStep() {
        addReadyAccomplice();
        harness.setHand(player1, List.of(new GarrukWildspeaker()));
        addRedMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Garruk Wildspeaker");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.assertOnBattlefield(player1, "Garruk Wildspeaker");

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Garruk Wildspeaker");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Garruk Wildspeaker");
        harness.assertInGraveyard(player1, "Garruk Wildspeaker");
    }

    @Test
    @DisplayName("Declining leaves the planeswalker in hand")
    void decliningLeavesPlaneswalkerInHand() {
        addReadyAccomplice();
        harness.setHand(player1, List.of(new GarrukWildspeaker()));
        addRedMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Garruk Wildspeaker");
        harness.assertNotOnBattlefield(player1, "Garruk Wildspeaker");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Garruk Wildspeaker");
    }

    @Test
    @DisplayName("Can activate while summoning sick and tapped")
    void activatesWhileSummoningSickAndTapped() {
        Permanent accomplice = harness.addToBattlefieldAndReturn(player1, new PlaneboundAccomplice());
        accomplice.setSummoningSick(true);
        accomplice.tap();
        harness.setHand(player1, List.of(new GarrukWildspeaker()));
        addRedMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Garruk Wildspeaker");
        harness.assertNotInHand(player1, "Garruk Wildspeaker");
    }

    @Test
    @DisplayName("Accepting with no planeswalker in hand completes without a card choice")
    void acceptingWithNoPlaneswalkerCompletes() {
        addReadyAccomplice();
        harness.setHand(player1, List.of(new Mountain(), new GrizzlyBears()));
        addRedMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Mountain");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private Permanent addReadyAccomplice() {
        Permanent accomplice = harness.addToBattlefieldAndReturn(player1, new PlaneboundAccomplice());
        accomplice.setSummoningSick(false);
        return accomplice;
    }

    private void addRedMana() {
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
