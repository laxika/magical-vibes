package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BallLightning;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavageSwipe;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TreefolkUmbra.class, BallLightning.class, GrizzlyBears.class, FountainOfYouth.class,
        DoomBlade.class, SavageSwipe.class})
class TreefolkUmbraTest extends BaseCardTest {

    @Test
    @DisplayName("Treefolk Umbra gives the enchanted creature +0/+2 and toughness-based combat damage")
    void enchantedCreatureGetsBoostAndUsesToughnessForCombatDamage() {
        Permanent ballLightning = addCreatureReady(player1, new BallLightning());
        attachAura(ballLightning);

        assertThat(gqs.getEffectivePower(gd, ballLightning)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ballLightning)).isEqualTo(3);
        assertThat(gqs.getEffectiveCombatDamage(gd, ballLightning)).isEqualTo(3);
    }

    @Test
    @DisplayName("Umbra armor saves an enchanted creature from lethal damage and destroys Treefolk Umbra")
    void umbraArmorSavesFromLethalDamage() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = attachAura(creature);
        creature.setMarkedDamage(4);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Treefolk Umbra");
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
    }

    @Test
    @DisplayName("Umbra armor saves an enchanted creature from a destroy effect")
    void umbraArmorSavesFromDestroyEffect() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(creature);

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Treefolk Umbra");
    }

    @Test
    @DisplayName("Treefolk Umbra cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new TreefolkUmbra()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Treefolk Umbra resolves attached to an opponent's creature")
    void canEnchantOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TreefolkUmbra()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Treefolk Umbra");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveCombatDamage(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveCombatDamage(gd, otherCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("An unblocked enchanted creature deals combat damage using toughness even when power is higher")
    void unblockedCreatureDealsToughnessBasedDamage() {
        Permanent creature = addCreatureReady(player1, new BallLightning());
        attachAura(creature);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Umbra armor removes damage without tapping or removing the creature from combat and ignores regeneration restrictions")
    void umbraArmorIsNotRegeneration() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(creature);
        creature.setMarkedDamage(4);
        creature.setDamagedByDeathtouch(true);
        creature.setCantRegenerateThisTurn(true);
        creature.setAttacking(true);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Treefolk Umbra");
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.isDamagedByDeathtouch()).isFalse();
        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Umbra armor does not save a creature with zero toughness")
    void zeroToughnessIsNotDestruction() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(creature);
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 4);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Treefolk Umbra");
    }

    @Test
    @DisplayName("Treefolk Umbra does not replace power with toughness when fighting")
    void fightUsesPowerInsteadOfToughness() {
        Permanent creature = addCreatureReady(player1, new BallLightning());
        attachAura(creature);
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        opponent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new SavageSwipe()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(creature.getId(), opponent.getId()));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Ball Lightning");
        harness.assertInGraveyard(player1, "Treefolk Umbra");
        assertThat(creature.getMarkedDamage()).isZero();
    }

    private Permanent attachAura(Permanent creature) {
        Permanent aura = new Permanent(new TreefolkUmbra());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }
}
