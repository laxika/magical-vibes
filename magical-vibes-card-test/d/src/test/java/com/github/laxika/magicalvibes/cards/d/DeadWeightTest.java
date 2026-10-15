package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FesterhideBoar;
import com.github.laxika.magicalvibes.cards.t.TravelersAmulet;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeadWeight.class, FesterhideBoar.class, WalkingCorpse.class, TravelersAmulet.class})
class DeadWeightTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Dead Weight targeting a creature puts it on the stack")
    void castingPutsOnStack() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        harness.setHand(player1, List.of(new DeadWeight()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Resolving Dead Weight attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new FesterhideBoar());

        harness.setHand(player1, List.of(new DeadWeight()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Dead Weight").getAttachedTo()).isEqualTo(giant.getId());
    }

    @Test
    @DisplayName("Enchanted creature gets -2/-2")
    void enchantedCreatureGetsDebuff() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new FesterhideBoar());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DeadWeight());
        aura.setAttachedTo(giant.getId());

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creature returns to base stats when Dead Weight is removed")
    void effectsStopWhenRemoved() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new FesterhideBoar());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new DeadWeight());
        aura.setAttachedTo(giant.getId());

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Dead Weight kills a 2/2 creature")
    void killsTwoToughnessCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        harness.setHand(player1, List.of(new DeadWeight()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Walking Corpse");
        harness.assertInGraveyard(player1, "Walking Corpse");
    }

    @Test
    @DisplayName("Dead Weight fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        harness.setHand(player1, List.of(new DeadWeight()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dead Weight");
        harness.assertNotOnBattlefield(player1, "Dead Weight");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Dead Weight")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new WalkingCorpse());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new TravelersAmulet());
        harness.setHand(player1, List.of(new DeadWeight()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Dead Weight affects only the enchanted opposing creature")
    void affectsOnlyEnchantedOpposingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FesterhideBoar());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new DeadWeight()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Dead Weight").getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Dead Weight goes to its owner's graveyard after killing an opposing creature")
    void auraGoesToOwnersGraveyardWhenCreatureDies() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new DeadWeight()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Walking Corpse");
        harness.assertInGraveyard(player2, "Walking Corpse");
        harness.assertNotOnBattlefield(player1, "Dead Weight");
        harness.assertInGraveyard(player1, "Dead Weight");
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(card -> card instanceof DeadWeight);
    }

    @Test
    @DisplayName("Two Dead Weights combine to kill a creature that survives one")
    void multipleCopiesCombine() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FesterhideBoar());
        harness.setHand(player1, List.of(new DeadWeight(), new DeadWeight()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Festerhide Boar");
        harness.assertInGraveyard(player2, "Festerhide Boar");
        harness.assertNotOnBattlefield(player1, "Dead Weight");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof DeadWeight).hasSize(2);
    }
}
