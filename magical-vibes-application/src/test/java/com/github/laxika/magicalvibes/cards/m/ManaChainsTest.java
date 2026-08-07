package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ManaChainsTest extends BaseCardTest {

    private Permanent enchant(Permanent creature) {
        Permanent aura = new Permanent(new ManaChains());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    @Test
    @DisplayName("Enchanted creature's controller pays {1} per age counter for the granted cumulative upkeep")
    void grantedCumulativeUpkeepIsPaid() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        enchant(bears);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(bears.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
    }

    @Test
    @DisplayName("Declining the granted cumulative upkeep sacrifices the enchanted creature")
    void decliningSacrificesCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        enchant(bears);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Unenchanted creatures get no cumulative upkeep")
    void unenchantedCreatureUnaffected() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(bears.getCounterCount(CounterType.AGE)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
    }
}
