package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.d.DreamFighter;
import com.github.laxika.magicalvibes.cards.j.JungleWurm;
import com.github.laxika.magicalvibes.cards.v.ViashinoWarrior;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindHarness.class, JungleWurm.class, DreamFighter.class, ViashinoWarrior.class, Disenchant.class})
class MindHarnessTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Mind Harness steals the enchanted green creature")
    void resolvingStealsCreature() {
        Permanent creature = addCreatureReady(player2, new JungleWurm());

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
    @DisplayName("Resolving Mind Harness steals the enchanted red creature")
    void resolvingStealsRedCreature() {
        Permanent creature = addCreatureReady(player2, new ViashinoWarrior());

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
    @DisplayName("Destroying Mind Harness returns the creature to its owner")
    void destroyingAuraReturnsCreatureToOwner() {
        Permanent creature = addCreatureReady(player2, new JungleWurm());

        harness.setHand(player1, List.of(new MindHarness()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Mind Harness");
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, aura.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
        harness.assertInGraveyard(player1, "Mind Harness");
    }

    @Test
    @DisplayName("Paying cumulative upkeep keeps Mind Harness and control of the creature")
    void payingCumulativeUpkeepKeepsAura() {
        Permanent creature = addCreatureReady(player2, new JungleWurm());
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
    @DisplayName("Cumulative upkeep costs one mana for each age counter")
    void cumulativeUpkeepScalesWithAgeCounters() {
        Permanent creature = addCreatureReady(player2, new JungleWurm());
        Permanent aura = attach(player1, creature);
        aura.setCounterCount(CounterType.AGE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(aura.getCounterCount(CounterType.AGE)).isEqualTo(2);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Mind Harness")
    void decliningCumulativeUpkeepSacrificesAura() {
        Permanent creature = addCreatureReady(player2, new JungleWurm());
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
        Permanent wizard = addCreatureReady(player2, new DreamFighter());

        harness.setHand(player1, List.of(new MindHarness()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, wizard.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a red or green creature");
    }

    private Permanent attach(Player controller, Permanent enchanted) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new MindHarness());
        aura.setAttachedTo(enchanted.getId());
        return aura;
    }
}
