package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AbandonThePost;
import com.github.laxika.magicalvibes.cards.a.AerialManeuver;
import com.github.laxika.magicalvibes.cards.s.SamiteHealer;
import com.github.laxika.magicalvibes.cards.t.TitanicGrowth;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RunedTerror.class, TitanicGrowth.class, TurnToFrog.class, AerialManeuver.class, AbandonThePost.class,
        SamiteHealer.class})
class RunedTerrorTest extends BaseCardTest {

    @Test
    void playersTakeBeginningAndMainPhasesSequentially() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();

        harness.getGameService().advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.currentStep).isEqualTo(TurnStep.UNTAP);

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);

        harness.getGameService().advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
    }

    @Test
    void losingAbilitiesRestoresNormalPhaseProgression() {
        Permanent terror = harness.addToBattlefieldAndReturn(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, terror.getId());

        gs.advanceStep(gd);

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
    }

    @Test
    void temporaryBoostSurvivesOtherPlayersCleanup() {
        Permanent terror = harness.addToBattlefieldAndReturn(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        harness.setHand(player2, List.of(new TitanicGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        int originalPower = gqs.getEffectivePower(gd, terror);
        harness.castAndResolveInstant(player2, 0, terror.getId());
        assertThat(gqs.getEffectivePower(gd, terror)).isEqualTo(originalPower + 4);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gqs.getEffectivePower(gd, terror)).isEqualTo(originalPower + 4);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gqs.getEffectivePower(gd, terror)).isEqualTo(originalPower);
    }

    @Test
    void temporaryKeywordGrantSurvivesOtherPlayersCleanup() {
        Permanent terror = harness.addToBattlefieldAndReturn(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.setHand(player2, List.of(new AerialManeuver()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        int originalPower = gqs.getEffectivePower(gd, terror);
        harness.castAndResolveInstant(player2, 0, terror.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gqs.hasKeyword(gd, terror, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, terror, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, terror)).isEqualTo(originalPower + 1);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gqs.hasKeyword(gd, terror, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, terror, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, terror)).isEqualTo(originalPower);
    }

    @Test
    void activePlayersOwnBoostEndsAtTheirCleanup() {
        Permanent terror = harness.addToBattlefieldAndReturn(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new TitanicGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int originalPower = gqs.getEffectivePower(gd, terror);
        harness.castAndResolveInstant(player1, 0, terror.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, terror)).isEqualTo(originalPower);
    }

    @Test
    void removalLetsCurrentActivePlayerContinueNormally() {
        Permanent terror = harness.addToBattlefieldAndReturn(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(terror);
        gd.playerHands.get(player1.getId()).add(terror.getCard());

        gs.advanceStep(gd);

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
    }

    @Test
    void cantBlockFromOtherPlayersPhaseSurvivesActivePlayersCleanup() {
        Permanent terror = harness.addToBattlefieldAndReturn(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        harness.setHand(player2, List.of(new AbandonThePost()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player2, 0, List.of(terror.getId()));
        assertThat(terror.isCantBlockThisTurn()).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(terror.isCantBlockThisTurn()).isTrue();

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(terror.isCantBlockThisTurn()).isFalse();
    }

    @Test
    void activePlayersOwnCantBlockEndsAtTheirCleanup() {
        Permanent terror = harness.addToBattlefieldAndReturn(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AbandonThePost()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, List.of(terror.getId()));

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(terror.isCantBlockThisTurn()).isFalse();
    }

    @Test
    void creaturePreventionShieldFromOtherPlayersPhaseSurvivesActivePlayersCleanup() {
        Permanent terror = harness.addToBattlefieldAndReturn(player1, new RunedTerror());
        addCreatureReady(player2, new SamiteHealer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.activateAbility(player2, 0, null, terror.getId());
        harness.passBothPriorities();
        assertThat(terror.getDamagePreventionShield()).isEqualTo(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(terror.getDamagePreventionShield()).isEqualTo(1);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(terror.getDamagePreventionShield()).isZero();
    }

    @Test
    void usedUpPreventionShieldIsNotRestoredAtOtherPlayersCleanup() {
        Permanent terror = harness.addToBattlefieldAndReturn(player1, new RunedTerror());
        addCreatureReady(player2, new SamiteHealer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.activateAbility(player2, 0, null, terror.getId());
        harness.passBothPriorities();
        terror.setDamagePreventionShield(0);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(terror.getDamagePreventionShield()).isZero();
    }

    @Test
    void playerPreventionShieldFromOtherPlayersPhaseSurvivesActivePlayersCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        addCreatureReady(player2, new SamiteHealer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.activateAbility(player2, 0, null, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(1);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
    }
}
