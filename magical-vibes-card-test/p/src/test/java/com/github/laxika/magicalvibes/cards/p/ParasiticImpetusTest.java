package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ParasiticImpetus.class, GrizzlyBears.class, Mountain.class, RayOfCommand.class})
class ParasiticImpetusTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2 and is goaded")
    void enchantedCreatureGetsBoostAndIsGoaded() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        castParasiticImpetus(player1, bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(als.getMustAttackRequirementCount(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking with the enchanted creature makes its controller lose 2 life and you gain 2 life")
    void attackingWithEnchantedCreatureDrainsItsController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        castParasiticImpetus(player1, bears);

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Parasitic Impetus cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new ParasiticImpetus()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void attackTriggerResolvesBeforeCombatDamage() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        castParasiticImpetus(player1, bears);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0));
            resolveAllTriggers();
        });

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void enchantingOwnAttackerLosesAndGainsLifeForSamePlayer() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castParasiticImpetus(player1, bears);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            resolveAllTriggers();
        });

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void goadedCreatureCannotDeclineToAttackWhenAble() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        castParasiticImpetus(player1, bears);

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void attackTriggerUsesCreatureControllerAtResolution() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        castParasiticImpetus(player1, bears);
        harness.setHand(player1, List.of(new RayOfCommand()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0));
            harness.ensurePriority(player1);
            harness.castInstant(player1, 0, bears.getId());
            harness.passBothPriorities();
            assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
            resolveAllTriggers();
        });

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void castParasiticImpetus(Player caster, Permanent creature) {
        harness.setHand(caster, List.of(new ParasiticImpetus()));
        harness.addMana(caster, ManaColor.BLACK, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 2);
        harness.castEnchantment(caster, 0, creature.getId());
        harness.passBothPriorities();
    }
}
