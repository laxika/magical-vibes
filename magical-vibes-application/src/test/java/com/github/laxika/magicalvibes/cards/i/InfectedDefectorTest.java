package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FlameJavelin;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InfectedDefector.class, FlameJavelin.class})
class InfectedDefectorTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, it creates an Incubator token with three +1/+1 counters")
    void deathCreatesIncubatorToken() {
        harness.addToBattlefield(player1, new InfectedDefector());

        killWithFlameJavelin();
        harness.passBothPriorities();

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("The Incubator token can pay {2} to transform into a 3/3 Phyrexian")
    void incubatorTransformsWithItsAbility() {
        harness.addToBattlefield(player1, new InfectedDefector());

        killWithFlameJavelin();
        harness.passBothPriorities();

        Permanent incubator = findPermanent(player1, "Incubator");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(incubator), null, null);
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(incubator.getCard().getName()).isEqualTo("Phyrexian");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, incubator)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, incubator)).isEqualTo(3);
    }

    private void killWithFlameJavelin() {
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, java.util.List.of(new FlameJavelin()));
        harness.addMana(player2, ManaColor.RED, 6);

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Infected Defector"));
        harness.passBothPriorities();
    }
}
