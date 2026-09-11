package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({ExpeditionRaptor.class, GrizzlyBears.class, Plains.class})
class ExpeditionRaptorTest extends BaseCardTest {

    @Test
    void putsCounterOnOneOtherTargetCreature() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        castRaptor(List.of(bear.getId()));

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void putsCounterOnEachOfTwoOtherTargetCreatures() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());

        castRaptor(List.of(first.getId(), second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canEnterWithoutTargets() {
        harness.setHand(player1, List.of(new ExpeditionRaptor()));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Expedition Raptor");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new ExpeditionRaptor()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(plains.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castRaptor(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new ExpeditionRaptor()));
        addMana();

        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
