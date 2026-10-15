package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SwampsnareTrap.class, AirElemental.class, FountainOfYouth.class, GrizzlyBears.class})
class SwampsnareTrapTest extends BaseCardTest {

    @Test
    void enchantedCreatureGetsMinusFiveMinusThree() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SwampsnareTrap());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    void costsOneLessWhenTargetingCreatureWithFlying() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.setHand(player1, List.of(new SwampsnareTrap()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotReduceCostWhenTargetingCreatureWithoutFlying() {
        harness.addToBattlefield(player2, new AirElemental());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SwampsnareTrap()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new SwampsnareTrap()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void resolvesOntoFlyingCreatureAndOnlyWeakensThatCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new SwampsnareTrap()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Swampsnare Trap");
        assertThat(findPermanent(player1, "Swampsnare Trap").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(4);
    }

    @Test
    void canPayFullCostForNonflyingCreatureAndBothCreatureAndAuraGoToGraveyard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SwampsnareTrap()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Swampsnare Trap");
        harness.assertInGraveyard(player1, "Swampsnare Trap");
    }

    @Test
    void reductionDoesNotRemoveBlackManaRequirement() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new SwampsnareTrap()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reductionAlsoAppliesWhenTargetingOwnFlyingCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setHand(player1, List.of(new SwampsnareTrap()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertOnBattlefield(player1, "Swampsnare Trap");
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }
}
