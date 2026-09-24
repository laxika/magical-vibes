package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeirOfTheAncientFang.class, GrizzlyBears.class, Forest.class})
class HeirOfTheAncientFangTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter when you control a modified creature")
    void entersWithCounterWhenControllingModifiedCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castHeir();

        Permanent heir = findPermanent(player1, "Heir of the Ancient Fang");
        assertThat(heir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not enter with a counter without a modified creature")
    void doesNotEnterWithCounterWithoutModifiedCreature() {
        castHeir();

        Permanent heir = findPermanent(player1, "Heir of the Ancient Fang");
        assertThat(heir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A modified noncreature does not satisfy the condition")
    void modifiedNoncreatureDoesNotSatisfyCondition() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castHeir();

        Permanent heir = findPermanent(player1, "Heir of the Ancient Fang");
        assertThat(heir.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castHeir() {
        harness.setHand(player1, List.of(new HeirOfTheAncientFang()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
