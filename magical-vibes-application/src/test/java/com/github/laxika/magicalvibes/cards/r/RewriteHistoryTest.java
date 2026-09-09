package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
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

@CardUsed({RewriteHistory.class, LlanowarElves.class, GrizzlyBears.class, HolyDay.class, Divination.class})
class RewriteHistoryTest extends BaseCardTest {

    @Test
    @DisplayName("Loots and puts a plan counter on itself when a creature becomes tapped")
    void lootsAndAddsPlanCounter() {
        Permanent rewriteHistory = harness.addToBattlefieldAndReturn(player1, new RewriteHistory());
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        Card discarded = new GrizzlyBears();
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));

        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(elves));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(rewriteHistory.getCounterCount(CounterType.PLAN)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Sacrifices itself at four plan counters and returns up to two instants or sorceries")
    void sacrificesAtFourCountersAndReturnsSpells() {
        Permanent rewriteHistory = harness.addToBattlefieldAndReturn(player1, new RewriteHistory());
        rewriteHistory.setCounterCount(CounterType.PLAN, 3);
        Permanent elves = addCreatureReady(player1, new LlanowarElves());
        Card instant = new HolyDay();
        Card sorcery = new Divination();
        Card nonSpell = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(instant, sorcery, nonSpell));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(elves));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(instant.getId(), sorcery.getId());

        harness.handleMultipleCardsChosen(player1, List.of(instant.getId(), sorcery.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getCard)
                .doesNotContain(rewriteHistory.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(rewriteHistory.getCard(), nonSpell);
        assertThat(gd.playerHands.get(player1.getId())).contains(instant, sorcery);
    }
}
