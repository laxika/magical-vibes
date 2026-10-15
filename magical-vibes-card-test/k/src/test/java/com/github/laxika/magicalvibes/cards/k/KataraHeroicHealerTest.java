package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KataraHeroicHealer.class, GrizzlyBears.class, Plains.class})
class KataraHeroicHealerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on each other creature you control")
    void etbCountersOtherControlledCreatures() {
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.castFromHand(player1, new KataraHeroicHealer(), "{4}{W}");
        resolveAllTriggers();

        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        Permanent katara = findPermanent(player1, "Katara, Heroic Healer");
        assertThat(katara.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("ETB affects all creatures present at resolution and ignores lands")
    void etbUsesCreaturesPresentAtResolution() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.castFromHand(player1, new KataraHeroicHealer(), "{4}{W}");
        harness.passBothPriorities();

        Permanent lateCreature = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(lateCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player1, "Katara, Heroic Healer")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("ETB resolves with no other creatures without putting a counter on Katara")
    void etbWithNoOtherCreatures() {
        harness.castFromHand(player1, new KataraHeroicHealer(), "{4}{W}");
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Katara, Heroic Healer")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Katara's combat damage gains life for her controller")
    void combatDamageGainsLife() {
        addCreatureReady(player1, new KataraHeroicHealer());
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.getLife(player1.getId())).isEqualTo(12);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }
}
