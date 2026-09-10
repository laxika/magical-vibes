package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LowlandTracker.class, GrizzlyBears.class})
class LowlandTrackerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers a defending player's creature as a provoke target")
    void attackingOffersDefendingCreatureAsTarget() {
        Permanent tracker = addReadyCreature(player1, new LowlandTracker());
        Permanent defendingCreature = addReadyCreature(player2, new GrizzlyBears());
        Permanent ownCreature = addReadyCreature(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(defendingCreature.getId())
                .doesNotContain(tracker.getId(), ownCreature.getId());
    }

    @Test
    @DisplayName("Accepting provoke untaps the target and requires it to block")
    void acceptingUntapsAndRequiresBlock() {
        Permanent tracker = addReadyCreature(player1, new LowlandTracker());
        Permanent defendingCreature = addReadyCreature(player2, new GrizzlyBears());
        defendingCreature.tap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, defendingCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(defendingCreature.isTapped()).isFalse();
        assertThat(defendingCreature.getMustBlockIds()).containsExactly(tracker.getId());

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        assertThat(defendingCreature.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Declining provoke leaves the target untapped state and block requirements unchanged")
    void decliningDoesNothing() {
        Permanent tracker = addReadyCreature(player1, new LowlandTracker());
        Permanent defendingCreature = addReadyCreature(player2, new GrizzlyBears());
        defendingCreature.tap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, defendingCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(defendingCreature.isTapped()).isTrue();
        assertThat(defendingCreature.getMustBlockIds()).isEmpty();
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player,
                                       com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
