package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScrapTrawler;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PucasCovenant.class, GrizzlyBears.class, ScrapTrawler.class, Shock.class})
class PucasCovenantTest extends BaseCardTest {

    @Test
    @DisplayName("A creature with counters lets you return another permanent card within its counter limit")
    void returnsAnotherPermanentWithinCounterLimit() {
        harness.addToBattlefield(player1, new PucasCovenant());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        dying.setCounterCount(CounterType.CHARGE, 2);
        Card eligible = new GrizzlyBears();
        Card tooExpensive = new ScrapTrawler();
        Card nonPermanent = new Shock();
        harness.setGraveyard(player1, List.of(eligible, tooExpensive, nonPermanent));

        kill(dying);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Scrap Trawler");
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("A creature without counters does not trigger Puca's Covenant")
    void doesNotTriggerWithoutCounters() {
        harness.addToBattlefield(player1, new PucasCovenant());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        kill(dying);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Puca's Covenant stops triggering after returning a card that turn")
    void stopsTriggeringAfterReturningCard() {
        harness.addToBattlefield(player1, new PucasCovenant());
        Permanent firstDying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        firstDying.setCounterCount(CounterType.CHARGE, 2);
        Permanent secondDying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        secondDying.setCounterCount(CounterType.CHARGE, 2);
        Card firstTarget = new GrizzlyBears();
        Card secondTarget = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(firstTarget, secondTarget));

        kill(firstDying);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(firstTarget.getId(), secondTarget.getId());
        harness.handleMultipleCardsChosen(player1, List.of(firstTarget.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        kill(secondDying);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(secondTarget);
    }

    @Test
    void mayDeclineReturnWhenAbilityResolves() {
        harness.addToBattlefield(player1, new PucasCovenant());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        dying.setCounterCount(CounterType.CHARGE, 2);
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        kill(dying);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target);
        harness.assertNotInHand(player1, "Grizzly Bears");

        Permanent nextDying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        nextDying.setCounterCount(CounterType.CHARGE, 2);
        kill(nextDying);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
    }

    @Test
    void deathWithoutEligibleTargetDoesNotConsumeReturnForTurn() {
        harness.addToBattlefield(player1, new PucasCovenant());
        Permanent firstDying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        firstDying.setCounterCount(CounterType.CHARGE, 1);
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        kill(firstDying);
        assertThat(gd.interaction.activeInteraction()).isNull();

        Permanent secondDying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        secondDying.setCounterCount(CounterType.CHARGE, 2);
        kill(secondDying);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(target.getId(), firstDying.getCard().getId())
                .doesNotContain(secondDying.getCard().getId());
    }

    @Test
    void countsCountersOfDifferentTypesAndAllowsNoncreaturePermanents() {
        harness.addToBattlefield(player1, new PucasCovenant());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        dying.setCounterCount(CounterType.CHARGE, 1);
        dying.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Card target = new PucasCovenant();
        harness.setGraveyard(player1, List.of(target));

        kill(dying);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(target.getId());
    }

    @Test
    void opponentsCreatureDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new PucasCovenant());
        Permanent dying = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        dying.setCounterCount(CounterType.CHARGE, 2);
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));

        kill(dying);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target);
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    private void kill(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }
}
