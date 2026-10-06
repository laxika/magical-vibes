package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RhoxVeteran.class, GrizzlyBears.class})
class RhoxVeteranTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking taps a target creature an opponent controls")
    void attackTriggerTapsOpponentCreature() {
        addReadyVeteran(player1);
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);

        harness.handlePermanentChosen(player1, opponentBears.getId());
        harness.passBothPriorities();

        assertThat(opponentBears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The attack trigger cannot target a creature I control")
    void attackTriggerRejectsOwnCreature() {
        addReadyVeteran(player1);
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownBears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");

        harness.handlePermanentChosen(player1, opponentBears.getId());
        harness.passBothPriorities();

        assertThat(ownBears.isTapped()).isFalse();
        assertThat(opponentBears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Battle cry gives other attacking creatures +1/+0")
    void battleCryBoostsOtherAttackers() {
        addReadyVeteran(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("No attack target interaction occurs when the opponent controls no creatures")
    void noInteractionWithoutLegalTarget() {
        addReadyVeteran(player1);

        declareAttackers(List.of(0));

        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
    }

    @Test
    @DisplayName("Battle cry excludes its source and nonattackers and lasts until end of turn")
    void battleCryExclusionsAndDuration() {
        Permanent veteran = addCreatureReady(player1, new RhoxVeteran());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonattacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(veteran.getEffectivePower()).isEqualTo(2);
        assertThat(attacker.getEffectivePower()).isEqualTo(3);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(nonattacker.getEffectivePower()).isEqualTo(2);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(attacker.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("An already tapped opposing creature is a legal attack-trigger target")
    void attackTriggerAcceptsAlreadyTappedCreature() {
        addReadyVeteran(player1);
        Permanent target = addCreatureReady(player2, new RhoxVeteran());
        target.tap();

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Losing the tap target does not stop the separate battle cry trigger")
    void battleCryResolvesWhenTapTargetLeaves() {
        addReadyVeteran(player1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new RhoxVeteran());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToHand(gd, target));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(target.isTapped()).isFalse();
        assertThat(attacker.getEffectivePower()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    private void addReadyVeteran(Player player) {
        addCreatureReady(player, new RhoxVeteran());
    }
}
