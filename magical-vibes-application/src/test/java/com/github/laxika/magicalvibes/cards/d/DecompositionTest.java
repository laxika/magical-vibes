package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BogImp;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DecompositionTest extends BaseCardTest {

    private Permanent enchant(Permanent creature) {
        Permanent aura = new Permanent(new Decomposition());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    @Test
    @DisplayName("Enchanted creature's controller pays 1 life per age counter for the granted cumulative upkeep")
    void grantedCumulativeUpkeepCostsLife() {
        Permanent imp = harness.addToBattlefieldAndReturn(player2, new BogImp());
        enchant(imp);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(imp.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(imp);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Declining the granted cumulative upkeep sacrifices the creature and its controller loses 2 life")
    void decliningSacrificesAndDrains() {
        Permanent imp = harness.addToBattlefieldAndReturn(player2, new BogImp());
        enchant(imp);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(imp);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("When the enchanted creature dies, its controller loses exactly 2 life")
    void enchantedCreatureDeathLosesTwoLife() {
        Permanent imp = harness.addToBattlefieldAndReturn(player2, new BogImp());
        enchant(imp);
        harness.setLife(player2, 20);

        imp.setMarkedDamage(5);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Cannot enchant a nonblack creature")
    void cannotEnchantNonblackCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        // A legal black creature exists, so the Aura is castable — only this target is illegal.
        harness.addToBattlefield(player2, new BogImp());
        harness.setHand(player1, List.of(new Decomposition()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a black creature");
    }
}
