package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TormentOfVenom;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OminousSphinx.class, Censor.class, GrizzlyBears.class, TormentOfVenom.class})
class OminousSphinxTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling a card queues a target-creature choice for the -2/-0")
    void cyclingQueuesTargetChoice() {
        harness.addToBattlefield(player1, new OminousSphinx());
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        // Discarding Censor to pay its cycling cost triggers the -2/-0 once.
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.DiscardControllerTriggerTarget.class);
    }

    @Test
    @DisplayName("Resolving the trigger gives -2/-0 to the chosen opponent creature")
    void appliesMinusTwoZeroToOpponentCreature() {
        harness.addToBattlefield(player1, new OminousSphinx());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities(); // discard trigger awaits target

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities(); // resolve the -2/-0

        assertThat(bears.getPowerModifier()).isEqualTo(-2);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(bears.getEffectivePower()).isEqualTo(0);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The -2/-0 wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new OminousSphinx());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(-2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The trigger can only target a creature an opponent controls, not your own")
    void cannotTargetYourOwnCreature() {
        harness.addToBattlefield(player1, new OminousSphinx());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities(); // discard trigger awaits target

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The trigger is skipped when no opponent creature is on the battlefield")
    void skippedWhenNoOpponentCreature() {
        harness.addToBattlefield(player1, new OminousSphinx());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()); // only your own creature
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.DiscardControllerTriggerTarget.class)).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Discarding without cycling triggers the power reduction")
    void ordinaryDiscardTriggers() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new OminousSphinx());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Censor()));
        harness.setHand(player2, List.of(new TormentOfVenom()));
        harness.addMana(player2, ManaColor.BLACK, 4);

        harness.castInstant(player2, 0, sphinx.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, ChoiceContext.TormentPenaltyChoice.DISCARD);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Censor");
        assertThat(bears.getPowerModifier()).isEqualTo(-2);
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An opponent cycling a card does not trigger Ominous Sphinx")
    void opponentCyclingDoesNotTrigger() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new OminousSphinx());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Censor()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateHandAbility(player2, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Censor");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.DiscardControllerTriggerTarget.class)).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(sphinx.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Two Ominous Sphinxes each trigger once from one cycled card")
    void eachSphinxTriggersOncePerCycle() {
        harness.addToBattlefield(player1, new OminousSphinx());
        harness.addToBattlefield(player1, new OminousSphinx());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(-4);
        assertThat(bears.getToughnessModifier()).isZero();
        assertThat(bears.getEffectivePower()).isEqualTo(-2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }
}
