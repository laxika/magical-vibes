package com.github.laxika.magicalvibes.cards.w;

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

class WarWingSirenTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell that targets War-Wing Siren puts a +1/+1 counter on it")
    void castingSpellThatTargetsSirenTriggersHeroic() {
        harness.addToBattlefield(player1, new WarWingSiren());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID sirenId = harness.getPermanentId(player1, "War-Wing Siren");
        harness.castInstant(player1, 0, sirenId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent siren = findPermanent(player1, "War-Wing Siren");
        assertThat(siren.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A spell that targets a player does not trigger War-Wing Siren")
    void targetingPlayerDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new WarWingSiren());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        Permanent siren = findPermanent(player1, "War-Wing Siren");
        assertThat(siren.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's spell that targets War-Wing Siren does not trigger it")
    void opponentsSpellDoesNotTriggerHeroic() {
        harness.addToBattlefield(player1, new WarWingSiren());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        UUID sirenId = harness.getPermanentId(player1, "War-Wing Siren");
        harness.castInstant(player2, 0, sirenId);
        harness.passBothPriorities();

        Permanent siren = findPermanent(player1, "War-Wing Siren");
        assertThat(siren.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
