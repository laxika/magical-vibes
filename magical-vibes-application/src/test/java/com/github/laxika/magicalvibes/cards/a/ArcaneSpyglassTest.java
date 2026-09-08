package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArcaneSpyglassTest extends BaseCardTest {

    @Test
    void sacrificesLandDrawsAndAddsChargeCounter() {
        Permanent spyglass = addReadySpyglass();
        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(spyglass.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(spyglass.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void removesThreeChargeCountersAndDraws() {
        Permanent spyglass = addReadySpyglass();
        spyglass.setCounterCount(CounterType.CHARGE, 4);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(spyglass.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(spyglass.isTapped()).isFalse();
    }

    @Test
    void cannotActivateFirstAbilityWithoutLand() {
        addReadySpyglass();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateSecondAbilityWithFewerThanThreeChargeCounters() {
        Permanent spyglass = addReadySpyglass();
        spyglass.setCounterCount(CounterType.CHARGE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadySpyglass() {
        Permanent permanent = new Permanent(new ArcaneSpyglass());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }
}
