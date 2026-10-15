package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AuraGraft;
import com.github.laxika.magicalvibes.cards.b.BlossomingDefense;
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

@CardUsed({EnchantedRiversGrasp.class, AirElemental.class, FountainOfYouth.class, BlossomingDefense.class, AuraGraft.class})
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

        harness.passUntil(player2, TurnStep.UPKEEP);

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

    @Test
    @DisplayName("The entry trigger still taps and removes counters if the creature gains hexproof")
    void entryTriggerDoesNotTargetEnchantedCreature() {
        Permanent creature = addCreature(player2);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new EnchantedRiversGrasp()));
        addMana();
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        harness.setHand(player2, List.of(new BlossomingDefense()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Enchanted River's Grasp");
    }

    @Test
    @DisplayName("Counters added after the entry trigger resolves remain on the creature")
    void countersAddedLaterAreNotRemoved() {
        Permanent creature = addCreature(player2);
        castResolve(creature);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The entry trigger affects the creature enchanted when it resolves")
    void entryTriggerFollowsAuraMovedInResponse() {
        Permanent original = addCreature(player2);
        Permanent newHost = addCreature(player2);
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        newHost.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new EnchantedRiversGrasp()));
        addMana();
        harness.castEnchantment(player1, 0, original.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Enchanted River's Grasp");
        harness.setHand(player2, List.of(new AuraGraft()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, aura.getId());
        harness.handlePermanentChosen(player2, newHost.getId());
        harness.passBothPriorities();

        assertThat(original.isTapped()).isFalse();
        assertThat(original.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(newHost.isTapped()).isTrue();
        assertThat(newHost.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The creature untaps normally after the Aura leaves")
    void removingAuraRestoresUntapping() {
        Permanent creature = addCreature(player2);
        castResolve(creature);
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Enchanted River's Grasp"));

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    private Permanent addCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new AirElemental());
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

}
