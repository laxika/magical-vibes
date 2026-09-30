package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Flamebraider;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TendToTheKiln.class, Flamebraider.class, LightningBolt.class})
class TendToTheKilnTest extends BaseCardTest {

    @Test
    @DisplayName("Non-Elemental instants in hand become Elementals and fuel flame counters")
    void convertsOwnedInstantAndSorceryCards() {
        harness.setHand(player1, List.of(new LightningBolt()));
        Permanent kiln = harness.enterBattlefieldAndReturn(player1, new TendToTheKiln());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(kiln.getCounterCount(CounterType.FLAME)).isEqualTo(1);
    }

    @Test
    @DisplayName("Three Elemental spells conjure a hasty Flamebraider and remove the counters")
    void conjuresFlamebraiderAtThreeCounters() {
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        Permanent kiln = harness.enterBattlefieldAndReturn(player1, new TendToTheKiln());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 3);

        castAndResolveBolt();
        castAndResolveBolt();
        castAndResolveBolt();

        assertThat(kiln.getCounterCount(CounterType.FLAME)).isZero();
        Permanent flamebraider = findPermanent(player1, "Flamebraider");
        assertThat(flamebraider.getCard()).isInstanceOf(Flamebraider.class);
        assertThat(flamebraider.hasKeyword(com.github.laxika.magicalvibes.model.Keyword.HASTE)).isTrue();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Flamebraider")).isEmpty();
    }

    private void castAndResolveBolt() {
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
