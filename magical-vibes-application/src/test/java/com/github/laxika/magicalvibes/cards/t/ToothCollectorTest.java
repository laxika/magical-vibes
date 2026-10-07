package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ToothCollector.class, GrizzlyBears.class, Forest.class, Naturalize.class, Pacifism.class})
class ToothCollectorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield and gives a target creature an opposing -1/-1")
    void etbGivesTargetCreatureMinusOneMinusOne() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ToothCollector()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Delirium gives a target creature -1/-1 during an opponent's upkeep")
    void deliriumGivesTargetCreatureMinusOneMinusOneOnOpponentsUpkeep() {
        setDelirium();
        addCreatureReady(player1, new ToothCollector());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger on an opponent's upkeep without delirium")
    void doesNotTriggerWithoutDelirium() {
        addCreatureReady(player1, new ToothCollector());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature controlled by the Tooth Collector controller on upkeep")
    void cannotTargetOwnCreatureOnOpponentsUpkeep() {
        setDelirium();
        addCreatureReady(player1, new ToothCollector());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player2);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(1);
    }

    @Test
    @DisplayName("Rechecks delirium when the upkeep ability resolves")
    void rechecksDeliriumOnResolution() {
        setDelirium();
        addCreatureReady(player1, new ToothCollector());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerGraveyards.get(player1.getId()).removeLast();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The reduction wears off at end of turn")
    void reductionWearsOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ToothCollector()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0, 0, target.getId());
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Delirium does not trigger during its controller's upkeep")
    void doesNotTriggerOnOwnUpkeep() {
        setDelirium();
        addCreatureReady(player1, new ToothCollector());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Four cards with only three card types do not enable delirium")
    void countsDistinctTypesRatherThanCards() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new Forest(), new Naturalize()));
        addCreatureReady(player1, new ToothCollector());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's graveyard does not enable delirium")
    void doesNotCountOpponentsGraveyard() {
        harness.setGraveyard(player2, List.of(
                new GrizzlyBears(), new Forest(), new Naturalize(), new Pacifism()));
        addCreatureReady(player1, new ToothCollector());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Naturalize(), new Pacifism()));
    }
}
