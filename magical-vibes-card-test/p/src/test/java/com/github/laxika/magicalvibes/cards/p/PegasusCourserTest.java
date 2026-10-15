package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({PegasusCourser.class, GrizzlyBears.class})
class PegasusCourserTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with Pegasus Courser queues target selection for another attacking creature")
    void attackTriggerQueuesForTargetSelection() {
        addCreatureReady(player1, new PegasusCourser());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Targeted attacking creature gains flying until end of turn")
    void targetedCreatureGainsFlying() {
        addCreatureReady(player1, new PegasusCourser());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        // Choose bears as target
        harness.handlePermanentChosen(player1, bears.getId());

        // Resolve the triggered ability
        harness.passBothPriorities();

        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Cannot target Pegasus Courser itself (another restriction)")
    void cannotTargetItself() {
        Permanent courser = addCreatureReady(player1, new PegasusCourser());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        // Choosing Pegasus Courser itself should fail
        assertThatThrownBy(
                () -> harness.handlePermanentChosen(player1, courser.getId())
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("No trigger when attacking alone (no valid targets)")
    void noTriggerWhenAttackingAlone() {
        addCreatureReady(player1, new PegasusCourser());

        declareAttackers(player1, List.of(0));

        // Should not be awaiting permanent choice since there are no other attacking creatures
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        addCreatureReady(player1, new PegasusCourser());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent stayBack = addCreatureReady(player1, new GrizzlyBears());

        // Only courser (index 0) and attacker (index 1) attack; stayBack (index 2) stays back
        declareAttackers(player1, List.of(0, 1));

        // Choosing the non-attacking creature should fail
        assertThatThrownBy(
                () -> harness.handlePermanentChosen(player1, stayBack.getId())
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attack trigger puts triggered ability on the stack")
    void attackPutsTriggeredAbilityOnStack() {
        addCreatureReady(player1, new PegasusCourser());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        // Choose bears as target
        harness.handlePermanentChosen(player1, bears.getId());

        // Triggered ability should be on the stack
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.stream()
                .anyMatch(entry -> entry.getCard().getName().equals("Pegasus Courser")))
                .isTrue();
    }

    @Test
    @DisplayName("Granted flying expires at end of turn")
    void flyingExpiresAtEndOfTurn() {
        Permanent courser = addCreatureReady(player1, new PegasusCourser());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, courser, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Trigger does not grant flying if its target stops attacking before resolution")
    void targetMustStillBeAttackingOnResolution() {
        addCreatureReady(player1, new PegasusCourser());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, bears.getId());
        bears.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Attack trigger resolves after Pegasus Courser leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Permanent courser = addCreatureReady(player1, new PegasusCourser());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(courser);
        gd.playerGraveyards.get(player1.getId()).add(courser.getCard());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }
}
