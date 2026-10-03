package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClammyProwler.class, GrizzlyBears.class})
class ClammyProwlerTest extends BaseCardTest {

    @Test
    void attackTriggerTargetsAnotherAttackingCreature() {
        Permanent prowler = addCreatureReady(player1, new ClammyProwler());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(attacker.getId())
                .doesNotContain(prowler.getId(), nonattacker.getId());

        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.isCantBeBlocked()).isTrue();
        assertThat(prowler.isCantBeBlocked()).isFalse();
    }

    @Test
    void doesNotTriggerWhenThereIsNoOtherAttackingCreature() {
        Permanent prowler = addCreatureReady(player1, new ClammyProwler());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(prowler.isCantBeBlocked()).isFalse();
    }

    @Test
    void unblockableWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new ClammyProwler());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.isCantBeBlocked()).isFalse();
    }

    @Test
    void doesNotTriggerWhenProwlerDoesNotAttack() {
        Permanent prowler = addCreatureReady(player1, new ClammyProwler());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(attacker.isCantBeBlocked()).isFalse();
        assertThat(prowler.isCantBeBlocked()).isFalse();
    }

    @Test
    void targetRemovedFromCombatBeforeResolutionIsNotMadeUnblockable() {
        addCreatureReady(player1, new ClammyProwler());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.isCantBeBlocked()).isFalse();
    }

    @Test
    void triggerResolvesAfterProwlerLeavesBattlefield() {
        Permanent prowler = addCreatureReady(player1, new ClammyProwler());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        gd.playerBattlefields.get(player1.getId()).remove(prowler);
        gd.playerGraveyards.get(player1.getId()).add(prowler.getCard());
        harness.passBothPriorities();

        assertThat(attacker.isCantBeBlocked()).isTrue();
    }
}