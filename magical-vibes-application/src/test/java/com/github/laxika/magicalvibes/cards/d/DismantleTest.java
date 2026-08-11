package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.j.JalumTome;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DismantleTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the target artifact and puts its total counters on a controlled artifact")
    void destroysArtifactAndPutsPlusOnePlusOneCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JalumTome());
        target.setCounterCount(CounterType.FUSE, 2);
        target.setCounterCount(CounterType.TIME, 1);
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        castDismantle(target);

        harness.assertInGraveyard(player2, "Jalum Tome");
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.context()).isInstanceOf(ChoiceContext.DismantleCounterTypeChoice.class);
        assertThat(choice.options()).containsExactly(
                ChoiceContext.DismantleCounterTypeChoice.PLUS_ONE_PLUS_ONE,
                ChoiceContext.DismantleCounterTypeChoice.CHARGE);

        harness.handleListChoice(player1, ChoiceContext.DismantleCounterTypeChoice.PLUS_ONE_PLUS_ONE);

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Lets the controller choose a controlled artifact for charge counters")
    void choosesChargeCounterRecipient() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JalumTome());
        target.setCounterCount(CounterType.CHARGE, 2);
        Permanent firstRecipient = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent secondRecipient = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        castDismantle(target);
        harness.handleListChoice(player1, ChoiceContext.DismantleCounterTypeChoice.CHARGE);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();
        harness.handleMultiplePermanentsChosen(player1, List.of(secondRecipient.getId()));

        assertThat(firstRecipient.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(secondRecipient.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Puts counters on an artifact even when the target is indestructible")
    void putsCountersWhenTargetSurvivesDestruction() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        target.setCounterCount(CounterType.TIME, 2);
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        castDismantle(target);
        harness.handleListChoice(player1, ChoiceContext.DismantleCounterTypeChoice.CHARGE);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(recipient.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Destroys an artifact with no counters without asking for a counter choice")
    void noCountersNeedsNoFollowUpChoice() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JalumTome());

        castDismantle(target);

        harness.assertInGraveyard(player2, "Jalum Tome");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a nonartifact permanent")
    void cannotTargetNonartifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Dismantle()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castDismantle(Permanent target) {
        harness.setHand(player1, List.of(new Dismantle()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
