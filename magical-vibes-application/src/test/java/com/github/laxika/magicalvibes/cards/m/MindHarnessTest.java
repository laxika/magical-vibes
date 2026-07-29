package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MindHarnessTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Mind Harness steals the enchanted green creature")
    void resolvingStealsCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new MindHarness()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Paying cumulative upkeep keeps Mind Harness and control of the creature")
    void payingCumulativeUpkeepKeepsAura() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = attach(player1, creature);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(aura.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Mind Harness")
    void decliningCumulativeUpkeepSacrificesAura() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent aura = attach(player1, creature);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        harness.assertInGraveyard(player1, "Mind Harness");
    }

    @Test
    @DisplayName("Cannot enchant a creature that is neither red nor green")
    void cannotEnchantBlueCreature() {
        Permanent wizard = addCreatureReady(player2, new FugitiveWizard());
        addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new MindHarness()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, wizard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a red or green creature");
    }

    private Permanent attach(Player controller, Permanent enchanted) {
        Permanent aura = new Permanent(new MindHarness());
        aura.setAttachedTo(enchanted.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }
}
