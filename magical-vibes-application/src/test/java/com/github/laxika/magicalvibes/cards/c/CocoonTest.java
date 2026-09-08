package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CocoonTest extends BaseCardTest {

    @Test
    @DisplayName("Cocoon enters by tapping its enchanted creature and adding three pupa counters")
    void entersWithCountersAndTapsCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        Permanent cocoon = castCocoon(creature);

        assertThat(creature.isTapped()).isTrue();
        assertThat(cocoon.getCounterCount(CounterType.PUPA)).isEqualTo(3);
    }

    @Test
    @DisplayName("Pupa counters keep the enchanted creature tapped through its untap step")
    void pupaCountersPreventUntap() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent cocoon = castCocoon(creature);
        creature.tap();

        advanceToUpkeep(player1);

        assertThat(creature.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(cocoon.getCounterCount(CounterType.PUPA)).isEqualTo(2);
    }

    @Test
    @DisplayName("When Cocoon has no pupa counters, it is sacrificed and permanently rewards the creature")
    void sacrificesAndRewardsEnchantedCreatureWithoutCounters() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castCocoon(creature);

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, creature, com.github.laxika.magicalvibes.model.Keyword.FLYING)).isFalse();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, com.github.laxika.magicalvibes.model.Keyword.FLYING)).isTrue();
        harness.assertNotOnBattlefield(player1, "Cocoon");
        harness.assertInGraveyard(player1, "Cocoon");
    }

    @Test
    @DisplayName("Cocoon can target only a creature its controller controls")
    void rejectsOpponentCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Cocoon()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    private Permanent castCocoon(Permanent creature) {
        harness.setHand(player1, List.of(new Cocoon()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof Cocoon)
                .findFirst()
                .orElseThrow();
    }
}
