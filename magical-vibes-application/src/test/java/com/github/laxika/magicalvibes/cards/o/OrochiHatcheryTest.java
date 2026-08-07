package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrochiHatcheryTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=3 enters with 3 charge counters")
    void entersWithXChargeCounters() {
        harness.setHand(player1, List.of(new OrochiHatchery()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(findHatchery(player1).getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting with X=0 enters with no charge counters")
    void entersWithNoCountersForXZero() {
        harness.setHand(player1, List.of(new OrochiHatchery()));

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(findHatchery(player1).getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Ability creates one 1/1 Snake per charge counter")
    void createsOneSnakePerChargeCounter() {
        Permanent hatchery = addHatcheryReady(player1, 3);
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        List<Permanent> snakes = snakes(player1);
        assertThat(snakes).hasSize(3);
        assertThat(snakes).allSatisfy(snake -> {
            assertThat(gqs.getEffectivePower(gd, snake)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, snake)).isEqualTo(1);
        });
        assertThat(hatchery.isTapped()).isTrue();
        assertThat(hatchery.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability with no charge counters creates no tokens")
    void createsNoTokensWithoutCounters() {
        addHatcheryReady(player1, 0);
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(snakes(player1)).isEmpty();
    }

    private Permanent addHatcheryReady(Player player, int chargeCounters) {
        Permanent perm = new Permanent(new OrochiHatchery());
        perm.setSummoningSick(false);
        perm.setCounterCount(CounterType.CHARGE, chargeCounters);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findHatchery(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Orochi Hatchery"))
                .findFirst().orElseThrow();
    }

    private List<Permanent> snakes(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.SNAKE))
                .toList();
    }
}
