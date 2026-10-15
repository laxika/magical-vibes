package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeylineOfPunishment;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IronscaleHydra.class, GrizzlyBears.class, LeylineOfPunishment.class, Shock.class})
class IronscaleHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage and gets one counter per damage source")
    void preventsCombatDamageWithOneCounterPerSource() {
        Permanent hydra = addCreatureReady(player2, new IronscaleHydra());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(hydra);
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(hydra.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not prevent noncombat damage")
    void doesNotPreventNoncombatDamage() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player2, new IronscaleHydra());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID hydraId = hydra.getId();
        harness.castInstant(player1, 0, hydraId);
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(hydra.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Gets a counter but takes damage when combat damage cannot be prevented")
    void getsCounterWhenCombatDamageCannotBePrevented() {
        harness.addToBattlefield(player1, new LeylineOfPunishment());
        Permanent hydra = addCreatureReady(player2, new IronscaleHydra());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(hydra);
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(hydra.getMarkedDamage()).isEqualTo(2);
    }
}
