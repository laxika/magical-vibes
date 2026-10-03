package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AbbeyGriffin;
import com.github.laxika.magicalvibes.cards.f.FortressCrab;
import com.github.laxika.magicalvibes.cards.m.MomentOfHeroism;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BalefireDragon.class, WalkingCorpse.class, AbbeyGriffin.class, FortressCrab.class,
        MomentOfHeroism.class})
class BalefireDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 6 damage to each creature the damaged player controls when dealing combat damage")
    void dealsDamageToEachCreatureOnCombatDamage() {
        harness.setLife(player2, 20);
        Permanent dragon = addCreatureReady(player1, new BalefireDragon());
        dragon.setAttacking(true);

        // Opponent has two 2/2 creatures on the battlefield (not blocking)
        Permanent bear1 = addCreatureReady(player2, new WalkingCorpse());
        Permanent bear2 = addCreatureReady(player2, new WalkingCorpse());

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        resolveCombat();
        resolveAllTriggers();

        // Dragon deals 6 combat damage to player2
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);

        // Both 2/2 creatures die from lethal damage.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(bear1, bear2);
    }

    @Test
    @DisplayName("Deals 6 damage to a seven-toughness creature that survives")
    void highToughnessCreatureSurvives() {
        harness.setLife(player2, 20);
        Permanent dragon = addCreatureReady(player1, new BalefireDragon());
        dragon.setAttacking(true);

        Permanent wall = addCreatureReady(player2, new FortressCrab());
        wall.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        resolveCombat();
        resolveAllTriggers();

        // Dragon deals 6 combat damage to player2
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(wall);
        assertThat(wall.getMarkedDamage()).isEqualTo(6);
    }

    @Test
    @DisplayName("No trigger when dragon is blocked and deals no player damage")
    void noTriggerWhenBlocked() {
        harness.setLife(player2, 20);
        Permanent dragon = addCreatureReady(player1, new BalefireDragon());
        dragon.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new AbbeyGriffin());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        // Another creature that should NOT be damaged
        Permanent bear = addCreatureReady(player2, new WalkingCorpse());

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        resolveCombat();
        resolveAllTriggers();

        // No combat damage to player
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        // Bear should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
    }

    @Test
    @DisplayName("Does not damage creatures the attacker controls")
    void doesNotDamageAttackerCreatures() {
        harness.setLife(player2, 20);
        Permanent dragon = addCreatureReady(player1, new BalefireDragon());
        dragon.setAttacking(true);

        // Attacker's own creature should not be affected
        Permanent ownBear = addCreatureReady(player1, new WalkingCorpse());

        // Opponent has a creature
        Permanent oppBear = addCreatureReady(player2, new WalkingCorpse());

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        resolveCombat();
        resolveAllTriggers();

        // Attacker's creature should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownBear);

        // Opponent's creature should be destroyed
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(oppBear);
    }

    @Test
    @DisplayName("Trigger still fires when opponent has no creatures")
    void triggerFiresWithNoOpponentCreatures() {
        harness.setLife(player2, 20);
        Permanent dragon = addCreatureReady(player1, new BalefireDragon());
        dragon.setAttacking(true);

        // No creatures for opponent
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        // Just combat damage to player
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Sweep uses combat damage dealt even if the Dragon's power changes before resolution")
    void damageAmountIsFixedAtCombatDamage() {
        Permanent dragon = addCreatureReady(player1, new BalefireDragon());
        dragon.setAttacking(true);
        Permanent crab = addCreatureReady(player2, new FortressCrab());
        crab.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new MomentOfHeroism()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();
        harness.assertLife(player2, 14);
        assertThat(gd.stack).hasSize(1);
        harness.castInstant(player1, 0, dragon.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(crab);
        assertThat(crab.getMarkedDamage()).isEqualTo(6);
    }

    @Test
    @DisplayName("Lifelink granted in response applies to the Dragon's sweep")
    void sweepUsesDragonsCurrentLifelink() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent dragon = addCreatureReady(player1, new BalefireDragon());
        dragon.setAttacking(true);
        Permanent corpse = addCreatureReady(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new MomentOfHeroism()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 14);
        assertThat(gd.stack).hasSize(1);
        harness.castInstant(player1, 0, dragon.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(corpse);
        harness.assertLife(player1, 26);
    }

    @Test
    @DisplayName("Sweep damages creatures present at resolution, including ones that entered after combat damage")
    void damagesCreaturesPresentAtResolution() {
        Permanent dragon = addCreatureReady(player1, new BalefireDragon());
        dragon.setAttacking(true);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);
        Permanent corpse = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(corpse);
        harness.assertInGraveyard(player2, "Walking Corpse");
    }
}
