package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CanopyGorger;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SaddlebackLagac.class, CanopyGorger.class})
class SaddlebackLagacTest extends BaseCardTest {

    @Test
    void supportsUpToTwoOtherCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CanopyGorger());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CanopyGorger());
        harness.setHand(player1, List.of(new SaddlebackLagac()));
        addMana();

        harness.castCreature(player1, 0, List.of(first.getId(), second.getId()));
        resolveCreatureAndEtb();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void maySupportFewerThanTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CanopyGorger());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new CanopyGorger());
        harness.setHand(player1, List.of(new SaddlebackLagac()));
        addMana();

        harness.castCreature(player1, 0, List.of(first.getId()));
        resolveCreatureAndEtb();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canBeCastWithoutTargets() {
        harness.castFromHand(player1, new SaddlebackLagac(), "{3}{G}");
        resolveCreatureAndEtb();

        harness.assertOnBattlefield(player1, "Saddleback Lagac");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetItself() {
        Permanent other = harness.addToBattlefieldAndReturn(player1, new CanopyGorger());
        Permanent lagac = harness.enterBattlefieldAndReturn(player1, new SaddlebackLagac());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, lagac.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, other.getId());
        harness.passBothPriorities();

        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(lagac.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canSupportAnOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CanopyGorger());
        harness.setHand(player1, List.of(new SaddlebackLagac()));
        addMana();

        harness.castCreature(player1, 0, List.of(target.getId()));
        resolveCreatureAndEtb();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canChooseNoTargetsEvenWhenOtherCreaturesExist() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CanopyGorger());

        harness.enterBattlefieldAndReturn(player1, new SaddlebackLagac());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Saddleback Lagac");
    }

    @Test
    void remainingTargetStillGetsCounterWhenAnotherTargetLeaves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new CanopyGorger());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new CanopyGorger());
        harness.setHand(player1, List.of(new SaddlebackLagac()));
        addMana();

        harness.castCreature(player1, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(first);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
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
