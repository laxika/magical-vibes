package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrypticTrilobite.class})
class CrypticTrilobiteTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=3 enters with three +1/+1 counters")
    void entersWithXCounters() {
        harness.setHand(player1, List.of(new CrypticTrilobite()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent trilobite = findPermanent(player1, "Cryptic Trilobite");
        assertThat(trilobite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removing a +1/+1 counter adds two ability-only colorless mana")
    void removesCounterForAbilityOnlyMana() {
        Permanent trilobite = addReadyTrilobite(player1, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(trilobite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(pool.getAbilityOnlyMana(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(pool.get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Ability-only mana cannot cast a creature spell")
    void abilityOnlyManaCannotCastSpell() {
        addReadyTrilobite(player1, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        harness.setHand(player1, List.of(new CrypticTrilobite()));
        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability-only mana pays for the counter ability")
    void abilityOnlyManaPaysForCounterAbility() {
        Permanent trilobite = addReadyTrilobite(player1, 2);
        harness.activateAbility(player1, 0, 0, null, null);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(trilobite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(trilobite.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyManaTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("The mana ability cannot be activated without a +1/+1 counter")
    void cannotActivateManaAbilityWithoutCounter() {
        Permanent trilobite = addReadyTrilobite(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(trilobite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Casting with X=0 puts the zero-toughness creature into the graveyard")
    void zeroXDiesOnEntry() {
        harness.setHand(player1, List.of(new CrypticTrilobite()));

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cryptic Trilobite");
        harness.assertInGraveyard(player1, "Cryptic Trilobite");
    }

    @Test
    @DisplayName("Casting requires paying both X symbols")
    void cannotCastWithoutPayingTwiceX() {
        harness.setHand(player1, List.of(new CrypticTrilobite()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 3, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Cryptic Trilobite");
    }

    @Test
    @DisplayName("The mana ability works while tapped and summoning sick without using the stack")
    void manaAbilityIgnoresTapAndSummoningSickness() {
        Permanent trilobite = addReadyTrilobite(player1, 2);
        trilobite.setSummoningSick(true);
        trilobite.tap();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(trilobite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(trilobite.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyManaTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing the last counter still produces mana before the creature dies")
    void removingLastCounterProducesManaAndCreatureDies() {
        addReadyTrilobite(player1, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Cryptic Trilobite");
        harness.assertInGraveyard(player1, "Cryptic Trilobite");
        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyManaTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("The tap ability cannot be activated while summoning sick")
    void counterAbilityRequiresNoSummoningSickness() {
        Permanent trilobite = addReadyTrilobite(player1, 1);
        trilobite.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(trilobite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(trilobite.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The tap ability adds its counter only when it resolves")
    void counterAbilityUsesStackAndCannotBeRepeatedWhileTapped() {
        Permanent trilobite = addReadyTrilobite(player1, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(trilobite.isTapped()).isTrue();
        assertThat(trilobite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
        assertThat(trilobite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The two restricted mana can pay for abilities of two different creatures")
    void restrictedManaPaysForTwoSeparateAbilities() {
        Permanent first = addReadyTrilobite(player1, 2);
        Permanent second = addReadyTrilobite(player1, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyManaTotal()).isZero();
    }

    private Permanent addReadyTrilobite(Player player, int counters) {
        Permanent trilobite = harness.addToBattlefieldAndReturn(player, new CrypticTrilobite());
        trilobite.setSummoningSick(false);
        trilobite.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        return trilobite;
    }
}
