package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SaddlebackLagac.class, GrizzlyBears.class})
class SaddlebackLagacTest extends BaseCardTest {

    @Test
    void supportsUpToTwoOtherCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SaddlebackLagac()));
        addMana();

        harness.castCreature(player1, 0, List.of(first.getId(), second.getId()));
        resolveCreatureAndEtb();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void maySupportFewerThanTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SaddlebackLagac()));
        addMana();

        harness.castCreature(player1, 0, List.of(first.getId()));
        resolveCreatureAndEtb();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canBeCastWithoutTargets() {
        harness.setHand(player1, List.of(new SaddlebackLagac()));
        addMana();

        harness.castCreature(player1, 0);
        resolveCreatureAndEtb();

        harness.assertOnBattlefield(player1, "Saddleback Lagac");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetItself() {
        harness.setHand(player1, List.of(new SaddlebackLagac()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        UUID lagacId = harness.getPermanentId(player1, "Saddleback Lagac");
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, lagacId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void resolveCreatureAndEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
