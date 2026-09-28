package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MindbladeRender.class, ElvishWarrior.class, GrizzlyBears.class, Forest.class})
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
}
