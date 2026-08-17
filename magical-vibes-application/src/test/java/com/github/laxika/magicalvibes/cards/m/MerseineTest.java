package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MerseineTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three net counters and keeps the enchanted creature tapped")
    void entersWithCountersAndPreventsUntap() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = castMerseine(creature);

        assertThat(aura.getCounterCount(CounterType.NET)).isEqualTo(3);
        creature.tap();

        advanceToNextUpkeep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Pays the enchanted creature's mana cost and removes a net counter")
    void paysEnchantedCreatureManaCost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = castMerseine(creature);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(aura), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(aura.getCounterCount(CounterType.NET)).isEqualTo(3);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, indexOf(aura), 0, null, null);
        harness.passBothPriorities();

        assertThat(aura.getCounterCount(CounterType.NET)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing all net counters lets the enchanted creature untap")
    void removingAllCountersAllowsUntap() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = castMerseine(creature);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 3);

        for (int i = 0; i < 3; i++) {
            harness.activateAbility(player1, indexOf(aura), 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(aura.getCounterCount(CounterType.NET)).isZero();
        creature.tap();
        advanceToNextUpkeep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Only the enchanted creature's controller may activate Merseine")
    void onlyEnchantedCreatureControllerMayActivate() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = castMerseine(creature);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(aura), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("enchanted permanent's controller");
        assertThat(aura.getCounterCount(CounterType.NET)).isEqualTo(3);

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.activateAbility(player2, 1, 0, null, null);
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

    private void advanceToNextUpkeep(Player endingActivePlayer) {
        harness.forceActivePlayer(endingActivePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
