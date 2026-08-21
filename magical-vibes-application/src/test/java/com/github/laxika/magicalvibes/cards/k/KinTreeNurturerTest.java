package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(KinTreeNurturer.class)
class KinTreeNurturerTest extends BaseCardTest {

    @Test
    @DisplayName("When Kin-Tree Nurturer enters, enduring with counters puts a +1/+1 counter on it")
    void enduresWithCounter() {
        castNurturer();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put 1 +1/+1 counter on this permanent");

        Permanent nurturer = findPermanent(player1, "Kin-Tree Nurturer");
        assertThat(nurturer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("When Kin-Tree Nurturer enters, enduring with a token creates a 1/1 Spirit")
    void enduresWithSpiritToken() {
        castNurturer();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Create a 1/1 white Spirit creature token");

        Permanent nurturer = findPermanent(player1, "Kin-Tree Nurturer");
        assertThat(nurturer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    @DisplayName("Kin-Tree Nurturer's lifelink gains life from combat damage")
    void lifelinkGainsLifeFromCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent nurturer = addCreatureReady(player1, new KinTreeNurturer());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(nurturer)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private void castNurturer() {
        harness.setHand(player1, List.of(new KinTreeNurturer()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }
}
