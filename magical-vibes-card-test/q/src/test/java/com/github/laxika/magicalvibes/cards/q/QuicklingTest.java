package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NissaWorldwaker;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Quickling.class, RuneclawBear.class, NissaWorldwaker.class, Forest.class})
class QuicklingTest extends BaseCardTest {

    @Test
    void sacrificesWhenItIsTheOnlyCreatureItsControllerHas() {
        castQuickling();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Quickling");
        harness.assertInGraveyard(player1, "Quickling");
    }

    @Test
    void returningAnotherCreatureKeepsQuickling() {
        harness.addToBattlefield(player1, new RuneclawBear());

        castQuickling();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        UUID bearsId = harness.getPermanentId(player1, "Runeclaw Bear");
        harness.handlePermanentChosen(player1, bearsId);

        harness.assertOnBattlefield(player1, "Quickling");
        harness.assertInHand(player1, "Runeclaw Bear");
    }

    @Test
    void decliningTheReturnSacrificesQuickling() {
        harness.addToBattlefield(player1, new RuneclawBear());

        castQuickling();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Quickling");
        harness.assertOnBattlefield(player1, "Runeclaw Bear");
    }

    @Test
    void opponentCreatureDoesNotSatisfyTheRequirement() {
        harness.addToBattlefield(player2, new RuneclawBear());

        castQuickling();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Quickling");
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
    }

    @Test
    void quicklingIsNotAValidReturnChoice() {
        harness.addToBattlefield(player1, new RuneclawBear());

        castQuickling();
        harness.handleMayAbilityChosen(player1, true);

        UUID quicklingId = harness.getPermanentId(player1, "Quickling");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .doesNotContain(quicklingId);
    }

    @Test
    void canReturnAnAnimatedLandWhenItIsTheOnlyOtherCreature() {
        Permanent forest = animateForest();

        castQuickling();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, forest.getId());

        harness.assertOnBattlefield(player1, "Quickling");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void animatedLandIsAValidReturnChoiceAlongsideAPrintedCreature() {
        Permanent forest = animateForest();
        harness.addToBattlefield(player1, new RuneclawBear());

        castQuickling();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .contains(forest.getId());
        harness.handlePermanentChosen(player1, forest.getId());

        harness.assertOnBattlefield(player1, "Quickling");
        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void canReturnAnotherQuickling() {
        Permanent otherQuickling = harness.addToBattlefieldAndReturn(player1, new Quickling());

        castQuickling();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, otherQuickling.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(otherQuickling.getId()));
        harness.assertOnBattlefield(player1, "Quickling");
        harness.assertInHand(player1, "Quickling");
    }

    private Permanent animateForest() {
        harness.addToBattlefield(player1, new NissaWorldwaker());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, forest.getId(), null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        return forest;
    }

    private void castQuickling() {
        harness.setHand(player1, List.of(new Quickling()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
