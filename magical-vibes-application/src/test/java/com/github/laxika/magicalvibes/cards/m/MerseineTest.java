package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.r.RiverMerfolk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Merseine.class, RiverMerfolk.class})
class MerseineTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three net counters and keeps the enchanted creature tapped")
    void entersWithCountersAndPreventsUntap() {
        Permanent creature = addCreatureReady(player1, new RiverMerfolk());
        Permanent aura = castMerseine(creature);

        assertThat(aura.getCounterCount(CounterType.NET)).isEqualTo(3);
        creature.tap();

        advanceToNextUpkeep();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Pays the enchanted creature's mana cost and removes a net counter")
    void paysEnchantedCreatureManaCost() {
        Permanent creature = addCreatureReady(player1, new RiverMerfolk());
        Permanent aura = castMerseine(creature);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(aura), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(aura.getCounterCount(CounterType.NET)).isEqualTo(3);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, indexOf(aura), 0, null, null);
        harness.passBothPriorities();

        assertThat(aura.getCounterCount(CounterType.NET)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing all net counters lets the enchanted creature untap")
    void removingAllCountersAllowsUntap() {
        Permanent creature = addCreatureReady(player1, new RiverMerfolk());
        Permanent aura = castMerseine(creature);
        harness.addMana(player1, ManaColor.BLUE, 6);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, indexOf(aura), 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(aura.getCounterCount(CounterType.NET)).isZero();
        harness.addMana(player1, ManaColor.BLUE, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(aura), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");

        creature.tap();
        advanceToNextUpkeep();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Only the enchanted creature's controller may activate Merseine")
    void onlyEnchantedCreatureControllerMayActivate() {
        Permanent creature = addCreatureReady(player2, new RiverMerfolk());
        addCreatureReady(player1, new RiverMerfolk());
        Permanent aura = castMerseine(creature);

        harness.addMana(player1, ManaColor.BLUE, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(aura), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("enchanted permanent's controller");
        assertThat(aura.getCounterCount(CounterType.NET)).isEqualTo(3);

        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.activateAbility(player2, indexOf(aura), 0, null, null);
        harness.passBothPriorities();

        assertThat(aura.getCounterCount(CounterType.NET)).isEqualTo(2);
    }

    private Permanent castMerseine(Permanent creature) {
        harness.setHand(player1, List.of(new Merseine()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        return findPermanent(player1, "Merseine");
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void advanceToNextUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);
    }

}
