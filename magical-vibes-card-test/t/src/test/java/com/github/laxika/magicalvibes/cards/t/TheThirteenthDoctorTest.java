package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({TheThirteenthDoctor.class, GrizzlyBears.class})
class TheThirteenthDoctorTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell from outside the hand puts a +1/+1 counter on a target creature")
    void outsideHandSpellPutsCounterOnTargetCreature() {
        harness.addToBattlefield(player1, new TheThirteenthDoctor());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GrizzlyBears spell = new GrizzlyBears();
        gd.addToExile(player1.getId(), spell);
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, spell.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a spell from hand does not trigger Paradox")
    void handSpellDoesNotTriggerParadox() {
        harness.addToBattlefield(player1, new TheThirteenthDoctor());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Paradox can target creatures but not players")
    void paradoxCannotTargetPlayer() {
        harness.addToBattlefield(player1, new TheThirteenthDoctor());
        GrizzlyBears spell = new GrizzlyBears();
        gd.addToExile(player1.getId(), spell);
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castFromExile(player1, spell.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Team TARDIS untaps only creatures with counters at your end step")
    void untapsOnlyCounteredCreaturesAtEndStep() {
        harness.addToBattlefield(player1, new TheThirteenthDoctor());
        Permanent counteredCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent uncounteredCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        counteredCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        counteredCreature.tap();
        uncounteredCreature.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(counteredCreature.isTapped()).isFalse();
        assertThat(uncounteredCreature.isTapped()).isTrue();
    }

    @Test
    void paradoxCanTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new TheThirteenthDoctor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TheThirteenthDoctor());
        GrizzlyBears spell = new GrizzlyBears();
        harness.setExile(player1, List.of(spell));
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castFromExile(player1, spell.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void opponentsOutsideHandSpellDoesNotTriggerParadox() {
        Permanent doctor = harness.addToBattlefieldAndReturn(player1, new TheThirteenthDoctor());
        GrizzlyBears spell = new GrizzlyBears();
        harness.setExile(player2, List.of(spell));
        gd.exilePlayPermissions.put(spell.getId(), player2.getId());
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player2);

        harness.castFromExile(player2, spell.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        resolveAllTriggers();

        assertThat(doctor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void teamTardisUntapsOtherCounterTypesButOnlyControlledCreatures() {
        Permanent doctor = harness.addToBattlefieldAndReturn(player1, new TheThirteenthDoctor());
        Permanent opposingDoctor = harness.addToBattlefieldAndReturn(player2, new TheThirteenthDoctor());
        doctor.setCounterCount(CounterType.CHARGE, 1);
        opposingDoctor.setCounterCount(CounterType.CHARGE, 1);
        doctor.tap();
        opposingDoctor.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(doctor.isTapped()).isFalse();
        assertThat(opposingDoctor.isTapped()).isTrue();
    }

    @Test
    void teamTardisChecksCountersWhenTheAbilityResolves() {
        harness.addToBattlefield(player1, new TheThirteenthDoctor());
        Permanent gainingCounter = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent losingCounter = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        losingCounter.setCounterCount(CounterType.CHARGE, 1);
        gainingCounter.tap();
        losingCounter.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        gainingCounter.setCounterCount(CounterType.CHARGE, 1);
        losingCounter.setCounterCount(CounterType.CHARGE, 0);
        resolveAllTriggers();

        assertThat(gainingCounter.isTapped()).isFalse();
        assertThat(losingCounter.isTapped()).isTrue();
    }
}
