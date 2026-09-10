package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PoliticalTriumph.class, GrizzlyBears.class})
class PoliticalTriumphTest extends BaseCardTest {

    @Test
    void scriesAndAddsPlanCounterWhenCreatureEnters() {
        Permanent triumph = addTriumph();
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(triumph.getCounterCount(CounterType.PLAN)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void fourthPlanCounterSacrificesDrawsAndPutsCountersOnControlledCreatures() {
        Permanent triumph = addTriumph();
        triumph.setCounterCount(CounterType.PLAN, 3);
        Permanent existingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        Permanent enteringCreature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(triumph.getCounterCount(CounterType.PLAN)).isEqualTo(4);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(triumph);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(triumph.getCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard);
        assertThat(existingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(enteringCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addTriumph() {
        return harness.addToBattlefieldAndReturn(player1, new PoliticalTriumph());
    }
}
