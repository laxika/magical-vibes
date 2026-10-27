package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AbandonThePost;
import com.github.laxika.magicalvibes.cards.a.AerialManeuver;
import com.github.laxika.magicalvibes.cards.b.BorneUponAWind;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.d.DebtOfLoyalty;
import com.github.laxika.magicalvibes.cards.e.EerieInterference;
import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.cards.g.GildedLight;
import com.github.laxika.magicalvibes.cards.m.Microscope;
import com.github.laxika.magicalvibes.cards.s.SafePassage;
import com.github.laxika.magicalvibes.cards.s.Silence;
import com.github.laxika.magicalvibes.cards.s.Skullcrack;
import com.github.laxika.magicalvibes.cards.s.SkeletonScavengers;
import com.github.laxika.magicalvibes.cards.s.SoldeviSentry;
import com.github.laxika.magicalvibes.cards.s.SamiteHealer;
import com.github.laxika.magicalvibes.cards.t.TitanicGrowth;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RunedTerror.class, TitanicGrowth.class, TurnToFrog.class, AerialManeuver.class, AbandonThePost.class,
        SamiteHealer.class, Fog.class, SafePassage.class, EerieInterference.class, Skullcrack.class, Silence.class,
        GildedLight.class, BorneUponAWind.class, DebtOfLoyalty.class, SkeletonScavengers.class, SoldeviSentry.class,
        Microscope.class, DarksteelCitadel.class})
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

    @Test
    void fogFromOtherPlayersPhaseSurvivesActivePlayersCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.setHand(player2, List.of(new Fog()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0);
        assertThat(gd.preventAllCombatDamage).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.preventAllCombatDamage).isTrue();

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.preventAllCombatDamage).isFalse();
    }

    @Test
    void activePlayersOwnFogEndsAtTheirCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Fog()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gd.preventAllCombatDamage).isFalse();
    }

    @Test
    void playerDamagePreventionFromOtherPlayersPhaseSurvivesActivePlayersCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.setHand(player2, List.of(new SafePassage()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0);
        assertThat(gd.playersWithAllDamagePrevented).containsExactly(player2.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.playersWithAllDamagePrevented).containsExactly(player2.getId());

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.playersWithAllDamagePrevented).isEmpty();
    }

    @Test
    void matchingSourcePreventionFromOtherPlayersPhaseSurvivesActivePlayersCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.setHand(player2, List.of(new EerieInterference()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0);
        assertThat(gd.playersWithDamageFromMatchingSourcesPrevented.get(player2.getId())).hasSize(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.playersWithDamageFromMatchingSourcesPrevented.get(player2.getId())).hasSize(1);
        assertThat(gd.playersWithDamageToControlledCreaturesFromMatchingSourcesPrevented.get(player2.getId()))
                .hasSize(1);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.playersWithDamageFromMatchingSourcesPrevented).isEmpty();
        assertThat(gd.playersWithDamageToControlledCreaturesFromMatchingSourcesPrevented).isEmpty();
    }

    @Test
    void cantGainLifeFromOtherPlayersPhaseSurvivesActivePlayersCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.setHand(player2, List.of(new Skullcrack()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        assertThat(gd.playersCantGainLifeThisTurn).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.playersCantGainLifeThisTurn).isTrue();

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.playersCantGainLifeThisTurn).isFalse();
    }

    @Test
    void silenceFromOtherPlayersPhaseSurvivesActivePlayersCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.setHand(player2, List.of(new Silence()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player2, 0);
        assertThat(gd.playersSilencedThisTurn).containsExactly(player1.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.playersSilencedThisTurn).containsExactly(player1.getId());

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.playersSilencedThisTurn).isEmpty();
    }

    @Test
    void activePlayersOwnSilenceEndsAtTheirCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Silence()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0);
        assertThat(gd.playersSilencedThisTurn).containsExactly(player2.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gd.playersSilencedThisTurn).isEmpty();
    }

    @Test
    void playerKeywordFromOtherPlayersPhaseSurvivesActivePlayersCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.setHand(player2, List.of(new GildedLight()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0);
        assertThat(gd.playerKeywordsUntilEndOfTurn.get(player2.getId())).containsExactly(Keyword.SHROUD);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.playerKeywordsUntilEndOfTurn.get(player2.getId())).containsExactly(Keyword.SHROUD);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.playerKeywordsUntilEndOfTurn).isEmpty();
    }

    @Test
    void flashPermissionFromOtherPlayersPhaseSurvivesActivePlayersCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.setHand(player2, List.of(new BorneUponAWind()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0);
        assertThat(gd.playersWithFlashUntilEndOfTurn).containsExactly(player2.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.playersWithFlashUntilEndOfTurn).containsExactly(player2.getId());

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.playersWithFlashUntilEndOfTurn).isEmpty();
    }

    @Test
    void gainControlRegenerationShieldFromOtherPlayersPhaseKeepsItsRiderAfterActivePlayersCleanup() {
        Permanent terror = harness.addToBattlefieldAndReturn(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.setHand(player2, List.of(new DebtOfLoyalty()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player2, 0, terror.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(terror.getRegenerationShield()).isEqualTo(1);
        assertThat(terror.getGainControlRegenerationShields()).containsExactly(player2.getId());

        terror.setMarkedDamage(100);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(terror);
    }

    @Test
    void gainControlRegenerationShieldFromOtherPlayersPhaseEndsAtTheirCleanup() {
        Permanent terror = harness.addToBattlefieldAndReturn(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.setHand(player2, List.of(new DebtOfLoyalty()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player2, 0, terror.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        harness.passUntil(player2, TurnStep.CLEANUP);

        assertThat(terror.getRegenerationShield()).isZero();
        assertThat(terror.getGainControlRegenerationShields()).isEmpty();
    }

    @Test
    void plusOneCounterRegenerationShieldFromOtherPlayersPhaseKeepsItsRiderAfterActivePlayersCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        Permanent scavengers = addCreatureReady(player2, new SkeletonScavengers());
        scavengers.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        assertThat(scavengers.getPlusOnePlusOneCounterRegenerationShield()).isEqualTo(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(scavengers.getRegenerationShield()).isEqualTo(1);
        assertThat(scavengers.getPlusOnePlusOneCounterRegenerationShield()).isEqualTo(1);
        assertThat(scavengers.getPlusOnePlusOneCounterRegenerationShieldControllers())
                .containsExactly(player2.getId());

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(scavengers.getRegenerationShield()).isZero();
        assertThat(scavengers.getPlusOnePlusOneCounterRegenerationShield()).isZero();
        assertThat(scavengers.getPlusOnePlusOneCounterRegenerationShieldControllers()).isEmpty();
    }

    @Test
    void opponentDrawRegenerationShieldFromOtherPlayersPhaseKeepsItsRiderAfterActivePlayersCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        Permanent sentry = addCreatureReady(player2, new SoldeviSentry());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        assertThat(sentry.getOpponentDrawRegenerationShield()).isEqualTo(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(sentry.getRegenerationShield()).isEqualTo(1);
        assertThat(sentry.getOpponentDrawRegenerationShield()).isEqualTo(1);
        assertThat(sentry.getOpponentDrawRegenerationShieldRecipients()).containsExactly(player1.getId());
        assertThat(sentry.getOpponentDrawRegenerationShieldControllers()).containsExactly(player2.getId());

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(sentry.getOpponentDrawRegenerationShield()).isZero();
        assertThat(sentry.getOpponentDrawRegenerationShieldRecipients()).isEmpty();
        assertThat(sentry.getOpponentDrawRegenerationShieldControllers()).isEmpty();
    }

    @Test
    void graveyardCardAnimationFromOtherPlayersPhaseSurvivesActivePlayersCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        Permanent microscope = harness.addToBattlefieldAndReturn(player2, new Microscope());
        microscope.setSummoningSick(false);
        Card target = new DarksteelCitadel();
        harness.setGraveyard(player1, List.of(target));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.activateAbility(player2, 0, 1, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        assertThat(gqs.cardHasType(target, CardType.CREATURE, gd, player1.getId())).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gqs.cardHasType(target, CardType.CREATURE, gd, player1.getId())).isTrue();

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gqs.cardHasType(target, CardType.CREATURE, gd, player1.getId())).isFalse();
    }
}
