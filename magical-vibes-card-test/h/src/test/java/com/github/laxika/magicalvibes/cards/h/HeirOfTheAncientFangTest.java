package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BambooGroveArcher;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NinjasKunai;
import com.github.laxika.magicalvibes.cards.s.ShortCircuit;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeirOfTheAncientFang.class, GrizzlyBears.class, Forest.class,
        BambooGroveArcher.class, NinjasKunai.class, ShortCircuit.class})
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

    @Test
    void unmodifiedCreatureDoesNotSatisfyCondition() {
        harness.addToBattlefield(player1, new BambooGroveArcher());

        castHeir();

        assertThat(findPermanent(player1, "Heir of the Ancient Fang")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentsModifiedCreatureDoesNotSatisfyCondition() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BambooGroveArcher());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        castHeir();

        assertThat(findPermanent(player1, "Heir of the Ancient Fang")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void counterNeedNotBePlusOnePlusOne() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BambooGroveArcher());
        creature.setCounterCount(CounterType.CHARGE, 1);

        castHeir();

        assertThat(findPermanent(player1, "Heir of the Ancient Fang")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void equipmentQualifiesRegardlessOfItsController(boolean opponentControlsEquipment) {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BambooGroveArcher());
        Permanent equipment = harness.addToBattlefieldAndReturn(
                opponentControlsEquipment ? player2 : player1, new NinjasKunai());
        equipment.setAttachedTo(creature.getId());

        castHeir();

        assertThat(findPermanent(player1, "Heir of the Ancient Fang")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void auraQualifiesOnlyWhenControlledByCreatureController(boolean opponentControlsAura) {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BambooGroveArcher());
        Permanent aura = harness.addToBattlefieldAndReturn(
                opponentControlsAura ? player2 : player1, new ShortCircuit());
        aura.setAttachedTo(creature.getId());

        castHeir();

        assertThat(findPermanent(player1, "Heir of the Ancient Fang")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(opponentControlsAura ? 0 : 1);
    }

    @Test
    void multipleModifiedCreaturesStillGiveOnlyOneCounter() {
        for (int i = 0; i < 2; i++) {
            Permanent creature = harness.addToBattlefieldAndReturn(player1, new BambooGroveArcher());
            creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        }

        castHeir();

        assertThat(findPermanent(player1, "Heir of the Ancient Fang")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void conditionIsCheckedAtEntryRatherThanWhenCast(boolean modifiedAtEntry) {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BambooGroveArcher());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, modifiedAtEntry ? 0 : 1);
        harness.castFromHand(player1, new HeirOfTheAncientFang(), "{2}{G}");

        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, modifiedAtEntry ? 1 : 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Heir of the Ancient Fang")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(modifiedAtEntry ? 1 : 0);
        assertThat(gd.stack).isEmpty();
    }

    private void castHeir() {
        harness.castFromHand(player1, new HeirOfTheAncientFang(), "{2}{G}");
        harness.passBothPriorities();
    }
}
