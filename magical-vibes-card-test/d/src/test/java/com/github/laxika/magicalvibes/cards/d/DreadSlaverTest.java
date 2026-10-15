package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.n.NaturalAffinity;
import com.github.laxika.magicalvibes.cards.u.UlvenwaldTracker;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DreadSlaver.class, GrizzlyBears.class, CruelEdict.class,
        UlvenwaldTracker.class, NaturalAffinity.class, Forest.class, Humility.class})
class DreadSlaverTest extends BaseCardTest {

    /** Dread Slaver attacks, Grizzly Bears blocks and dies to the combat damage. */
    private void slaverKillsBearsInCombat() {
        Permanent slaver = harness.addToBattlefieldAndReturn(player1, new DreadSlaver());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        slaver.setSummoningSick(false);
        slaver.setAttacking(true);

        bears.setSummoningSick(false);
        bears.setBlocking(true);
        bears.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_OF_COMBAT);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("A creature the Slaver kills returns under its controller immediately")
    void returnsDamagedCreatureUnderControl() {
        slaverKillsBearsInCombat();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The returned creature is a black Zombie in addition to its other colors and types")
    void returnedCreatureIsBlackZombie() {
        slaverKillsBearsInCombat();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectiveColors(gd, bears)).contains(CardColor.BLACK, CardColor.GREEN);
        assertThat(GameQueryService.permanentHasSubtype(bears, CardSubtype.ZOMBIE)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(bears, CardSubtype.BEAR)).isTrue();
    }

    @Test
    @DisplayName("A creature the Slaver never damaged is not returned when it dies")
    void noReturnForUndamagedCreature() {
        harness.addToBattlefield(player1, new DreadSlaver());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Non-combat damage from the Slaver also enables the return")
    void returnsCreatureDamagedOutsideCombat() {
        Permanent slaver = harness.addToBattlefieldAndReturn(player1, new DreadSlaver());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.creatureCardsDamagedThisTurnBySourcePermanent
                .computeIfAbsent(slaver.getId(), ignored -> ConcurrentHashMap.newKeySet())
                .add(bears.getCard().getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Actual fight damage enables Dread Slaver's return trigger")
    void returnsCreatureKilledByFightDamage() {
        Permanent tracker = harness.addToBattlefieldAndReturn(player1, new UlvenwaldTracker());
        tracker.setSummoningSick(false);
        Permanent slaver = harness.addToBattlefieldAndReturn(player1, new DreadSlaver());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(slaver.getId(), bears.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(slaver.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Both Slavers trigger when they die simultaneously in combat")
    void triggersWhenSourceDiesAtSameTime() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new DreadSlaver());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new DreadSlaver());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setMarkedDamage(2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        blocker.setMarkedDamage(2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Dread Slaver").getCard()).isSameAs(blocker.getCard());
        assertThat(findPermanent(player2, "Dread Slaver").getCard()).isSameAs(attacker.getCard());
        harness.assertNotInGraveyard(player1, "Dread Slaver");
        harness.assertNotInGraveyard(player2, "Dread Slaver");
    }

    @Test
    @DisplayName("An animated land killed by Dread Slaver returns even though its card is not a creature")
    void returnsAnimatedLandKilledByFight() {
        Permanent tracker = harness.addToBattlefieldAndReturn(player1, new UlvenwaldTracker());
        tracker.setSummoningSick(false);
        Permanent slaver = harness.addToBattlefieldAndReturn(player1, new DreadSlaver());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new NaturalAffinity()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0);
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(slaver.getId(), forest.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotInGraveyard(player2, "Forest");
        assertThat(gqs.getEffectiveColors(gd, findPermanent(player1, "Forest"))).contains(CardColor.BLACK);
    }

    @Test
    @DisplayName("Dread Slaver cannot trigger while Humility removes its abilities")
    void doesNotReturnCreatureWhenAbilitiesAreRemoved() {
        Permanent slaver = harness.addToBattlefieldAndReturn(player1, new DreadSlaver());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Humility());
        slaver.setSummoningSick(false);
        slaver.setAttacking(true);
        bears.setBlocking(true);
        bears.addBlockingTarget(0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertInGraveyard(player1, "Dread Slaver");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }
}
