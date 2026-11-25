package com.github.laxika.magicalvibes.mechanics;

import com.github.laxika.magicalvibes.cards.f.FathomMage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * "Put N +1/+1 counters on this creature" is the one counter placement: a later "that many" reads it (CR 608.2c),
 * and an ability that triggers for each counter put on the creature triggers once per counter (CR 603.2c).
 */
@CardUsed({GrizzlyBears.class, FathomMage.class})
class SelfCounterPlacementTest extends BaseCardTest {

    @Test
    @DisplayName("A later \"draw that many\" reads the counters the ability put on its source")
    void laterStepReadsTheSelfPlacement() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int handSize = gd.playerHands.get(player1.getId()).size();

        resolveAbilityOf(bears, PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE, 2),
                new DrawCardEffect(new AmountThisWay(StepKind.PUT_COUNTERS)));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 2);
    }

    @Test
    @DisplayName("Two counters put on Fathom Mage at once trigger its per-counter ability twice")
    void eachCounterTriggersAPerCounterAbility() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new FathomMage());

        resolveAbilityOf(mage, PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE, 2));

        assertThat(mage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).hasSize(2);
    }

    private void resolveAbilityOf(Permanent source, CardEffect... effects) {
        gd.stack.add(new StackEntry(StackEntryType.ACTIVATED_ABILITY, source.getCard(), player1.getId(),
                source.getCard().getName() + "'s ability", new ArrayList<>(List.of(effects)), (UUID) null, source.getId()));
        harness.passBothPriorities();
    }
}
