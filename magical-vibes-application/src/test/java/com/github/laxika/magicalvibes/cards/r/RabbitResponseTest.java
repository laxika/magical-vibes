package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BraveKinDuo;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RabbitResponse.class, GrizzlyBears.class, BraveKinDuo.class})
class RabbitResponseTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts own creatures without scrying when no Rabbit is controlled")
    void boostsWithoutRabbit() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new RabbitResponse(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(4);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Boosts own creatures and scries 2 when a Rabbit is controlled")
    void boostsAndScriesWithRabbit() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new BraveKinDuo());
        harness.castFromHand(player1, new RabbitResponse(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectivePower()).isEqualTo(4);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);

        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Rabbit does not enable scry")
    void opponentRabbitDoesNotEnableScry() {
        Permanent rabbit = harness.addToBattlefieldAndReturn(player2, new BraveKinDuo());

        harness.castFromHand(player1, new RabbitResponse(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThat(rabbit.getEffectivePower()).isEqualTo(1);
        assertThat(rabbit.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Rabbit arriving before resolution enables scry and receives the boost")
    void rabbitArrivingBeforeResolutionEnablesScry() {
        RabbitResponse first = new RabbitResponse();
        BraveKinDuo second = new BraveKinDuo();
        RabbitResponse third = new RabbitResponse();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.castFromHand(player1, new RabbitResponse(), "{2}{W}{W}");
        Permanent rabbit = harness.addToBattlefieldAndReturn(player1, new BraveKinDuo());

        harness.passBothPriorities();

        assertThat(rabbit.getEffectivePower()).isEqualTo(3);
        assertThat(rabbit.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Losing the only Rabbit before resolution prevents scry but not the boost")
    void losingRabbitBeforeResolutionPreventsScry() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent rabbit = harness.addToBattlefieldAndReturn(player1, new BraveKinDuo());
        harness.castFromHand(player1, new RabbitResponse(), "{2}{W}{W}");
        gd.playerBattlefields.get(player1.getId()).remove(rabbit);
        gd.playerGraveyards.get(player1.getId()).add(rabbit.getCard());

        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The boost excludes later arrivals and expires at end of turn")
    void boostExcludesLaterArrivalsAndExpires() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new RabbitResponse(), "{2}{W}{W}");
        harness.passBothPriorities();
        Permanent lateRabbit = harness.addToBattlefieldAndReturn(player1, new BraveKinDuo());

        assertThat(creature.getEffectivePower()).isEqualTo(4);
        assertThat(creature.getEffectiveToughness()).isEqualTo(3);
        assertThat(lateRabbit.getEffectivePower()).isEqualTo(1);
        assertThat(lateRabbit.getEffectiveToughness()).isEqualTo(1);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Scry handles a library with only one card")
    void scriesWithOneCardLibrary() {
        harness.addToBattlefield(player1, new BraveKinDuo());
        RabbitResponse remaining = new RabbitResponse();
        harness.setLibrary(player1, List.of(remaining));

        harness.castFromHand(player1, new RabbitResponse(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(remaining);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
