package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GoblinCohort;
import com.github.laxika.magicalvibes.cards.i.IchigaWhoTopplesOaks;
import com.github.laxika.magicalvibes.cards.k.KamiOfFalseHope;
import com.github.laxika.magicalvibes.cards.v.VitalSurge;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BudokaPupil.class, IchigaWhoTopplesOaks.class, VitalSurge.class,
        KamiOfFalseHope.class, GoblinCohort.class})
class BudokaPupilTest extends BaseCardTest {

    @Test
    @DisplayName("May put a ki counter on itself when an Arcane spell is cast")
    void arcaneSpellAddsKiCounterWhenAccepted() {
        Permanent pupil = addPupil(player1);
        prepareMainPhase();
        harness.setHand(player1, List.of(new VitalSurge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(pupil.getCounterCount(CounterType.KI)).isEqualTo(1);
    }

    @Test
    @DisplayName("May put a ki counter on itself when a Spirit spell is cast")
    void spiritSpellAddsKiCounterWhenAccepted() {
        Permanent pupil = addPupil(player1);
        prepareMainPhase();
        harness.setHand(player1, List.of(new KamiOfFalseHope()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(pupil.getCounterCount(CounterType.KI)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the spell-cast trigger does not put on a ki counter")
    void decliningSpellCastTriggerPlacesNoCounter() {
        Permanent pupil = addPupil(player1);
        prepareMainPhase();
        harness.setHand(player1, List.of(new VitalSurge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(pupil.getCounterCount(CounterType.KI)).isZero();
    }

    @Test
    @DisplayName("A spell that is neither Spirit nor Arcane does not trigger")
    void unrelatedSpellDoesNotTrigger() {
        Permanent pupil = addPupil(player1);
        prepareMainPhase();
        harness.setHand(player1, List.of(new GoblinCohort()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(pupil.getCounterCount(CounterType.KI)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The spell-cast may choice waits until the triggered ability resolves")
    void spellCastMayChoiceWaitsForTriggerResolution() {
        addPupil(player1);
        prepareMainPhase();
        harness.setHand(player1, List.of(new VitalSurge()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() != null
                && entry.getCard().getName().equals("Budoka Pupil"));
    }

    @Test
    @DisplayName("An opponent casting a Spirit spell does not trigger it")
    void opponentCastingSpiritDoesNotTrigger() {
        Permanent pupil = addPupil(player1);
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new KamiOfFalseHope()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(pupil.getCounterCount(CounterType.KI)).isZero();
    }

    @Test
    @DisplayName("Flips at the end step when it has two ki counters and the choice is accepted")
    void flipsAtEndStepWithTwoKiCounters() {
        Permanent pupil = addPupil(player1);
        pupil.setCounterCount(CounterType.KI, 2);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(pupil.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Flips during an opponent's end step when the controller accepts")
    void flipsAtOpponentsEndStepWithTwoKiCounters() {
        Permanent pupil = addPupil(player1);
        pupil.setCounterCount(CounterType.KI, 2);

        advanceToEndStep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(pupil.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Declining the end-step trigger leaves it unflipped")
    void decliningEndStepTriggerLeavesUnflipped() {
        Permanent pupil = addPupil(player1);
        pupil.setCounterCount(CounterType.KI, 2);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(pupil.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Does not flip at the end step with fewer than two ki counters")
    void doesNotFlipBelowTwoKiCounters() {
        Permanent pupil = addPupil(player1);
        pupil.setCounterCount(CounterType.KI, 1);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(pupil.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Does not flip if the ki counters fall below two before resolution")
    void doesNotFlipIfCountersFallBelowTwoBeforeResolution() {
        Permanent pupil = addPupil(player1);
        pupil.setCounterCount(CounterType.KI, 2);

        advanceToEndStep(player1);
        pupil.setCounterCount(CounterType.KI, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(pupil.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Ichiga removes a ki counter to give a target creature +2/+2")
    void ichigaBoostsTargetForKiCounter() {
        Permanent ichiga = addFlippedPupil();
        ichiga.setCounterCount(CounterType.KI, 1);
        Permanent goblin = addCreatureReady(player1, new GoblinCohort());

        prepareMainPhase();
        harness.activateAbility(player1, 0, 0, null, goblin.getId());
        harness.passBothPriorities();

        assertThat(ichiga.getCounterCount(CounterType.KI)).isZero();
        assertThat(goblin.getEffectivePower()).isEqualTo(4);
        assertThat(goblin.getEffectiveToughness()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.CLEANUP);

        assertThat(goblin.getEffectivePower()).isEqualTo(2);
        assertThat(goblin.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ichiga cannot activate without a ki counter")
    void ichigaRequiresKiCounter() {
        Permanent ichiga = addFlippedPupil();
        ichiga.setCounterCount(CounterType.KI, 0);
        Permanent goblin = addCreatureReady(player1, new GoblinCohort());
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, goblin.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addPupil(Player player) {
        return addCreatureReady(player, new BudokaPupil());
    }

    private Permanent addFlippedPupil() {
        BudokaPupil card = new BudokaPupil();
        Permanent pupil = addCreatureReady(player1, card);
        pupil.setCard(card.getBackFaceCard());
        pupil.setTransformed(true);
        pupil.setCounterCount(CounterType.KI, 2);
        return pupil;
    }

    private void prepareMainPhase() {
        prepareMainPhase(player1);
    }

    private void prepareMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
