package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AnabaBodyguard;
import com.github.laxika.magicalvibes.cards.d.Didgeridoo;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Carapace.class, AnabaBodyguard.class, Didgeridoo.class})
class CarapaceTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Carapace attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AnabaBodyguard());

        harness.setHand(player1, List.of(new Carapace()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Carapace")
                        && creature.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature gets +0/+2")
    void enchantedCreatureGetsBoost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AnabaBodyguard());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Carapace());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Creature returns to base stats when Carapace is removed")
    void boostStopsWhenRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AnabaBodyguard());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Carapace());
        aura.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Carapace can enchant and regenerate an opponent's creature")
    void canEnchantAndRegenerateOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AnabaBodyguard());

        harness.setHand(player1, List.of(new Carapace()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.getRegenerationShield()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Carapace");
    }

    @Test
    @DisplayName("Sacrificing Carapace regenerates the enchanted creature")
    void sacrificingRegeneratesEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AnabaBodyguard());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Carapace());
        aura.setAttachedTo(creature.getId());

        // aura is index 1 on the battlefield (creature is index 0)
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(creature.getRegenerationShield()).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Carapace");
        harness.assertInGraveyard(player1, "Carapace");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Carapace")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new AnabaBodyguard());
        harness.setHand(player1, List.of(new Carapace()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Didgeridoo());

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
