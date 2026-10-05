package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MindbladeRender.class, ElvishWarrior.class, GrizzlyBears.class, Forest.class, MirrorStrike.class})
class MindbladeRenderTest extends BaseCardTest {

    @Test
    void WarriorCombatDamageDrawsAndLosesLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        addCreatureReady(player1, new MindbladeRender());
        Permanent attacker = addCreatureReady(player1, new ElvishWarrior());
        attacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void MultipleWarriorsTriggerOnlyOnce() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        addCreatureReady(player1, new MindbladeRender());
        Permanent firstAttacker = addCreatureReady(player1, new ElvishWarrior());
        Permanent secondAttacker = addCreatureReady(player1, new ElvishWarrior());
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void NonWarriorCombatDamageDoesNotTrigger() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        addCreatureReady(player1, new MindbladeRender());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void ownCombatDamageDrawsAndLosesLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        Permanent render = addCreatureReady(player1, new MindbladeRender());
        render.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void simultaneousWarriorAndNonWarriorDamageTriggersOnce() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        addCreatureReady(player1, new MindbladeRender());
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        warrior.setAttacking(true);
        bear.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 16);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void eachRenderTriggersIndependently() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        addCreatureReady(player1, new MindbladeRender());
        addCreatureReady(player1, new MindbladeRender());
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());
        warrior.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void opponentWarriorDamagingControllerDoesNotTrigger() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        addCreatureReady(player1, new MindbladeRender());
        Permanent warrior = addCreatureReady(player2, new ElvishWarrior());
        warrior.setAttacking(true);

        resolveCombat(player2);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed({MindbladeRender.class, ElvishWarrior.class, Forest.class, MirrorStrike.class})
    void opponentWarriorDamageRedirectedToOpponentTriggers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new Forest()));
        addCreatureReady(player1, new MindbladeRender());
        Permanent warrior = addCreatureReady(player2, new ElvishWarrior());
        warrior.setAttacking(true);
        warrior.setAttackTarget(player1.getId());

        harness.setHand(player1, List.of(new MirrorStrike()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0, warrior.getId());
        harness.passBothPriorities();
        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 19);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
