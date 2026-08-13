package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SetessanOathswornTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets Setessan Oathsworn puts two +1/+1 counters on it")
    void castingSpellThatTargetsOathswornTriggersHeroic() {
        harness.addToBattlefield(player1, new SetessanOathsworn());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        UUID oathswornId = harness.getPermanentId(player1, "Setessan Oathsworn");
        harness.castInstant(player1, 0, oathswornId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent oathsworn = findPermanent(player1, "Setessan Oathsworn");
        assertThat(oathsworn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A spell that targets a player does not trigger Setessan Oathsworn")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new SetessanOathsworn());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        Permanent oathsworn = findPermanent(player1, "Setessan Oathsworn");
        assertThat(oathsworn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's spell that targets Setessan Oathsworn does not trigger it")
    void opponentsSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new SetessanOathsworn());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        UUID oathswornId = harness.getPermanentId(player1, "Setessan Oathsworn");
        harness.castInstant(player2, 0, oathswornId);
        harness.passBothPriorities();

        Permanent oathsworn = findPermanent(player1, "Setessan Oathsworn");
        assertThat(oathsworn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
