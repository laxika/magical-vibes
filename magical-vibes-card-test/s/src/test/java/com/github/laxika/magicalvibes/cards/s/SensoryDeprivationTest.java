package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SensoryDeprivation.class, WalkingCorpse.class, TravelersAmulet.class})
class SensoryDeprivationTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Sensory Deprivation targeting a creature puts it on the stack")
    void castingPutsOnStack() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        harness.setHand(player1, List.of(new SensoryDeprivation()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Resolving Sensory Deprivation attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        harness.setHand(player1, List.of(new SensoryDeprivation()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sensory Deprivation")
                        && bears.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature gets -3/-0")
    void enchantedCreatureGetsDebuff() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SensoryDeprivation());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creature returns to base stats when Sensory Deprivation is removed")
    void effectsStopWhenRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SensoryDeprivation());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sensory Deprivation fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        harness.setHand(player1, List.of(new SensoryDeprivation()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sensory Deprivation");
        harness.assertNotOnBattlefield(player1, "Sensory Deprivation");
    }

    @Test
    @DisplayName("Only the enchanted opposing creature gets the power reduction")
    void canEnchantOpposingCreatureWithoutAffectingOthers() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new SensoryDeprivation()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Sensory Deprivation").getAttachedTo()).isEqualTo(target.getId());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Walking Corpse");
    }

    @Test
    @DisplayName("Multiple Sensory Deprivations apply cumulatively")
    void multipleAurasStack() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new SensoryDeprivation(), new SensoryDeprivation()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Sensory Deprivation")).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(-4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Walking Corpse");
    }

    @Test
    @DisplayName("Sensory Deprivation goes to its owner's graveyard when its host leaves")
    void auraGoesToGraveyardWhenHostLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new SensoryDeprivation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Sensory Deprivation");
        harness.assertInGraveyard(player1, "Sensory Deprivation");
        harness.assertNotInGraveyard(player2, "Sensory Deprivation");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Sensory Deprivation")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new WalkingCorpse());
        harness.addToBattlefield(player1, new TravelersAmulet());
        harness.setHand(player1, List.of(new SensoryDeprivation()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Permanent artifact = findPermanent(player1, "Traveler's Amulet");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
