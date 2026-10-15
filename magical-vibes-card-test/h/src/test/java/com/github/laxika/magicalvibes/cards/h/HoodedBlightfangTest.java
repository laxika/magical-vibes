package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GarrukUnleashed;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HoodedBlightfang.class, GrizzlyBears.class, ProdigalPyromancer.class, GarrukUnleashed.class})
class HoodedBlightfangTest extends BaseCardTest {

    @Test
    void deathtouchAttackerCausesLifeLossAndLifeGain() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addReadyBlightfang(player1);

        declareAttackers(player1, List.of(0));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void doesNotTriggerForNonDeathtouchAttacker() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addReadyBlightfang(player1);
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void deathtouchCombatDamageDestroysPlaneswalker() {
        addReadyBlightfang(player1);
        addReadyDeathtouchCreature(player1);
        Permanent planeswalker = addPlaneswalker(player2, 4);

        declareAttackers(player1, List.of(1), Map.of(1, planeswalker.getId()));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(planeswalker);
    }

    @Test
    void deathtouchNoncombatDamageDestroysPlaneswalker() {
        addReadyBlightfang(player1);
        addReadyDeathtouchCreature(player1);
        Permanent planeswalker = addPlaneswalker(player2, 4);

        harness.activateAbility(player1, 1, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(planeswalker);
    }

    @Test
    void eachDeathtouchAttackerTriggersSeparately() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addReadyBlightfang(player1);
        addReadyDeathtouchCreature(player1);

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    void blightfangsOwnCombatDamageDestroysPlaneswalker() {
        addReadyBlightfang(player1);
        Permanent planeswalker = addPlaneswalker(player2, 4);

        declareAttackers(player1, List.of(0), Map.of(0, planeswalker.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(planeswalker);
    }

    @Test
    void opposingDeathtouchAttackerDoesNotTriggerBlightfang() {
        addReadyBlightfang(player1);
        addReadyDeathtouchCreature(player2);

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void nonDeathtouchDamageDoesNotDestroyPlaneswalker() {
        addReadyBlightfang(player1);
        addCreatureReady(player1, new ProdigalPyromancer());
        Permanent planeswalker = addPlaneswalker(player2, 4);

        harness.activateAbility(player1, 1, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(planeswalker);
    }

    @Test
    void opposingDeathtouchDamageDoesNotTriggerBlightfang() {
        addReadyBlightfang(player1);
        Permanent planeswalker = addPlaneswalker(player1, 4);
        addReadyDeathtouchCreature(player2);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(planeswalker);
    }

    @Test
    void deathtouchDamageAlsoDestroysYourOwnPlaneswalker() {
        addReadyBlightfang(player1);
        addReadyDeathtouchCreature(player1);
        Permanent planeswalker = addPlaneswalker(player1, 4);

        harness.activateAbility(player1, 1, null, planeswalker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(planeswalker);
    }

    @Test
    void destructionDoesNotTargetTheDamagedPlaneswalker() {
        addReadyBlightfang(player1);
        addReadyDeathtouchCreature(player1);
        Permanent planeswalker = addPlaneswalker(player2, 4);

        harness.activateAbility(player1, 1, null, planeswalker.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        planeswalker.getPersistentGrantedKeywords().add(Keyword.HEXPROOF);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(planeswalker);
    }

    @Test
    void indestructiblePlaneswalkerSurvivesDestructionTrigger() {
        addReadyBlightfang(player1);
        addReadyDeathtouchCreature(player1);
        Permanent planeswalker = addPlaneswalker(player2, 4);
        planeswalker.getPersistentGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        harness.activateAbility(player1, 1, null, planeswalker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(planeswalker);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }

    private Permanent addReadyBlightfang(Player player) {
        return addCreatureReady(player, new HoodedBlightfang());
    }

    private Permanent addReadyDeathtouchCreature(Player player) {
        Permanent permanent = addCreatureReady(player, new ProdigalPyromancer());
        permanent.getPersistentGrantedKeywords().add(Keyword.DEATHTOUCH);
        return permanent;
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GarrukUnleashed());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        return permanent;
    }
}
