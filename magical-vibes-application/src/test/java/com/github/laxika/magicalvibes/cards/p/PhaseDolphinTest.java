package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
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

@CardUsed({PhaseDolphin.class, AlmightyBrushwagg.class})
class PhaseDolphinTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking makes another attacking creature unblockable")
    void makesAnotherAttackingCreatureUnblockable() {
        Permanent dolphin = addCreatureReady(player1, new PhaseDolphin());
        Permanent brushwagg = addCreatureReady(player1, new AlmightyBrushwagg());

        declareAttackers(List.of(0, 1));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, brushwagg.getId());
        resolveAllTriggers();

        assertThat(brushwagg.isCantBeBlocked()).isTrue();
        assertThat(dolphin.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The attack trigger cannot target Phase Dolphin itself")
    void cannotTargetItself() {
        Permanent dolphin = addCreatureReady(player1, new PhaseDolphin());
        addCreatureReady(player1, new AlmightyBrushwagg());

        declareAttackers(List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, dolphin.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The attack trigger cannot target a creature that did not attack")
    void cannotTargetNonattackingCreature() {
        addCreatureReady(player1, new PhaseDolphin());
        Permanent attacker = addCreatureReady(player1, new AlmightyBrushwagg());
        Permanent nonattacker = addCreatureReady(player1, new AlmightyBrushwagg());

        declareAttackers(List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonattacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAllTriggers();

        assertThat(attacker.isCantBeBlocked()).isTrue();
        assertThat(nonattacker.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Attacking alone has no legal target and does not make the Dolphin unblockable")
    void attackingAloneHasNoLegalTarget() {
        Permanent dolphin = addCreatureReady(player1, new PhaseDolphin());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.stack).isEmpty();
        assertThat(dolphin.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Another creature attacking does not trigger a Dolphin that stays back")
    void doesNotTriggerWhenDolphinDoesNotAttack() {
        Permanent dolphin = addCreatureReady(player1, new PhaseDolphin());
        Permanent attacker = addCreatureReady(player1, new AlmightyBrushwagg());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.stack).isEmpty();
        assertThat(dolphin.isCantBeBlocked()).isFalse();
        assertThat(attacker.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Unblockable wears off at end of turn cleanup")
    void unblockableResetsAtEndOfTurn() {
        addCreatureReady(player1, new PhaseDolphin());
        Permanent brushwagg = addCreatureReady(player1, new AlmightyBrushwagg());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, brushwagg.getId());
        resolveAllTriggers();

        assertThat(brushwagg.isCantBeBlocked()).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(brushwagg.isCantBeBlocked()).isFalse();
    }
}
