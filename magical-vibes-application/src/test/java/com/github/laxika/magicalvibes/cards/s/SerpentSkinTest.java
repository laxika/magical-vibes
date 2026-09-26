package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.j.JourneyersKite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SerpentSkin.class, IsamaruHoundOfKonda.class, JourneyersKite.class})
class SerpentSkinTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Serpent Skin attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());

        SerpentSkin serpentSkin = new SerpentSkin();
        harness.setHand(player1, List.of(serpentSkin));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == serpentSkin
                        && creature.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature gets +1/+1")
    void enchantedCreatureGetsBoost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SerpentSkin());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creature returns to base stats when Serpent Skin is removed")
    void boostStopsWhenRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SerpentSkin());
        aura.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Paying {G} regenerates the enchanted creature and leaves the Aura on the battlefield")
    void activatingRegeneratesEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SerpentSkin());
        aura.setAttachedTo(creature.getId());

        harness.addMana(player1, ManaColor.GREEN, 1);

        // aura is index 1 on the battlefield (the creature is index 0)
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(creature.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
    }

    @Test
    @DisplayName("Serpent Skin can enchant an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new IsamaruHoundOfKonda());
        SerpentSkin serpentSkin = new SerpentSkin();
        harness.setHand(player1, List.of(serpentSkin));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == serpentSkin
                        && creature.getId().equals(p.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Serpent Skin cannot regenerate a creature while it is unattached")
    void activatingWhileUnattachedDoesNothing() {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SerpentSkin());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(aura.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Serpent Skin")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new JourneyersKite());
        harness.setHand(player1, List.of(new SerpentSkin()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
