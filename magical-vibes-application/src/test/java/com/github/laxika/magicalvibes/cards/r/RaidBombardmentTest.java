package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.EnclaveCryptologist;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.k.KozileksPredator;
import com.github.laxika.magicalvibes.cards.s.SarkhanTheMad;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RaidBombardment.class, EnclaveCryptologist.class, GlorySeeker.class,
        KozileksPredator.class, SarkhanTheMad.class})
class RaidBombardmentTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers for each attacking creature with power 2 or less")
    void triggersForSmallAttackingCreatures() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new RaidBombardment());
        addCreatureReady(player1, new GlorySeeker());
        addCreatureReady(player1, new EnclaveCryptologist());

        declareAttackers(List.of(1, 2));

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Does not trigger for an attacking creature with power greater than 2")
    void doesNotTriggerForLargeAttacker() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new RaidBombardment());
        addCreatureReady(player1, new KozileksPredator());

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damages the planeswalker attacked by a small creature")
    void damagesAttackedPlaneswalker() {
        harness.addToBattlefield(player1, new RaidBombardment());
        addCreatureReady(player1, new EnclaveCryptologist());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new SarkhanTheMad());
        planeswalker.setCounterCount(CounterType.LOYALTY, 7);

        declareAttackers(player1, List.of(1), Map.of(1, planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    void eachTriggerDamagesItsOwnAttackDestination() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new RaidBombardment());
        addCreatureReady(player1, new EnclaveCryptologist());
        addCreatureReady(player1, new EnclaveCryptologist());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new SarkhanTheMad());
        planeswalker.setCounterCount(CounterType.LOYALTY, 7);

        declareAttackers(player1, List.of(1, 2),
                Map.of(1, player2.getId(), 2, planeswalker.getId()));

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    void departedPlaneswalkerDoesNotRedirectDamageToItsController() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new RaidBombardment());
        addCreatureReady(player1, new EnclaveCryptologist());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new SarkhanTheMad());
        planeswalker.setCounterCount(CounterType.LOYALTY, 7);

        declareAttackers(player1, List.of(1), Map.of(1, planeswalker.getId()));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player2.getId()).remove(planeswalker);
        gd.playerGraveyards.get(player2.getId()).add(planeswalker.getCard());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void doesNotTriggerForOpponentsAttacker() {
        harness.addToBattlefield(player1, new RaidBombardment());
        addCreatureReady(player2, new EnclaveCryptologist());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void checksModifiedPowerWhenAttackerIsDeclared() {
        harness.addToBattlefield(player1, new RaidBombardment());
        Permanent attacker = addCreatureReady(player1, new GlorySeeker());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void negativePowerAttackerStillTriggers() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new RaidBombardment());
        Permanent attacker = addCreatureReady(player1, new EnclaveCryptologist());
        attacker.setPowerModifier(-1);

        declareAttackers(List.of(1));

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void increasingPowerAfterTriggeringDoesNotStopDamage() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new RaidBombardment());
        Permanent attacker = addCreatureReady(player1, new EnclaveCryptologist());
        addCreatureReady(player2, new KozileksPredator());

        declareAttackers(List.of(1));
        assertThat(gd.stack).hasSize(1);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void triggerResolvesAfterSourceAndAttackerLeave() {
        harness.setLife(player2, 20);
        Permanent bombardment = harness.addToBattlefieldAndReturn(player1, new RaidBombardment());
        Permanent attacker = addCreatureReady(player1, new EnclaveCryptologist());

        declareAttackers(List.of(1));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).removeAll(List.of(bombardment, attacker));
        gd.playerGraveyards.get(player1.getId()).addAll(List.of(bombardment.getCard(), attacker.getCard()));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices, Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }

}
