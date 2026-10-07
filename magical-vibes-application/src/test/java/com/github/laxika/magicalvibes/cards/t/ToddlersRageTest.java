package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ToddlersRage.class, GrizzlyBears.class, FountainOfYouth.class})
class ToddlersRageTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+1 and tantrum")
    void boostsEnchantedCreatureAndGrantsTantrum() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        castToddlersRage(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TANTRUM)).isTrue();
    }

    @Test
    @DisplayName("The Aura's effect ends when it leaves the battlefield")
    void effectEndsWhenAuraLeavesBattlefield() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        castToddlersRage(bears);
        Permanent aura = findPermanent(player1, "Toddler's Rage");

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TANTRUM)).isFalse();
    }

    @Test
    @DisplayName("Can target only a creature")
    void cannotTargetNoncreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new ToddlersRage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void tantrumDealsExcessBlockingDamageToAttackingPlayer() {
        Permanent blocker = addCreatureReady(player1, new GrizzlyBears());
        castToddlersRage(blocker);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLife(player2, 20);
        harness.setLife(player1, 20);

        resolveCombat(player2);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                attacker.getId(), 2, player2.getId(), 2));

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blocker);
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void tantrumRequiresLethalDamageBeforeAssigningDamageToAttackingPlayer() {
        Permanent blocker = addCreatureReady(player1, new GrizzlyBears());
        castToddlersRage(blocker);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLife(player2, 20);

        resolveCombat(player2);

        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(
                attacker.getId(), 1, player2.getId(), 3)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must assign lethal damage");

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                attacker.getId(), 2, player2.getId(), 2));
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void tantrumAllowsAllDamageToBeAssignedToAttacker() {
        Permanent blocker = addCreatureReady(player1, new GrizzlyBears());
        castToddlersRage(blocker);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLife(player2, 20);

        resolveCombat(player2);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(attacker.getId(), 4));

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blocker);
    }

    @Test
    void tantrumDoesNotAllowOverflowWhenAttacking() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        castToddlersRage(attacker);
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void canEnchantOpponentsCreatureWithoutBoostingOtherCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        castToddlersRage(opposingCreature);

        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.TANTRUM)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TANTRUM)).isFalse();
    }

    @Test
    void flashAllowsCastingDuringOpponentsUpkeep() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new ToddlersRage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passPriority(player2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Toddler's Rage");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TANTRUM)).isTrue();
    }

    private void castToddlersRage(Permanent target) {
        harness.setHand(player1, List.of(new ToddlersRage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
