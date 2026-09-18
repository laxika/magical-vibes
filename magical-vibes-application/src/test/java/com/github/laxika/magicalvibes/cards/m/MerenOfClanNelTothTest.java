package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerenOfClanNelToth.class, GrizzlyBears.class, AirElemental.class, Shock.class})
class MerenOfClanNelTothTest extends BaseCardTest {

    @Test
    void gainsExperienceWhenAnotherCreatureYouControlDies() {
        harness.addToBattlefield(player1, new MerenOfClanNelToth());
        harness.addToBattlefield(player1, new GrizzlyBears());

        killOwnCreature();

        assertThat(gd.playerExperienceCounters).containsEntry(player1.getId(), 1);
    }

    @Test
    void returnsCreatureToBattlefieldWhenManaValueIsWithinExperience() {
        Card target = new GrizzlyBears();
        harness.addToBattlefield(player1, new MerenOfClanNelToth());
        harness.setGraveyard(player1, List.of(target));
        gd.playerExperienceCounters.put(player1.getId(), 2);

        advanceToEndStep();
        chooseGraveyardTarget(target);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void returnsCreatureToHandWhenManaValueExceedsExperience() {
        Card target = new AirElemental();
        harness.addToBattlefield(player1, new MerenOfClanNelToth());
        harness.setGraveyard(player1, List.of(target));
        gd.playerExperienceCounters.put(player1.getId(), 2);

        advanceToEndStep();
        chooseGraveyardTarget(target);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Air Elemental");
        harness.assertNotInGraveyard(player1, "Air Elemental");
    }

    @Test
    void endStepAbilityOnlyTargetsCreatureCardsInYourGraveyard() {
        Card target = new GrizzlyBears();
        harness.addToBattlefield(player1, new MerenOfClanNelToth());
        harness.setGraveyard(player1, List.of(new Shock(), target));
        gd.playerExperienceCounters.put(player1.getId(), 2);

        advanceToEndStep();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(target.getId());
    }

    private void killOwnCreature() {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();
        resolveAllTriggers();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void chooseGraveyardTarget(Card target) {
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
    }
}
