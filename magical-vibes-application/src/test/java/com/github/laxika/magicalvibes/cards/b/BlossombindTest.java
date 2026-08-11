package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Skinrender;
import com.github.laxika.magicalvibes.cards.t.Twiddle;
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

class BlossombindTest extends BaseCardTest {

    @Test
    @DisplayName("When Blossombind enters, it taps the enchanted creature")
    void entersAndTapsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Blossombind()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Blossombind prevents the enchanted creature from untapping")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();

        Permanent aura = new Permanent(new Blossombind());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Blossombind prevents a spell from untapping the enchanted creature")
    void enchantedCreatureCannotBeUntappedBySpell() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.tap();

        Permanent aura = new Permanent(new Blossombind());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player2.getId()).add(aura);

        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Blossombind prevents counters from being put on the enchanted creature")
    void enchantedCreatureCantHaveCounters() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = new Permanent(new Blossombind());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player2.getId()).add(aura);

        harness.setHand(player1, List.of(new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
