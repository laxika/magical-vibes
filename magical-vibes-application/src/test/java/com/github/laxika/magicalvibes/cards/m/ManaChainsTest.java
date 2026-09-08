package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.r.RedwoodTreefolk;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ManaChains.class, RedwoodTreefolk.class, MindStone.class})
class ManaChainsTest extends BaseCardTest {

    private Permanent enchant(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ManaChains());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    @Test
    @DisplayName("Enchanted creature's controller pays {1} per age counter for the granted cumulative upkeep")
    void grantedCumulativeUpkeepIsPaid() {
        Permanent treefolk = harness.addToBattlefieldAndReturn(player2, new RedwoodTreefolk());
        enchant(treefolk);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(treefolk.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(treefolk);
    }

    @Test
    @DisplayName("Declining the granted cumulative upkeep sacrifices the enchanted creature")
    void decliningSacrificesCreature() {
        Permanent treefolk = harness.addToBattlefieldAndReturn(player2, new RedwoodTreefolk());
        enchant(treefolk);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(treefolk);
        harness.assertInGraveyard(player2, "Redwood Treefolk");
    }

    @Test
    @DisplayName("Unenchanted creatures get no cumulative upkeep")
    void unenchantedCreatureUnaffected() {
        Permanent treefolk = harness.addToBattlefieldAndReturn(player2, new RedwoodTreefolk());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(treefolk.getCounterCount(CounterType.AGE)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(treefolk);
    }

    @Test
    @DisplayName("Each subsequent upkeep charges for all age counters on the enchanted creature")
    void secondUpkeepCostsTwoMana() {
        Permanent treefolk = harness.addToBattlefieldAndReturn(player2, new RedwoodTreefolk());
        enchant(treefolk);
        treefolk.setCounterCount(CounterType.AGE, 1);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(treefolk.getCounterCount(CounterType.AGE)).isEqualTo(2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(treefolk);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Mana Chains cannot target a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        harness.setHand(player1, List.of(new ManaChains()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
