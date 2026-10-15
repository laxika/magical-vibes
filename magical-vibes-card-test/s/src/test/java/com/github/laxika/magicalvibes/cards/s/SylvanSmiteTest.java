package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SylvanSmite.class, AirElemental.class, GrizzlyBears.class, HillGiant.class})
class SylvanSmiteTest extends BaseCardTest {

    @Test
    void startingPlayerDealsBasePowerWithoutPuttingOnCounter() {
        gd.startingPlayerId = player1.getId();
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        cast(player1, source, target);

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void nonStartingPlayerPutsCounterBeforeDealingDamage() {
        gd.startingPlayerId = player1.getId();
        Permanent source = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        cast(player2, source, target);

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void requiresControlledSourceAndOpponentCreatureTarget() {
        Permanent ownSource = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new SylvanSmite()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(ownSource.getId(), ownTarget.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(com.github.laxika.magicalvibes.model.Player caster,
                      Permanent source, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new SylvanSmite()));
        harness.addMana(caster, ManaColor.GREEN, 2);
        harness.castInstant(caster, 0, List.of(source.getId(), target.getId()));
        harness.passBothPriorities();
    }
}
