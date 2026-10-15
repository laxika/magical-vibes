package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FellBeastOfMordor.class, GrizzlyBears.class})
class FellBeastOfMordorTest extends BaseCardTest {

    @Test
    @DisplayName("Devour 1 adds one counter per sacrificed creature and drains the target opponent")
    void devourAndEnterTrigger() {
        Permanent fodderA = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent fodderB = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FellBeastOfMordor()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(fodderA.getId(), fodderB.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        Permanent beast = findPermanent(player1, "Fell Beast of Mordor");
        assertThat(beast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fodderA, fodderB);
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Attack trigger drains for the current number of +1/+1 counters")
    void attackTriggerUsesCounters() {
        Permanent beast = addCreatureReady(player1, new FellBeastOfMordor());
        beast.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        assertThat(gd.getLife(player2.getId())).isEqualTo(11);
    }
}
