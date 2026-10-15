package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HolyMantle.class, GrizzlyBears.class, HillGiant.class, FountainOfYouth.class,
        ProdigalPyromancer.class, Shock.class, Naturalize.class})
class HolyMantleTest extends BaseCardTest {

    private Permanent enchant(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyMantle());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoost() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        enchant(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Enchanted creature cannot be blocked by a creature")
    void creaturesCannotBlockEnchantedCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        enchant(bears);

        Permanent blocker = addCreatureReady(player2, new HillGiant());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");

        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Enchanted creature takes no combat damage from a blocking creature")
    void enchantedCreatureTakesNoCombatDamage() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        enchant(bears);

        Permanent blocker = addCreatureReady(player2, new HillGiant());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new HolyMantle()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void enchantedCreatureCanBlockAndPreventsDamageFromAttacker() {
        Permanent attacker = addCreatureReady(player2, new HillGiant());
        attacker.setAttacking(true);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        enchant(bears);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void resolvesAttachedToAnOpponentsCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HolyMantle()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.runStateBasedActions();

        Permanent aura = findPermanent(player1, "Holy Mantle");
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    void rejectsCreatureAbilitiesFromEitherController() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        enchant(bears);
        addCreatureReady(player1, new ProdigalPyromancer());
        addCreatureReady(player2, new ProdigalPyromancer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 2, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    void creatureAbilityLosesItsTargetWhenProtectionIsGranted() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new ProdigalPyromancer());
        harness.activateAbility(player2, 0, null, bears.getId());

        enchant(bears);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(bears.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void noncreatureSpellCanTargetAndDamageEnchantedCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        enchant(bears);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, bears.getId());

        assertThat(bears.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void destroyingAuraRemovesBoostAndProtection() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = enchant(bears);
        addCreatureReady(player2, new ProdigalPyromancer());
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player2, 0, aura.getId());

        harness.assertInGraveyard(player1, "Holy Mantle");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        harness.activateAbility(player2, 0, null, bears.getId());
        harness.passBothPriorities();
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }
}
