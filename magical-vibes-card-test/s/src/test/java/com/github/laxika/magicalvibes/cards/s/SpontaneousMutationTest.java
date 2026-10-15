package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.t.ThrabenInspector;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpontaneousMutation.class, GrizzlyBears.class, Opt.class, FountainOfYouth.class, ThrabenInspector.class})
class SpontaneousMutationTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Spontaneous Mutation puts it on the stack")
    void castingPutsOnStack() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

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
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
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
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SpontaneousMutation());
        aura.setAttachedTo(bears.getId());

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
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setGraveyard(player1, List.of(new Opt(), new Opt(), new Opt()));
        harness.setGraveyard(player2, List.of(new Opt()));

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SpontaneousMutation());
        aura.setAttachedTo(opponentBears.getId());

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Effect ends when aura leaves the battlefield")
    void effectEndsWhenAuraLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Opt(), new Opt()));

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SpontaneousMutation());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(0);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new SpontaneousMutation()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @CardUsed({SpontaneousMutation.class, ThrabenInspector.class})
    @DisplayName("Flash allows casting during the opponent's upkeep")
    void castsDuringOpponentsUpkeep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ThrabenInspector());
        harness.setGraveyard(player1, List.of(new ThrabenInspector()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new SpontaneousMutation()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spontaneous Mutation");
        assertThat(findPermanent(player1, "Spontaneous Mutation").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @CardUsed({SpontaneousMutation.class, ThrabenInspector.class})
    @DisplayName("Aura goes to the graveyard if its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ThrabenInspector());
        harness.setHand(player1, List.of(new SpontaneousMutation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, creature.getId());

        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.setGraveyard(player2, List.of(creature.getCard()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spontaneous Mutation");
        harness.assertInGraveyard(player1, "Spontaneous Mutation");
        assertThat(gd.stack).isEmpty();
    }

}
