package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AshPartyCrasher.class, Forest.class, GrizzlyBears.class})
class AshPartyCrasherTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when two nonland permanents entered this turn")
    void putsCounterAfterTwoNonlandPermanentsEnter() {
        Permanent ash = castAsh();
        castGrizzlyBears();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(ash.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger without two nonland permanents")
    void doesNotTriggerWithoutTwoNonlandPermanents() {
        Permanent ash = castAsh();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(ash.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not count lands toward celebration")
    void doesNotCountLands() {
        Permanent ash = castAsh();
        gd.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(player1.getId(), ignored -> new ArrayList<>())
                .add(new Forest());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(ash.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent castAsh() {
        harness.setHand(player1, List.of(new AshPartyCrasher()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Ash, Party Crasher");
    }

    private void castGrizzlyBears() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
