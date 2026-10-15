package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkymarkRoc.class, GrizzlyBears.class, HillGiant.class})
class SkymarkRocTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking queues target selection for the bounce trigger")
    void attackQueuesTargetSelection() {
        addReadyRoc();
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
    }

    @Test
    @DisplayName("Accepting bounces the defending player's toughness-2 creature")
    void acceptBouncesTarget() {
        addReadyRoc();
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining leaves the target on the battlefield")
    void declineLeavesTarget() {
        addReadyRoc();
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(bears.getId()).isEqualTo(harness.getPermanentId(player2, "Grizzly Bears"));
    }

    @Test
    @DisplayName("A creature with toughness greater than 2 is not a legal target")
    void toughness3IsIllegal() {
        addReadyRoc();
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent giant = addCreatureReady(player2, new HillGiant());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, giant.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("No trigger when the defender controls no legal targets")
    void noLegalTargetSkipsTrigger() {
        addReadyRoc();

        declareAttackers(List.of(0));

        assertThat(gd.hasPendingInteraction(PermanentChoiceContext.AttackTriggerTarget.class)).isFalse();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("The attacking player's creatures are not legal targets")
    void ownCreatureIsIllegal() {
        addReadyRoc();
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent defendingBears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownBears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, defendingBears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Toughness increasing above 2 before resolution makes the target illegal")
    void increasedToughnessInvalidatesTarget() {
        addReadyRoc();
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The bounce resolves after Skymark Roc leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Permanent roc = addReadyRoc();
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, roc));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Skymark Roc");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A creature controlled by the defender returns to its owner's hand")
    void stolenCreatureReturnsToOwner() {
        addReadyRoc();
        GrizzlyBears card = new GrizzlyBears();
        card.setOwnerId(player1.getId());
        Permanent bears = addCreatureReady(player2, card);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player2, "Grizzly Bears");
    }

    private Permanent addReadyRoc() {
        return addCreatureReady(player1, new SkymarkRoc());
    }
}
