package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BigMotherMouser.class, WrathOfGod.class})
class BigMotherMouserTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters")
    void entersWithTwoCounters() {
        harness.setHand(player1, List.of(new BigMotherMouser()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent mouser = findPermanent(player1, "Big Mother Mouser");
        assertThat(mouser.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Doubles its +1/+1 counters when it attacks")
    void doublesCountersOnAttack() {
        Permanent mouser = addMouserReady(player1, 2);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(mouser.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Creates one artifact Robot token per +1/+1 counter when it dies")
    void deathCreatesRobotTokensPerCounter() {
        addMouserReady(player1, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.castSorcery(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> robots = findPermanents(player1, "Robot");
        assertThat(robots).hasSize(3);
        assertThat(robots).allSatisfy(robot -> {
            assertThat(robot.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
            assertThat(gqs.isCreature(gd, robot)).isTrue();
            assertThat(gqs.getEffectivePower(gd, robot)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, robot)).isEqualTo(1);
        });
    }

    private Permanent addMouserReady(Player player, int counters) {
        Permanent mouser = addCreatureReady(player, new BigMotherMouser());
        mouser.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        return mouser;
    }
}
