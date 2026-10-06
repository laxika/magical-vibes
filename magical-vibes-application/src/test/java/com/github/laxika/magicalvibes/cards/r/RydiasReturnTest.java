package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RydiasReturn.class, GrizzlyBears.class, LeoninScimitar.class, Shock.class})
class RydiasReturnTest extends BaseCardTest {

    @Test
    void modeZeroBoostsOnlyYourCreaturesUntilEndOfTurn() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RydiasReturn()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
    }

    @Test
    void modeOneReturnsUpToTwoPermanentCardsAndExcludesNonpermanents() {
        Card creature = new GrizzlyBears();
        Card artifact = new LeoninScimitar();
        Card instant = new Shock();
        harness.setGraveyard(player1, List.of(creature, artifact, instant));
        harness.setHand(player1, List.of(new RydiasReturn()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(creature.getId(), artifact.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Leonin Scimitar");
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void modeOneCanReturnTwoPermanentCards() {
        Card creature = new GrizzlyBears();
        Card artifact = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setHand(player1, List.of(new RydiasReturn()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 1);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), artifact.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Leonin Scimitar");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Leonin Scimitar");
    }

    @Test
    void modeOneCanChooseZeroTargetsEvenWithEligibleCards() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new RydiasReturn()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 1);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Rydia's Return");
    }

    @Test
    void modeOneCanBeCastWithNoEligibleCardsInYourGraveyard() {
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new RydiasReturn()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveSorcery(player1, 0, 1);

        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Rydia's Return");
    }

    @Test
    void modeOneReturnsRemainingTargetWhenAnotherLeavesGraveyard() {
        Card creature = new GrizzlyBears();
        Card artifact = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setHand(player1, List.of(new RydiasReturn()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 1);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), artifact.getId()));
        harness.setGraveyard(player1, List.of(artifact));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Leonin Scimitar");
        harness.assertInGraveyard(player1, "Rydia's Return");
    }

    @Test
    void modeZeroDoesNotBoostCreaturesEnteringAfterResolutionOrReturnCards() {
        Permanent original = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new LeoninScimitar()));
        harness.setHand(player1, List.of(new RydiasReturn()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent later = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, later)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Leonin Scimitar");
        harness.assertNotInHand(player1, "Leonin Scimitar");
    }
}
