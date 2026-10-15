package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BusterSword;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SazhsChocobo;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheEarthCrystal.class, TravelingChocobo.class, BusterSword.class, SazhsChocobo.class, Forest.class})
class TheEarthCrystalTest extends BaseCardTest {

    @Test
    @DisplayName("reduces the cost of green spells you cast")
    void reducesGreenSpellCost() {
        harness.addToBattlefield(player1, new TheEarthCrystal());
        harness.castFromHand(player1, new TravelingChocobo(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("does not reduce colorless spell costs")
    void doesNotReduceColorlessSpellCost() {
        harness.addToBattlefield(player1, new TheEarthCrystal());
        assertThatThrownBy(() -> harness.castFromHand(player1, new BusterSword(), "{2}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("doubles counters distributed to one creature")
    void doublesCountersOnOneCreature() {
        harness.addToBattlefield(player1, new TheEarthCrystal());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TravelingChocobo());
        addAbilityMana();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("distributes and doubles counters across two creatures")
    void distributesCountersAcrossTwoCreatures() {
        harness.addToBattlefield(player1, new TheEarthCrystal());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TravelingChocobo());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new TravelingChocobo());
        addAbilityMana();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("can target only creatures you control")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new TheEarthCrystal());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new TravelingChocobo());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotReduceOpponentsGreenSpells() {
        harness.addToBattlefield(player2, new TheEarthCrystal());

        assertThatThrownBy(() -> harness.castFromHand(player1, new TravelingChocobo(), "{1}{G}"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotReduceColoredMana() {
        harness.addToBattlefield(player1, new TheEarthCrystal());

        assertThatThrownBy(() -> harness.castFromHand(player1, new SazhsChocobo(), ""))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doublesCountersFromAnotherAbility() {
        harness.addToBattlefield(player1, new TheEarthCrystal());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SazhsChocobo());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotDoubleOpponentsCounters() {
        harness.addToBattlefield(player1, new TheEarthCrystal());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SazhsChocobo());
        harness.enterBattlefieldAndReturn(player2, new Forest());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void sourceLeavingDoesNotCounterAbilityOrDoubleCounters() {
        Permanent crystal = harness.addToBattlefieldAndReturn(player1, new TheEarthCrystal());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TravelingChocobo());
        addAbilityMana();
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(creature.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(crystal);
        gd.playerGraveyards.get(player1.getId()).add(crystal.getCard());

        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotRedistributeCountersWhenOneTargetLeaves() {
        harness.addToBattlefield(player1, new TheEarthCrystal());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TravelingChocobo());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new TravelingChocobo());
        addAbilityMana();
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(second);
        gd.playerGraveyards.get(player1.getId()).add(second.getCard());

        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void targetChangingControllersBecomesIllegal() {
        harness.addToBattlefield(player1, new TheEarthCrystal());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TravelingChocobo());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new TravelingChocobo());
        addAbilityMana();
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(second);
        gd.playerBattlefields.get(player2.getId()).add(second);

        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void activationPaysManaAndTapsCrystal() {
        Permanent crystal = harness.addToBattlefieldAndReturn(player1, new TheEarthCrystal());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TravelingChocobo());
        addAbilityMana();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(creature.getId()));

        assertThat(crystal.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);
    }
}
