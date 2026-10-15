package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HemosymbicMite.class, GrizzlyBears.class})
class HemosymbicMiteTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming tapped targets another creature you control")
    void becomingTappedTargetsAnotherOwnCreature() {
        Permanent mite = addCreatureReady(player1, new HemosymbicMite());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds())
                .contains(ownCreature.getId())
                .doesNotContain(mite.getId(), opposingCreature.getId());
    }

    @Test
    @DisplayName("Becoming tapped boosts the target by the Mite's current power")
    void becomingTappedBoostsByCurrentPower() {
        addCreatureReady(player1, new HemosymbicMite()).setPowerModifier(2);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new HemosymbicMite());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Power is evaluated when the triggered ability resolves")
    void usesPowerAtResolution() {
        Permanent mite = addCreatureReady(player1, new HemosymbicMite());
        Permanent target = addCreatureReady(player1, new HemosymbicMite());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        mite.setPowerModifier(3);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    @DisplayName("Another creature becoming tapped does not trigger the Mite")
    void anotherCreatureBecomingTappedDoesNotTrigger() {
        addCreatureReady(player1, new HemosymbicMite());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("With no other creature to target the ability does not remain on the stack")
    void noLegalTarget() {
        addCreatureReady(player1, new HemosymbicMite());
        addCreatureReady(player2, new HemosymbicMite());

        declareAttackers(List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Negative source power produces a zero boost")
    void negativePowerProducesZeroBoost() {
        Permanent mite = addCreatureReady(player1, new HemosymbicMite());
        Permanent target = addCreatureReady(player1, new HemosymbicMite());
        target.setToughnessModifier(3);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        mite.setPowerModifier(-3);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }
}
