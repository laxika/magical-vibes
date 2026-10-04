package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.t.TravelingPhilosopher;
import com.github.laxika.magicalvibes.cards.t.TravelersAmulet;
import com.github.laxika.magicalvibes.cards.v.VoyagesEnd;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FateForetold.class, TravelingPhilosopher.class, TravelersAmulet.class, VoyagesEnd.class})
class FateForetoldTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when the Aura enters")
    void drawsCardWhenAuraEnters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new FateForetold()));
        harness.setLibrary(player1, List.of(new TravelingPhilosopher()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(TravelingPhilosopher.class);
    }

    @Test
    @DisplayName("When the enchanted creature dies, its controller draws a card")
    void enchantedCreatureControllerDrawsOnDeath() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FateForetold());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new TravelingPhilosopher()));

        creature.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new TravelersAmulet());
        harness.setHand(player1, List.of(new FateForetold()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enchantingOpponentsCreatureDrawsForAuraControllerOnEntry() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new FateForetold()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new TravelingPhilosopher()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void doesNotDrawWhenTargetDiesBeforeAuraResolves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new FateForetold()));
        harness.setLibrary(player1, List.of(new TravelingPhilosopher()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        creature.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof FateForetold);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void unrelatedCreatureDyingDoesNotTriggerDraw() {
        Permanent enchanted = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FateForetold());
        aura.setAttachedTo(enchanted.getId());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new TravelingPhilosopher()));

        other.setMarkedDamage(2);
        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void returningEnchantedCreatureToHandDoesNotTriggerDraw() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TravelingPhilosopher());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FateForetold());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new TravelersAmulet()));
        harness.setHand(player2, List.of(new VoyagesEnd()));
        harness.setLibrary(player2, List.of());
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player2, 0, creature.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isSameAs(creature.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof FateForetold);
        assertThat(gd.stack).isEmpty();
    }
}
