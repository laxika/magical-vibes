package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaseOfTheMarketMelee.class, GiantSpider.class, Shock.class})
class CaseOfTheMarketMeleeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to its ETB target")
    void dealsDamageWhenItEnters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent casePermanent = castCase(target.getId());

        assertThat(casePermanent).isNotNull();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Solves at the beginning of the end step when three creatures are damaged")
    void solvesWithThreeDamagedCreatures() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(player1, new CaseOfTheMarketMelee());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        first.setMarkedDamage(1);
        second.setMarkedDamage(1);
        third.setMarkedDamage(1);
        resolveEndStepTriggers();

        assertThat(casePermanent.isSolved()).isTrue();
    }

    @Test
    @DisplayName("Its static ability keeps creature damage marked through cleanup")
    void damageRemainsMarkedThroughCleanup() {
        harness.addToBattlefield(player1, new CaseOfTheMarketMelee());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        creature.setMarkedDamage(2);

        harness.inMutationScope(() ->
                GameTestEngineContext.get().getBean(TurnCleanupService.class).resetEndOfTurnModifiers(gd));

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("When solved, deals damage equal to the number of attacking creatures divided as chosen")
    void solvedAttackTriggerDealsAttackerCountDividedAsChosen() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(player1, new CaseOfTheMarketMelee());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent thirdTarget = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        addReadyCreature(player1);
        addReadyCreature(player1);
        addReadyCreature(player1);
        dealDamage(firstTarget, secondTarget, thirdTarget);
        resolveEndStepTriggers();
        assertThat(casePermanent.isSolved()).isTrue();

        declareAttackers(List.of(1, 2, 3));

        PendingInteraction.MultiPermanentChoice targets =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(targets).isNotNull();
        assertThat(targets.validPlayerIds()).contains(player2.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 3);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    private Permanent castCase(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new CaseOfTheMarketMelee()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();
        resolveAllTriggers();
        return findPermanent(player1, "Case of the Market Melee");
    }

    private void dealDamage(Permanent... targets) {
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, targets.length);
        for (Permanent target : targets) {
            harness.castInstant(player1, 0, target.getId());
            harness.passBothPriorities();
        }
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = new Permanent(new GiantSpider());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void resolveEndStepTriggers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        while (!gd.stack.isEmpty() && !gd.interaction.isAwaitingInput()) {
            harness.passBothPriorities();
        }
    }
}
