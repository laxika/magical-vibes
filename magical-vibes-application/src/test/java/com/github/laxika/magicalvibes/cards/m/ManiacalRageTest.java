package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.h.HoodedKavu;
import com.github.laxika.magicalvibes.cards.p.PhyrexianLens;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ManiacalRage.class, HoodedKavu.class, PhyrexianLens.class})
class ManiacalRageTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Maniacal Rage attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HoodedKavu());

        ManiacalRage rage = new ManiacalRage();
        harness.setHand(player1, List.of(rage));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == rage
                        && p.isAttached()
                        && creature.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HoodedKavu());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ManiacalRage());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Enchanted creature can't block")
    void enchantedCreatureCantBlock() {
        Permanent creature = addCreatureReady(player1, new HoodedKavu());

        assertThat(bls.canBlock(gd, creature)).isTrue();

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ManiacalRage());
        aura.setAttachedTo(creature.getId());

        assertThat(bls.canBlock(gd, creature)).isFalse();
    }

    @Test
    @DisplayName("Creature returns to base stats and can block again when Maniacal Rage is removed")
    void effectsStopWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new HoodedKavu());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ManiacalRage());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(bls.canBlock(gd, creature)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(bls.canBlock(gd, creature)).isTrue();
    }

    @Test
    @DisplayName("Maniacal Rage can enchant an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HoodedKavu());
        ManiacalRage rage = new ManiacalRage();
        harness.setHand(player1, List.of(rage));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == rage
                        && p.isAttached()
                        && creature.getId().equals(p.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Maniacal Rage goes to its owner's graveyard when its target is removed")
    void fizzlesIfTargetRemoved() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HoodedKavu());
        ManiacalRage rage = new ManiacalRage();
        harness.setHand(player1, List.of(rage));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(rage);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == rage);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Maniacal Rage")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PhyrexianLens());
        harness.setHand(player1, List.of(new ManiacalRage()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
