package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VengefulAncestor.class, SakuraTribeElder.class})
class VengefulAncestorTest extends BaseCardTest {

    @Test
    void entersAndGoadsTargetCreature() {
        Permanent target = addCreatureReady(player2, new SakuraTribeElder());

        castVengefulAncestor(target);

        assertThat(gqs.isGoaded(gd, target)).isTrue();
    }

    @Test
    void goadedCreatureDealsDamageToItsControllerWhenItAttacks() {
        Permanent target = addCreatureReady(player2, new SakuraTribeElder());
        castVengefulAncestor(target);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void attackingGoadsAnotherCreature() {
        Permanent firstTarget = addCreatureReady(player2, new SakuraTribeElder());
        Permanent secondTarget = addCreatureReady(player2, new SakuraTribeElder());
        castVengefulAncestor(firstTarget);
        findPermanent(player1, "Vengeful Ancestor").setSummoningSick(false);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, secondTarget.getId());
            resolveAllTriggers();
        });

        assertThat(gqs.isGoaded(gd, firstTarget)).isTrue();
        assertThat(gqs.isGoaded(gd, secondTarget)).isTrue();
    }

    @Test
    void ungoadedAttackerDoesNotDealDamageToItsController() {
        Permanent goaded = addCreatureReady(player2, new SakuraTribeElder());
        addCreatureReady(player1, new SakuraTribeElder());
        castVengefulAncestor(goaded);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void ownGoadedAttackerDealsDamageToItsController() {
        Permanent target = addCreatureReady(player1, new SakuraTribeElder());
        castVengefulAncestor(target);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void eachGoadedAttackerDealsOneDamage() {
        Permanent firstTarget = addCreatureReady(player2, new SakuraTribeElder());
        Permanent secondTarget = addCreatureReady(player2, new SakuraTribeElder());
        castVengefulAncestor(firstTarget);
        castVengefulAncestor(secondTarget);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0, 1));
            resolveAllTriggers();
        });

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void goadExpiresAtStartOfControllersNextTurn() {
        Permanent target = addCreatureReady(player2, new SakuraTribeElder());
        castVengefulAncestor(target);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gqs.isGoaded(gd, target)).isTrue();
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);
        resolveAllTriggers();
        harness.passUntilWithNoAttackers(player1, TurnStep.UPKEEP);

        assertThat(gqs.isGoaded(gd, target)).isFalse();
    }

    @Test
    void sacrificedGoadedAttackerStillDealsDamageToItsLastController() {
        Permanent target = addCreatureReady(player2, new SakuraTribeElder());
        harness.setLibrary(player2, List.of());
        castVengefulAncestor(target);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0));
            harness.activateAbility(player2, 0, null, null);
            harness.assertInGraveyard(player2, "Sakura-Tribe Elder");
            resolveAllTriggers();
        });

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    private void castVengefulAncestor(Permanent target) {
        harness.setHand(player1, List.of(new VengefulAncestor()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
