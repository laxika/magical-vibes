package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AuraFinesse;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NoggleTheMind;
import com.github.laxika.magicalvibes.cards.s.Skinrender;
import com.github.laxika.magicalvibes.cards.t.Twiddle;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Blossombind.class, GrizzlyBears.class, Skinrender.class, Twiddle.class, NoggleTheMind.class,
        AuraFinesse.class})
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

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Blossombind());
        aura.setAttachedTo(creature.getId());

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Blossombind prevents a spell from untapping the enchanted creature")
    void enchantedCreatureCannotBeUntappedBySpell() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.tap();

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Blossombind());
        aura.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Blossombind prevents counters from being put on the enchanted creature")
    void enchantedCreatureCantHaveCounters() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Blossombind());
        aura.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Blossombind leaves existing counters on the enchanted creature")
    void existingCountersRemain() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new Blossombind()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The enter trigger taps the current enchanted creature after the Aura moves")
    void enterTriggerFollowsMovedAura() {
        Permanent originalHost = addCreatureReady(player2, new GrizzlyBears());
        Permanent destination = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Blossombind(), new AuraFinesse()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, originalHost.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Blossombind");
        assertThat(originalHost.isTapped()).isFalse();

        harness.castAndResolveInstant(player1, 0, List.of(aura.getId(), destination.getId()));
        assertThat(aura.getAttachedTo()).isEqualTo(destination.getId());
        harness.passBothPriorities();

        assertThat(originalHost.isTapped()).isFalse();
        assertThat(destination.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing Blossombind allows the creature to untap")
    void creatureUntapsAfterAuraLeaves() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Blossombind());
        aura.setAttachedTo(creature.getId());

        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Losing all abilities does not remove Blossombind's untap restriction")
    void abilityRemovalDoesNotAllowUntapping() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Blossombind());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new NoggleTheMind()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Losing all abilities does not remove Blossombind's counter restriction")
    void abilityRemovalDoesNotAllowCounters() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Blossombind());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new NoggleTheMind(), new Skinrender()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.castCreature(player1, 0, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }
}
