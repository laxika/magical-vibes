package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.j.JourneyersKite;
import com.github.laxika.magicalvibes.cards.w.WearAway;
import com.github.laxika.magicalvibes.cards.y.YamabushisFlame;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SerpentSkin.class, IsamaruHoundOfKonda.class, JourneyersKite.class,
        WearAway.class, YamabushisFlame.class})
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

    @Test
    @DisplayName("Flash allows Serpent Skin to resolve during an opponent's upkeep")
    void canCastDuringOpponentsUpkeep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        harness.setHand(player1, List.of(new SerpentSkin()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Serpent Skin").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("The Aura's controller can regenerate an opponent's enchanted creature")
    void canRegenerateOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new IsamaruHoundOfKonda());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SerpentSkin());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.getRegenerationShield()).isEqualTo(1);
        assertThat(aura.getRegenerationShield()).isZero();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Regeneration still resolves if Serpent Skin is destroyed in response")
    void regenerationSurvivesAuraRemoval() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SerpentSkin());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new WearAway()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.castAndResolveInstant(player2, 0, aura.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Serpent Skin");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature).doesNotContain(aura);
        assertThat(creature.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration saves the enchanted creature from lethal damage and keeps the Aura attached")
    void regenerationPreventsLethalDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IsamaruHoundOfKonda());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SerpentSkin());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new YamabushisFlame()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castInstant(player2, 0, creature.getId());
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        assertThat(creature.isTapped()).isFalse();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature, aura);
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getRegenerationShield()).isZero();
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        harness.assertNotInGraveyard(player1, "Isamaru, Hound of Konda");
    }
}
