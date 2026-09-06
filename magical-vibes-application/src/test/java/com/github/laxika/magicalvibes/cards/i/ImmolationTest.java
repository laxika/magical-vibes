package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
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

@CardUsed({Immolation.class, GrizzlyBears.class, HillGiant.class, HowlingMine.class})
class ImmolationTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Immolation targeting a creature puts it on the stack")
    void castingPutsOnStack() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        harness.setHand(player1, List.of(new Immolation()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, giant.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(Immolation.class);
    }

    @Test
    @DisplayName("Resolving Immolation attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        harness.setHand(player1, List.of(new Immolation()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof Immolation
                        && giant.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Immolation can enchant an opponent's creature")
    void enchantsOpponentsCreature() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.setHand(player1, List.of(new Immolation()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enchanted creature gets +2/-2")
    void enchantedCreatureGetsBoost() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Immolation());
        aura.setAttachedTo(giant.getId());

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creature returns to base stats when Immolation is removed")
    void effectsStopWhenRemoved() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Immolation());
        aura.setAttachedTo(giant.getId());

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Immolation causes a 2/2 creature to die from zero toughness")
    void killsCreatureWithZeroToughness() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Immolation immolation = new Immolation();

        harness.setHand(player1, List.of(immolation));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Immolation);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card == bears.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card == immolation);
    }

    @Test
    @DisplayName("Immolation fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Immolation immolation = new Immolation();

        harness.setHand(player1, List.of(immolation));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, giant.getId());
        gd.playerBattlefields.get(player1.getId()).remove(giant);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(immolation);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == immolation);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Immolation")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new HillGiant());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        harness.setHand(player1, List.of(new Immolation()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
