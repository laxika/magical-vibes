package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnchantedRiversGrasp.class, AirElemental.class, FountainOfYouth.class})
class EnchantedRiversGraspTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted River's Grasp taps the creature and removes all its counters")
    void entersTappingCreatureAndRemovingCounters() {
        Permanent creature = addCreature(player2);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        creature.setCounterCount(CounterType.STUN, 1);

        castResolve(creature);

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(creature.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    @DisplayName("Enchanted River's Grasp removes abilities and prevents untapping")
    void removesAbilitiesAndPreventsUntapping() {
        Permanent creature = addCreature(player2);
        castResolve(creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing Enchanted River's Grasp restores the creature's abilities")
    void removingAuraRestoresAbilities() {
        Permanent creature = addCreature(player2);
        castResolve(creature);

        Permanent aura = findPermanent(player1, "Enchanted River's Grasp");
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Enchanted River's Grasp cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new EnchantedRiversGrasp()));
        addMana();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addCreature(Player player) {
        Permanent creature = new Permanent(new AirElemental());
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private void castResolve(Permanent creature) {
        harness.setHand(player1, List.of(new EnchantedRiversGrasp()));
        addMana();
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
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
