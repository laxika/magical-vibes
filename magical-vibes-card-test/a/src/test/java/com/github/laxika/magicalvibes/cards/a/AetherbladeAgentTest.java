package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GitaxianMindstinger;
import com.github.laxika.magicalvibes.cards.i.InvasionOfInnistrad;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AetherbladeAgent.class, GitaxianMindstinger.class, Forest.class, InvasionOfInnistrad.class})
class AetherbladeAgentTest extends BaseCardTest {

    @Test
    void transformsByPayingBlueMana() {
        Permanent agent = addAgent();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(agent.isTransformed()).isTrue();
        assertThat(agent.getCard()).isInstanceOf(GitaxianMindstinger.class);
    }

    @Test
    void canPayPhyrexianManaWithLife() {
        Permanent agent = addAgent();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(agent.isTransformed()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void canOnlyTransformAtSorcerySpeed() {
        addAgent();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void drawsWhenTransformedFaceDealsCombatDamageToPlayer() {
        Permanent agent = addAgent();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        agent.setSummoningSick(false);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        agent.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void drawsWhenTransformedFaceDealsCombatDamageToBattle() {
        Permanent agent = addAgent();
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfInnistrad());
        battle.setProtectorPlayerId(player2.getId());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        agent.setSummoningSick(false);
        agent.setAttacking(true);
        agent.setAttackTarget(battle.getId());

        resolveCombat();
        resolveAllTriggers();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(2);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void frontFaceDoesNotDrawWhenDealingCombatDamageToPlayer() {
        Permanent agent = addCreatureReady(player1, new AetherbladeAgent());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        agent.setAttacking(true);
        agent.setAttackTarget(player2.getId());

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotTransformDuringCombat() {
        addAgent();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void deathtouchKillsLargerBlockerWithoutDrawing() {
        Permanent agent = addCreatureReady(player1, new AetherbladeAgent());
        Permanent blocker = addCreatureReady(player2, new GitaxianMindstinger());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        agent.setAttacking(true);
        agent.setAttackTarget(player2.getId());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(agent);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player2, 20);
    }

    private Permanent addAgent() {
        return harness.addToBattlefieldAndReturn(player1, new AetherbladeAgent());
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
