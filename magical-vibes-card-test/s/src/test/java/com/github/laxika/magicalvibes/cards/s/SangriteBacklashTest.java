package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({SangriteBacklash.class, GrizzlyBears.class, SerraAngel.class, FountainOfYouth.class})
class SangriteBacklashTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Sangrite Backlash targeting a creature puts it on the stack")
    void castingPutsOnStack() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new SangriteBacklash()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Sangrite Backlash");
    }

    @Test
    @DisplayName("Resolving Sangrite Backlash attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent serra = harness.addToBattlefieldAndReturn(player1, new SerraAngel());

        harness.setHand(player1, List.of(new SangriteBacklash()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, serra.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sangrite Backlash")
                        && serra.getId().equals(p.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature gets +3/-3")
    void enchantedCreatureGetsBoostAndDebuff() {
        Permanent serra = harness.addToBattlefieldAndReturn(player1, new SerraAngel());

        Permanent aura = new Permanent(new SangriteBacklash());
        aura.setAttachedTo(serra.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, serra)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, serra)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creature returns to base stats when Sangrite Backlash is removed")
    void effectsStopWhenRemoved() {
        Permanent serra = harness.addToBattlefieldAndReturn(player1, new SerraAngel());

        Permanent aura = new Permanent(new SangriteBacklash());
        aura.setAttachedTo(serra.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, serra)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, serra)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, serra)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, serra)).isEqualTo(4);
    }

    @Test
    @DisplayName("Sangrite Backlash fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new SangriteBacklash()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sangrite Backlash");
        harness.assertNotOnBattlefield(player1, "Sangrite Backlash");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Sangrite Backlash")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new SangriteBacklash()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Sangrite Backlash can enchant an opponent's creature using green hybrid mana")
    void enchantsOpponentCreatureWithGreenMana() {
        Permanent serra = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SangriteBacklash()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, serra.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Sangrite Backlash");
        assertThat(aura.getAttachedTo()).isEqualTo(serra.getId());
        assertThat(gqs.getEffectivePower(gd, serra)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, serra)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Lethal toughness reduction sends the creature and its Aura to their owners' graveyards")
    void lethalToughnessReductionRemovesCreatureAndAura() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SangriteBacklash()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Sangrite Backlash");
        harness.assertInGraveyard(player1, "Sangrite Backlash");
        assertThat(gd.stack).isEmpty();
    }
}
