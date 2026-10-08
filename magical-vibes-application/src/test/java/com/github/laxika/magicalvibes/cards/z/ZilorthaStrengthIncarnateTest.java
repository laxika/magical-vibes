package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.y.YavimayaWurm;
import com.github.laxika.magicalvibes.cards.l.Lignify;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZilorthaStrengthIncarnate.class, GoblinPiker.class, GiantSpider.class, YavimayaWurm.class, Lignify.class})
class ZilorthaStrengthIncarnateTest extends BaseCardTest {

    @Test
    void ownCreaturesUsePowerToDetermineLethalDamage() {
        addCreatureReady(player1, new ZilorthaStrengthIncarnate());
        Permanent piker = addCreatureReady(player1, new GoblinPiker());

        piker.addMarkedDamage(null, 1);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(piker);
        assertThat(gqs.getLethalDamageThreshold(gd, piker)).isEqualTo(2);
    }

    @Test
    void opposingCreaturesStillUseToughnessForLethalDamage() {
        addCreatureReady(player1, new ZilorthaStrengthIncarnate());
        Permanent piker = addCreatureReady(player2, new GoblinPiker());

        piker.addMarkedDamage(null, 1);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(piker);
    }

    @Test
    void trampleUsesOpposingBlockersToughnessForLethalDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new YavimayaWurm());
        addCreatureReady(player1, new ZilorthaStrengthIncarnate());
        Permanent blocker = addCreatureReady(player2, new GiantSpider());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 4,
                player2.getId(), 2
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    void zilorthaItselfSurvivesUntilDamageReachesItsPower() {
        Permanent zilortha = addCreatureReady(player1, new ZilorthaStrengthIncarnate());

        zilortha.addMarkedDamage(null, 6);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(zilortha);

        zilortha.addMarkedDamage(null, 1);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(zilortha);
    }

    @Test
    void lowerPowerMakesDamageLethalBeforeToughnessIsReached() {
        addCreatureReady(player1, new ZilorthaStrengthIncarnate());
        Permanent spider = addCreatureReady(player1, new GiantSpider());

        spider.addMarkedDamage(null, 2);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(spider);
    }

    @Test
    void zeroPowerNeedsAtLeastOneMarkedDamage() {
        addCreatureReady(player1, new ZilorthaStrengthIncarnate());
        Permanent spider = addCreatureReady(player1, new GiantSpider());
        spider.setPowerModifier(-2);

        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spider);

        spider.addMarkedDamage(null, 1);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(spider);
    }

    @Test
    void zeroToughnessStillPutsCreatureIntoGraveyard() {
        addCreatureReady(player1, new ZilorthaStrengthIncarnate());
        Permanent piker = addCreatureReady(player1, new GoblinPiker());
        piker.setToughnessModifier(-1);

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(piker);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(piker.getCard());
    }

    @Test
    void losingZilorthaMakesPreviouslyNonlethalDamageLethal() {
        Permanent zilortha = addCreatureReady(player1, new ZilorthaStrengthIncarnate());
        Permanent piker = addCreatureReady(player1, new GoblinPiker());
        piker.addMarkedDamage(null, 1);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(piker);

        zilortha.addMarkedDamage(null, 7);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(zilortha, piker);
    }

    @Test
    void opposingTrampleUsesPowerOfZilorthasControllerBlocker() {
        harness.setLife(player1, 20);
        addCreatureReady(player1, new ZilorthaStrengthIncarnate());
        Permanent blocker = addCreatureReady(player1, new GiantSpider());
        addCreatureReady(player2, new YavimayaWurm());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));
        resolveCombat(player2);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player2, 0, Map.of(
                blocker.getId(), 2,
                player1.getId(), 4
        ));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blocker);
    }

    @Test
    void creatureLosingAbilitiesStillUsesPowerForLethalDamage() {
        addCreatureReady(player1, new ZilorthaStrengthIncarnate());
        Permanent spider = addCreatureReady(player1, new GiantSpider());
        Permanent lignify = harness.addToBattlefieldAndReturn(player2, new Lignify());
        lignify.setAttachedTo(spider.getId());

        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spider);

        spider.addMarkedDamage(null, 1);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(spider);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spider.getCard());
    }
}
