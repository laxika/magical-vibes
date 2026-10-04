package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({GhostSpiderGwenStacy.class, GrizzlyBears.class, JaceBeleren.class, Boomerang.class})
class GhostSpiderGwenStacyTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking alone deals 1 damage to the defending player")
    void attackingAloneDealsOneDamage() {
        addCreatureReady(player1, new GhostSpiderGwenStacy());

        declareAttackers(player1, List.of(0));
        resolveAttackTrigger();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals damage equal to the number of attacking creatures")
    void damageScalesWithAttackingCreatures() {
        addCreatureReady(player1, new GhostSpiderGwenStacy());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAttackTrigger();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Attacking a planeswalker still damages its defending player")
    void attackingPlaneswalkerDamagesItsController() {
        addCreatureReady(player1, new GhostSpiderGwenStacy());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);

        declareAttackers(player1, List.of(0), Map.of(0, planeswalker.getId()));
        resolveAttackTrigger();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices,
                                  Map<Integer, UUID> attackTargets) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices, attackTargets);
    }

    private void resolveAttackTrigger() {
        harness.passUntil(TurnStep.DECLARE_BLOCKERS);
    }

    @Test
    void nonattackingCreaturesDoNotIncreaseDamage() {
        addCreatureReady(player1, new GhostSpiderGwenStacy());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAttackTrigger();

        harness.assertLife(player2, 19);
    }

    @Test
    void countsRemainingAttackersAtResolution() {
        addCreatureReady(player1, new GhostSpiderGwenStacy());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, bear.getId());
        resolveAttackTrigger();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertLife(player2, 19);
    }

    @Test
    void triggerStillDealsDamageAfterGhostSpiderLeavesCombat() {
        Permanent ghostSpider = addCreatureReady(player1, new GhostSpiderGwenStacy());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, ghostSpider.getId());
        resolveAttackTrigger();

        harness.assertInHand(player1, "Ghost-Spider, Gwen Stacy");
        harness.assertLife(player2, 19);
    }

    @Test
    void stillDamagesDefendingPlayerAfterAttackedPlaneswalkerLeaves() {
        addCreatureReady(player1, new GhostSpiderGwenStacy());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);

        declareAttackers(player1, List.of(0), Map.of(0, planeswalker.getId()));
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, planeswalker.getId());
        resolveAttackTrigger();

        harness.assertInHand(player2, "Jace Beleren");
        harness.assertLife(player2, 19);
    }
}
