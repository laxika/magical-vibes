package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.l.LeylineOfPunishment;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Defang.class, FountainOfYouth.class, GoldMyr.class, GrizzlyBears.class,
        LightningBolt.class, LeylineOfPunishment.class, ProdigalPyromancer.class})
class DefangTest extends BaseCardTest {

    @Test
    @DisplayName("Can target a creature with Defang")
    void canTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Defang()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Defang")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Defang()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanted attacker deals no combat damage to the defending player")
    void enchantedAttackerDealsNoDamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Defang());
        aura.setAttachedTo(bears.getId());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Enchanted attacker deals no combat damage to its blocker")
    void enchantedAttackerDealsNoDamageToBlocker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Defang());
        aura.setAttachedTo(attacker.getId());

        Permanent blocker = addCreatureReady(player2, new GoldMyr());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Gold Myr");
    }

    @Test
    @DisplayName("Damage dealt to the enchanted creature is not prevented")
    void damageToEnchantedCreatureStillApplies() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Defang());
        aura.setAttachedTo(bears.getId());

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void resolvedAuraPreventsActivatedAbilityDamageToPlayer() {
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        harness.setHand(player1, List.of(new Defang()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.setLife(player1, 20);

        harness.castEnchantment(player1, 0, pyromancer.getId());
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Defang").getAttachedTo()).isEqualTo(pyromancer.getId());

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void preventsActivatedAbilityDamageToCreature() {
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoldMyr());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Defang());
        aura.setAttachedTo(pyromancer.getId());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Gold Myr");
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void cannotPreventUnpreventableActivatedAbilityDamageToPlayer() {
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Defang());
        aura.setAttachedTo(pyromancer.getId());
        harness.addToBattlefield(player1, new LeylineOfPunishment());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void enchantedBlockerDealsNoDamageButStillReceivesDamage() {
        Permanent attacker = addCreatureReady(player1, new GoldMyr());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Defang());
        aura.setAttachedTo(blocker.getId());

        harness.resolveCombatDamage();

        harness.assertOnBattlefield(player1, "Gold Myr");
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void cannotPreventUnpreventableCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Defang());
        aura.setAttachedTo(attacker.getId());
        harness.addToBattlefield(player1, new LeylineOfPunishment());
        harness.setLife(player2, 20);

        harness.resolveCombatDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void damageAbilityStillResolvesAfterEnchantedSourceDies() {
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new Defang());
        aura.setAttachedTo(pyromancer.getId());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.castInstant(player2, 0, pyromancer.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Prodigal Pyromancer");
        harness.assertInGraveyard(player2, "Defang");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
