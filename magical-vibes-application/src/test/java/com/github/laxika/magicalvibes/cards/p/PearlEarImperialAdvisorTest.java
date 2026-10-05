package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BlanchwoodArmor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HistoryOfBenalia;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PearlEarImperialAdvisor.class, GrizzlyBears.class, HolyStrength.class, HistoryOfBenalia.class,
        LeoninScimitar.class, Pacifism.class, BlanchwoodArmor.class})
class PearlEarImperialAdvisorTest extends BaseCardTest {

    @Test
    void enchantmentSpellsCostOneLessForEachAuraYouControl() {
        harness.addToBattlefield(player1, new PearlEarImperialAdvisor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new HistoryOfBenalia()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void cannotCastEnchantmentWithoutEnoughManaWhenNoAuraIsControlled() {
        harness.addToBattlefield(player1, new PearlEarImperialAdvisor());
        harness.setHand(player1, List.of(new HistoryOfBenalia()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void drawsWhenCastingAuraTargetingYourModifiedPermanent() {
        harness.addToBattlefield(player1, new PearlEarImperialAdvisor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    void doesNotDrawWhenAuraTargetsAnUnmodifiedPermanent() {
        harness.addToBattlefield(player1, new PearlEarImperialAdvisor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }
    @Test
    void eachControlledAuraReducesTheGenericCost() {
        harness.addToBattlefield(player1, new PearlEarImperialAdvisor());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        for (int i = 0; i < 2; i++) {
            Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
            aura.setAttachedTo(creature.getId());
        }
        harness.setHand(player1, List.of(new BlanchwoodArmor()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void nonPowerToughnessCounterAlsoModifiesTheTarget() {
        harness.addToBattlefield(player1, new PearlEarImperialAdvisor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.CHARGE, 1);
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void drawsWhenTargetAlreadyHasYourAura() {
        harness.addToBattlefield(player1, new PearlEarImperialAdvisor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(creature.getId());
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void opponentsAuraDoesNotModifyYourCreature() {
        harness.addToBattlefield(player1, new PearlEarImperialAdvisor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new HolyStrength());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void opponentsEquipmentStillModifiesYourCreature() {
        harness.addToBattlefield(player1, new PearlEarImperialAdvisor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        equipment.setAttachedTo(creature.getId());
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void doesNotTriggerForOpponentsModifiedCreature() {
        harness.addToBattlefield(player1, new PearlEarImperialAdvisor());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void drawStillResolvesAfterTargetLosesItsModification() {
        harness.addToBattlefield(player1, new PearlEarImperialAdvisor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(new HolyStrength()));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void yourAuraOnOpponentsCreatureReducesAuraSpellCost() {
        harness.addToBattlefield(player1, new PearlEarImperialAdvisor());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentsAuraDoesNotReduceYourSpellCost() {
        harness.addToBattlefield(player1, new PearlEarImperialAdvisor());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new HolyStrength());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void affinityDoesNotPayColoredMana() {
        harness.addToBattlefield(player1, new PearlEarImperialAdvisor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new HolyStrength()));

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void affinityDoesNotReduceNonEnchantmentSpells() {
        harness.addToBattlefield(player1, new PearlEarImperialAdvisor());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
