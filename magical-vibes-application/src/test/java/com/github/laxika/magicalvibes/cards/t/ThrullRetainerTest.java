package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArmorThrull;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThrullRetainer.class, ArmorThrull.class, TidalInfluence.class})
class ThrullRetainerTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Thrull Retainer attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArmorThrull());

        harness.setHand(player1, List.of(new ThrullRetainer()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Thrull Retainer")
                        && creature.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Thrull Retainer can enchant an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArmorThrull());

        harness.setHand(player1, List.of(new ThrullRetainer()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Thrull Retainer")
                        && creature.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature gets +1/+1")
    void enchantedCreatureGetsBoost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArmorThrull());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ThrullRetainer());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Creature returns to base stats when Thrull Retainer is removed")
    void boostStopsWhenRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArmorThrull());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ThrullRetainer());
        aura.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Sacrificing Thrull Retainer regenerates the enchanted creature")
    void sacrificingRegeneratesEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArmorThrull());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ThrullRetainer());
        aura.setAttachedTo(creature.getId());

        // aura is index 1 on the battlefield (creature is index 0)
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(creature.getRegenerationShield()).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Thrull Retainer");
        harness.assertInGraveyard(player1, "Thrull Retainer");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Thrull Retainer")
    void cannotTargetNonCreature() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new TidalInfluence());
        harness.setHand(player1, List.of(new ThrullRetainer()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
