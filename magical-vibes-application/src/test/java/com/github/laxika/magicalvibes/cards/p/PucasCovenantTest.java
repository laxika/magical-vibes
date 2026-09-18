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

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, dying));
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

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

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, dying));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Puca's Covenant triggers only once each turn")
    void triggersOnlyOnceEachTurn() {
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

        kill(secondDying);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(secondTarget);
    }

    private void kill(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }
}
