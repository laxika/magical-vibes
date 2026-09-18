package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MysticBarrier.class, GrizzlyBears.class})
class MysticBarrierTest extends BaseCardTest {

    @Test
    @DisplayName("ETB direction limits attacks to the nearest opponent in that direction")
    void etbDirectionLimitsAttackTargets() {
        UUID player3Id = addThirdPlayer();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Card planeswalkerCard = new Card();
        planeswalkerCard.setName("Test Planeswalker");
        planeswalkerCard.setType(CardType.PLANESWALKER);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, planeswalkerCard);
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.enterBattlefieldAndReturn(player1, new MysticBarrier());

        harness.passBothPriorities();
        harness.handleListChoice(player1, "Right");
        harness.passBothPriorities();

        assertThat(als.canAttackDefender(gd, attacker, player2.getId())).isTrue();
        assertThat(als.canAttackDefender(gd, attacker, planeswalker.getId())).isTrue();
        assertThat(als.canAttackDefender(gd, attacker, player3Id)).isFalse();
    }

    private UUID addThirdPlayer() {
        UUID player3Id = UUID.randomUUID();
        gd.playerIds.add(player3Id);
        gd.orderedPlayerIds.add(player3Id);
        return player3Id;
    }
}
