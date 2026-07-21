package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpontaneousMutationTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Spontaneous Mutation puts it on the stack")
    void castingPutsOnStack() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player1, List.of(new SpontaneousMutation()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Spontaneous Mutation");
    }

    @Test
    @DisplayName("Resolving Spontaneous Mutation attaches and applies -X/-0 from graveyard size")
    void resolvesAndDebuffsFromGraveyard() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        harness.setGraveyard(player1, List.of(new Opt(), new Opt()));

        harness.setHand(player1, List.of(new SpontaneousMutation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Spontaneous Mutation")
                        && bears.getId().equals(p.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Debuff updates dynamically when graveyard size changes")
    void updatesDynamicallyWithGraveyardSize() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent aura = new Permanent(new SpontaneousMutation());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        harness.setGraveyard(player1, List.of(new Opt()));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);

        harness.setGraveyard(player1, List.of(new Opt(), new Opt(), new Opt()));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        harness.setGraveyard(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts aura controller's graveyard, not enchanted creature controller's")
    void countsAurasControllersGraveyard() {
        Permanent opponentBears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(opponentBears);

        harness.setGraveyard(player1, List.of(new Opt(), new Opt(), new Opt()));
        harness.setGraveyard(player2, List.of(new Opt()));

        Permanent aura = new Permanent(new SpontaneousMutation());
        aura.setAttachedTo(opponentBears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Effect ends when aura leaves the battlefield")
    void effectEndsWhenAuraLeaves() {
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);
        harness.setGraveyard(player1, List.of(new Opt(), new Opt()));

        Permanent aura = new Permanent(new SpontaneousMutation());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(0);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new SpontaneousMutation()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
