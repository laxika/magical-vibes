package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BonebreakerGiant;
import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.cards.r.RoyalAssassin;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiritMantle.class, RuneclawBear.class, BonebreakerGiant.class, Manalith.class, RoyalAssassin.class, Shock.class})
class SpiritMantleTest extends BaseCardTest {

    private Permanent enchant(Permanent creature) {
        Permanent aura = new Permanent(new SpiritMantle());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }

    @Test
    @DisplayName("Enchanted creature gets +1/+1")
    void enchantedCreatureGetsBoost() {
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        enchant(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost goes away when Spirit Mantle leaves the battlefield")
    void boostStopsWhenRemoved() {
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        Permanent aura = enchant(bears);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Enchanted creature can't be blocked by a creature")
    void creaturesCannotBlockEnchantedCreature() {
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        bears.setAttacking(true);
        enchant(bears);

        Permanent blocker = addCreatureReady(player2, new BonebreakerGiant());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");

        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Enchanted blocker takes no combat damage and still deals its own damage")
    void enchantedCreatureTakesNoCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new BonebreakerGiant());
        Permanent blocker = addCreatureReady(player2, new RuneclawBear());
        enchant(blocker);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player2, "Runeclaw Bear");
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player2, new RuneclawBear());
        harness.addToBattlefield(player1, new Manalith());
        harness.setHand(player1, List.of(new SpiritMantle()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent artifact = findPermanent(player1, "Manalith");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Resolves on an opponent's creature and stays attached despite granting protection")
    void resolvesOnOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new RuneclawBear());
        Permanent other = addCreatureReady(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new SpiritMantle()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.runStateBasedActions();

        Permanent aura = findPermanent(player1, "Spirit Mantle");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creature abilities cannot target the enchanted creature")
    void creatureAbilityCannotTarget() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        creature.tap();
        enchant(creature);
        addCreatureReady(player1, new RoyalAssassin());

        assertThatThrownBy(() -> harness.activateAbility(player1, 2, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Creature abilities can target again after the Aura leaves")
    void protectionStopsWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        creature.tap();
        Permanent aura = enchant(creature);
        addCreatureReady(player1, new RoyalAssassin());
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Protection from creatures does not stop damage from an instant")
    void instantCanTargetAndDamage() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        enchant(creature);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Aura goes to the graveyard when its creature dies")
    void auraLeavesWhenCreatureDies() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        enchant(creature);
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player1, "Spirit Mantle");
    }
}
