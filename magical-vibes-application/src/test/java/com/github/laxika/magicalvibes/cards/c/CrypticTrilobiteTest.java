package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarksteelBrute;
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

@CardUsed({CrypticTrilobite.class, DarksteelBrute.class})
class CrypticTrilobiteTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=3 enters with three +1/+1 counters")
    void entersWithXCounters() {
        harness.setHand(player1, List.of(new CrypticTrilobite()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent trilobite = findTrilobite(player1);
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

        harness.setHand(player1, List.of(new DarksteelBrute()));
        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability-only mana pays for the counter ability")
    void abilityOnlyManaPaysForCounterAbility() {
        Permanent trilobite = addReadyTrilobite(player1, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(trilobite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
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

    private Permanent addReadyTrilobite(Player player, int counters) {
        Permanent trilobite = new Permanent(new CrypticTrilobite());
        trilobite.setSummoningSick(false);
        trilobite.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        gd.playerBattlefields.get(player.getId()).add(trilobite);
        return trilobite;
    }

    private Permanent findTrilobite(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof CrypticTrilobite)
                .findFirst()
                .orElseThrow();
    }
}
