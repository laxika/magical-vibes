package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FleshtakerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature gains 1 life and scries 1")
    void sacrificeTriggersLifeGainAndScry() {
        Permanent fleshtaker = addCreatureReady(player1, new Fleshtaker());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        // Only one other creature -> auto-sacrificed as the {1} cost.
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve the {1} ability (+2/+2)
        harness.passBothPriorities(); // resolve the sacrifice trigger -> scry pauses

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(fleshtaker.getPowerModifier()).isEqualTo(2);
        assertThat(fleshtaker.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The {1} ability gives +2/+2 until end of turn")
    void activatedAbilityBoostsSelf() {
        Permanent fleshtaker = addCreatureReady(player1, new Fleshtaker());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve the {1} ability (+2/+2)
        harness.passBothPriorities(); // resolve the sacrifice trigger -> scry pauses
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(fleshtaker.getPowerModifier()).isEqualTo(2);
        assertThat(fleshtaker.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The +2/+2 wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent fleshtaker = addCreatureReady(player1, new Fleshtaker());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve the {1} ability (+2/+2)
        harness.passBothPriorities(); // resolve the sacrifice trigger -> scry pauses
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(fleshtaker.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(fleshtaker.getPowerModifier()).isEqualTo(0);
        assertThat(fleshtaker.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The {1} ability cannot be activated without another creature")
    void cannotActivateWithoutAnotherCreature() {
        addCreatureReady(player1, new Fleshtaker());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrificing a non-creature does not trigger the life gain")
    void nonCreatureSacrificeDoesNotTrigger() {
        addCreatureReady(player1, new Fleshtaker());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        Card artifact = new Card();
        artifact.setName("Thopter Token");
        artifact.setType(CardType.ARTIFACT);
        artifact.setManaCost("");
        artifact.setToken(true);
        Permanent thopter = new Permanent(artifact);
        gd.playerBattlefields.get(player1.getId()).add(thopter);

        gd.playerBattlefields.get(player1.getId()).remove(thopter);
        gd.playerGraveyards.get(player1.getId()).add(thopter.getCard());
        harness.getTriggerCollectionService()
                .checkAllyPermanentSacrificedTriggers(gd, player1.getId(), thopter.getCard());
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
