package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TributaryInstructor.class, FugitiveWizard.class, Shock.class})
class TributaryInstructorTest extends BaseCardTest {

    @Test
    @DisplayName("Mentor targets only an attacking creature with lesser power")
    void mentorTargetsOnlyAttackingCreatureWithLesserPower() {
        addCreatureReady(player1, new TributaryInstructor());
        Permanent attackingWizard = addCreatureReady(player1, new FugitiveWizard());
        Permanent nonAttackingWizard = addCreatureReady(player1, new FugitiveWizard());

        declareAttackers(List.of(0, 1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(attackingWizard.getId());

        harness.handlePermanentChosen(player1, attackingWizard.getId());
        resolveAllTriggers();

        assertThat(attackingWizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonAttackingWizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Draws a card when a creature you control with a +1/+1 counter dies")
    void drawsWhenCreatureWithPlusOneCounterDies() {
        harness.addToBattlefield(player1, new TributaryInstructor());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        dying.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new FugitiveWizard()));

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, dying.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Fugitive Wizard");
    }

    @Test
    @DisplayName("Does not draw when the dying creature has no +1/+1 counter")
    void doesNotDrawWithoutPlusOneCounter() {
        harness.addToBattlefield(player1, new TributaryInstructor());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        harness.setLibrary(player1, List.of(new FugitiveWizard()));

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, dying.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Draws when the Instructor itself dies with a +1/+1 counter")
    void drawsWhenInstructorItselfDiesWithCounter() {
        Permanent instructor = harness.addToBattlefieldAndReturn(player1, new TributaryInstructor());
        instructor.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        instructor.setMarkedDamage(3);
        FugitiveWizard drawnCard = new FugitiveWizard();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, instructor.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(instructor);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Does not draw when an opponent's creature with a +1/+1 counter dies")
    void doesNotDrawForOpponentsCreature() {
        harness.addToBattlefield(player1, new TributaryInstructor());
        Permanent dying = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());
        dying.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new FugitiveWizard()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, dying.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(dying);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Mentor does not target an attacking creature with equal power")
    void mentorExcludesEqualPower() {
        addCreatureReady(player1, new TributaryInstructor());
        Permanent wizard = addCreatureReady(player1, new FugitiveWizard());
        wizard.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(wizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Mentor rechecks lesser power as the ability resolves")
    void mentorRechecksPowerOnResolution() {
        addCreatureReady(player1, new TributaryInstructor());
        Permanent wizard = addCreatureReady(player1, new FugitiveWizard());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, wizard.getId());
        assertThat(gd.stack).hasSize(1);
        wizard.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        resolveAllTriggers();

        assertThat(wizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Mentor uses the Instructor's last known power after it dies")
    void mentorResolvesAfterInstructorDies() {
        Permanent instructor = addCreatureReady(player1, new TributaryInstructor());
        Permanent wizard = addCreatureReady(player1, new FugitiveWizard());
        wizard.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        instructor.setMarkedDamage(3);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, wizard.getId());
        assertThat(gd.stack).hasSize(1);
        harness.castInstant(player1, 0, instructor.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(instructor);
        assertThat(wizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Other kinds of counters do not cause the death ability to draw")
    void doesNotDrawForOtherCounterKinds() {
        harness.addToBattlefield(player1, new TributaryInstructor());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        dying.setCounterCount(CounterType.CHARGE, 1);
        harness.setLibrary(player1, List.of(new FugitiveWizard()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, dying.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dying);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
