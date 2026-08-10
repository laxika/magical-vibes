package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OblivionStoneTest extends BaseCardTest {

    @Test
    void putsFateCounterOnTargetPermanent() {
        Permanent stone = addReadyStone();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.FATE)).isEqualTo(1);
        assertThat(stone.isTapped()).isTrue();
    }

    @Test
    void destroysUnmarkedNonlandsAndClearsFateCountersAfterward() {
        addReadyStone();
        Permanent markedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        markedCreature.setCounterCount(CounterType.FATE, 1);
        Permanent unmarkedCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent markedLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        markedLand.setCounterCount(CounterType.FATE, 1);
        Permanent unmarkedArtifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Mind Stone");
        harness.assertInGraveyard(player1, "Oblivion Stone");
        assertThat(markedCreature.getCounterCount(CounterType.FATE)).isZero();
        assertThat(markedLand.getCounterCount(CounterType.FATE)).isZero();
        assertThat(unmarkedArtifact.getCounterCount(CounterType.FATE)).isZero();
    }

    @Test
    void canTargetAPlayerOnlyAsAGroupTargetIsRejected() {
        addReadyStone();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyStone() {
        Permanent stone = harness.addToBattlefieldAndReturn(player1, new OblivionStone());
        stone.setSummoningSick(false);
        return stone;
    }
}
