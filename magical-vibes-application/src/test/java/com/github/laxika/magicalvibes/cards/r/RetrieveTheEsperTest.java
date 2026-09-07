package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RetrieveTheEsper.class)
class RetrieveTheEsperTest extends BaseCardTest {

    @Test
    @DisplayName("Normal cast creates a 3/3 Robot Warrior without counters")
    void normalCastCreatesRobotWarriorWithoutCounters() {
        harness.setHand(player1, List.of(new RetrieveTheEsper()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent token = robotWarrior();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(token.getEffectivePower()).isEqualTo(3);
        assertThat(token.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Flashback creates a Robot Warrior with two +1/+1 counters")
    void flashbackCreatesRobotWarriorWithTwoCounters() {
        harness.setGraveyard(player1, List.of(new RetrieveTheEsper()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        Permanent token = robotWarrior();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(token.getEffectivePower()).isEqualTo(5);
        assertThat(token.getEffectiveToughness()).isEqualTo(5);
        harness.assertNotInGraveyard(player1, "Retrieve the Esper");
    }

    private Permanent robotWarrior() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && "Robot Warrior".equals(permanent.getCard().getName()))
                .findFirst()
                .orElseThrow();
    }
}
