package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AbandonThePost;
import com.github.laxika.magicalvibes.cards.a.ActOnImpulse;
import com.github.laxika.magicalvibes.cards.a.AegisOfHonor;
import com.github.laxika.magicalvibes.cards.a.AerialManeuver;
import com.github.laxika.magicalvibes.cards.a.AngelOfGrace;
import com.github.laxika.magicalvibes.cards.a.AngelsGrace;
import com.github.laxika.magicalvibes.cards.a.ArkOfHunger;
import com.github.laxika.magicalvibes.cards.a.AuriokReplica;
import com.github.laxika.magicalvibes.cards.a.AweStrike;
import com.github.laxika.magicalvibes.cards.b.BlindFury;
import com.github.laxika.magicalvibes.cards.b.BorneUponAWind;
import com.github.laxika.magicalvibes.cards.c.Comeuppance;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.d.DebtOfLoyalty;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DueRespect;
import com.github.laxika.magicalvibes.cards.e.EerieInterference;
import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.cards.g.GildedLight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Inkshield;
import com.github.laxika.magicalvibes.cards.i.IrencragFeat;
import com.github.laxika.magicalvibes.cards.k.KaylasMusicBox;
import com.github.laxika.magicalvibes.cards.m.Microscope;
import com.github.laxika.magicalvibes.cards.o.Overblaze;
import com.github.laxika.magicalvibes.cards.o.Overmaster;
import com.github.laxika.magicalvibes.cards.p.PastInFlames;
import com.github.laxika.magicalvibes.cards.q.Quicken;
import com.github.laxika.magicalvibes.cards.s.SafePassage;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Silence;
import com.github.laxika.magicalvibes.cards.s.Skullcrack;
import com.github.laxika.magicalvibes.cards.s.SkeletonScavengers;
import com.github.laxika.magicalvibes.cards.s.SoldeviSentry;
import com.github.laxika.magicalvibes.cards.s.SamiteHealer;
import com.github.laxika.magicalvibes.cards.s.SenTriplets;
import com.github.laxika.magicalvibes.cards.t.TitanicGrowth;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.cards.v.VengefulArchon;
import com.github.laxika.magicalvibes.cards.v.VoharVodalianDesecrator;
import com.github.laxika.magicalvibes.cards.w.Wish;
import com.github.laxika.magicalvibes.cards.z.ZameckGuildmage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DamageRedirectShield;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RunedTerror.class, TitanicGrowth.class, TurnToFrog.class, AerialManeuver.class, AbandonThePost.class,
        SamiteHealer.class, Fog.class, SafePassage.class, EerieInterference.class, Skullcrack.class, Silence.class,
        GildedLight.class, BorneUponAWind.class, DebtOfLoyalty.class, SkeletonScavengers.class, SoldeviSentry.class,
        Microscope.class, DarksteelCitadel.class, BlindFury.class, Overblaze.class, GrizzlyBears.class,
        ZameckGuildmage.class, DueRespect.class, AngelsGrace.class, AngelOfGrace.class, IrencragFeat.class,
        Comeuppance.class, Inkshield.class, AegisOfHonor.class, Shock.class, Quicken.class, Divination.class,
        PastInFlames.class, Overmaster.class, AuriokReplica.class, RepeatedReverberation.class,
        SenTriplets.class, ActOnImpulse.class, ArkOfHunger.class, KaylasMusicBox.class, VengefulArchon.class,
        AweStrike.class, VoharVodalianDesecrator.class, Wish.class, Forest.class})
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

    @Test
    void combatDamageDoublingFromOtherPlayersPhaseSurvivesActivePlayersCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.setHand(player2, List.of(new BlindFury()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0);
        assertThat(gd.combatDamageToCreaturesDoublingsThisTurn).isEqualTo(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.combatDamageToCreaturesDoublingsThisTurn).isEqualTo(1);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.combatDamageToCreaturesDoublingsThisTurn).isZero();
    }

    @Test
    void permanentDamageDoublingsAreSplitBetweenTheActivePlayersTheyWereCreatedUnder() {
        harness.addToBattlefield(player1, new RunedTerror());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Overblaze()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        gs.advanceStep(gd);
        harness.setHand(player2, List.of(new Overblaze()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        assertThat(gd.permanentDamageDoublingsThisTurn.get(bears.getId())).isEqualTo(2);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.permanentDamageDoublingsThisTurn.get(bears.getId())).isEqualTo(1);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.permanentDamageDoublingsThisTurn).isEmpty();
    }

    @Test
    void additionalEnterCountersFromOtherPlayersPhaseSurviveActivePlayersCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        Permanent guildmage = harness.addToBattlefieldAndReturn(player2, new ZameckGuildmage());
        guildmage.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.additionalEnterCountersThisTurn.get(player2.getId())).isEqualTo(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.additionalEnterCountersThisTurn.get(player2.getId())).isEqualTo(1);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.additionalEnterCountersThisTurn).isEmpty();
    }

    @Test
    void enterTappedFromOtherPlayersPhaseSurvivesActivePlayersCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.setHand(player2, List.of(new DueRespect()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0);
        assertThat(gd.allPermanentsEnterTappedThisTurn).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.allPermanentsEnterTappedThisTurn).isTrue();

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.allPermanentsEnterTappedThisTurn).isFalse();
    }

    @Test
    void playerStaticEffectsFromOtherPlayersPhaseSurviveActivePlayersCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.setHand(player2, List.of(new AngelsGrace()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player2, 0);
        assertThat(gd.playerStaticEffectsUntilEndOfTurn.get(player2.getId())).hasSize(2);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.playerStaticEffectsUntilEndOfTurn.get(player2.getId())).hasSize(2);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.playerStaticEffectsUntilEndOfTurn).isEmpty();
    }

    @Test
    void spellLimitFromOtherPlayersPhaseSurvivesActivePlayersCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.setHand(player2, List.of(new IrencragFeat()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castSorcery(player2, 0);
        harness.passBothPriorities();
        assertThat(gd.playersMaxSpellsThisTurn).containsEntry(player2.getId(), 2);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.playersMaxSpellsThisTurn).containsEntry(player2.getId(), 2);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.playersMaxSpellsThisTurn).isEmpty();
    }

    @Test
    void looserSpellLimitHiddenByTighterOneOutlivesTheTighterOnesCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.limitSpellsThisTurn(player1.getId(), 1);
        gs.advanceStep(gd);
        gd.limitSpellsThisTurn(player1.getId(), 3);
        assertThat(gd.playersMaxSpellsThisTurn).containsEntry(player1.getId(), 1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.playersMaxSpellsThisTurn).containsEntry(player1.getId(), 3);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.playersMaxSpellsThisTurn).isEmpty();
    }

    @Test
    void lifeFloorRepeatedDuringOtherPlayersPhaseOutlivesActivePlayersCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        castAngelOfGrace();
        gs.advanceStep(gd);
        castAngelOfGrace();
        assertThat(gd.damageLifeFloorsUntilEndOfTurn).containsEntry(player1.getId(), 1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.damageLifeFloorsUntilEndOfTurn).containsEntry(player1.getId(), 1);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.damageLifeFloorsUntilEndOfTurn).isEmpty();
    }

    @Test
    void activePlayersOwnLifeFloorEndsAtTheirCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        castAngelOfGrace();
        assertThat(gd.damageLifeFloorsUntilEndOfTurn).containsEntry(player1.getId(), 1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gd.damageLifeFloorsUntilEndOfTurn).isEmpty();
    }

    @Test
    void comeuppanceFromOtherPlayersPhaseSurvivesActivePlayersCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.setHand(player2, List.of(new Comeuppance()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player2, 0);
        assertThat(gd.comeuppanceDamagePreventionShields).hasSize(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.comeuppanceDamagePreventionShields).hasSize(1);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.comeuppanceDamagePreventionShields).isEmpty();
    }

    @Test
    void inkshieldFromOtherPlayersPhaseSurvivesActivePlayersCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.setHand(player2, List.of(new Inkshield()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player2, 0);
        assertThat(gd.combatDamagePreventionTokenShields).containsOnlyKeys(player2.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.combatDamagePreventionTokenShields).containsOnlyKeys(player2.getId());

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.combatDamagePreventionTokenShields).isEmpty();
    }

    @Test
    void oneShotRedirectShieldFromOtherPlayersPhaseOutlivesActivePlayersCleanupUnlessUsed() {
        harness.addToBattlefield(player1, new RunedTerror());
        Permanent aegis = harness.addToBattlefieldAndReturn(player2, new AegisOfHonor());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        activateAegis(player2, aegis);
        activateAegis(player2, aegis);
        assertThat(gd.playerNextInstantOrSorceryDamageRedirectShields)
                .containsExactly(player2.getId(), player2.getId());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.playerNextInstantOrSorceryDamageRedirectShields).containsExactly(player2.getId());

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.playerNextInstantOrSorceryDamageRedirectShields).isEmpty();
    }

    @Test
    void activePlayersOwnOneShotRedirectShieldEndsAtTheirCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        Permanent aegis = harness.addToBattlefieldAndReturn(player1, new AegisOfHonor());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        activateAegis(player1, aegis);
        assertThat(gd.playerNextInstantOrSorceryDamageRedirectShields).containsExactly(player1.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gd.playerNextInstantOrSorceryDamageRedirectShields).isEmpty();
    }

    @Test
    void nextSpellFlashGrantFromOtherPlayersPhaseSurvivesActivePlayersCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        castQuicken(player2);
        assertThat(gd.nextSpellFlashGrantsThisTurn.get(player2.getId())).containsExactly(CardType.SORCERY);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.nextSpellFlashGrantsThisTurn.get(player2.getId())).containsExactly(CardType.SORCERY);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.nextSpellFlashGrantsThisTurn).isEmpty();
    }

    @Test
    void nextSpellFlashGrantUsedDuringActivePlayersPhaseIsNotRestoredAtTheirCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        castQuicken(player2);
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        harness.clearPriorityPassed();
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.castFromHand(player2, new Divination(), "{2}{U}");
        harness.passBothPriorities();
        assertThat(gd.nextSpellFlashGrantsThisTurn.getOrDefault(player2.getId(), List.of())).isEmpty();

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gd.nextSpellFlashGrantsThisTurn.getOrDefault(player2.getId(), List.of())).isEmpty();
    }

    @Test
    void nextSpellCountGrantsAreSplitBetweenTheActivePlayersTheyWereCreatedUnder() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.addNextSpellConvokeGrant(player1.getId());
        gs.advanceStep(gd);
        gd.addNextSpellConvokeGrant(player1.getId());
        gd.addNextCreatureSpellCascade(player2.getId());
        assertThat(gd.nextSpellConvokeGrantsThisTurn).containsEntry(player1.getId(), 2);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.nextSpellConvokeGrantsThisTurn).containsEntry(player1.getId(), 1);
        assertThat(gd.nextCreatureSpellCascadeThisTurn).containsEntry(player2.getId(), 1);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.nextSpellConvokeGrantsThisTurn).isEmpty();
        assertThat(gd.nextCreatureSpellCascadeThisTurn).isEmpty();
    }

    @Test
    void grantedFlashbackFromOtherPlayersPhaseSurvivesActivePlayersCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        Card shock = new Shock();
        harness.setGraveyard(player2, List.of(shock));
        harness.castFromHand(player2, new PastInFlames(), "{3}{R}");
        harness.passBothPriorities();
        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).contains(shock.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).contains(shock.getId());

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).isEmpty();
    }

    @Test
    void activePlayersOwnGrantedFlashbackEndsAtTheirCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.castFromHand(player1, new PastInFlames(), "{3}{R}");
        harness.passBothPriorities();
        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).contains(shock.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gd.cardsGrantedFlashbackUntilEndOfTurn).isEmpty();
    }

    @Test
    void nextInstantSorceryUncounterableGrantFromOtherPlayersPhaseSurvivesActivePlayersCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.castFromHand(player2, new Overmaster(), "{R}");
        harness.passBothPriorities();
        assertThat(gd.pendingNextInstantSorceryUncounterableThisTurnCount).containsEntry(player2.getId(), 1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.pendingNextInstantSorceryUncounterableThisTurnCount).containsEntry(player2.getId(), 1);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.pendingNextInstantSorceryUncounterableThisTurnCount).isEmpty();
    }

    @Test
    void activePlayersOwnNextInstantSorceryUncounterableGrantEndsAtTheirCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new Overmaster(), "{R}");
        harness.passBothPriorities();
        assertThat(gd.pendingNextInstantSorceryUncounterableThisTurnCount).containsEntry(player1.getId(), 1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gd.pendingNextInstantSorceryUncounterableThisTurnCount).isEmpty();
    }

    @Test
    void pendingCopyGrantsAreSplitBetweenTheActivePlayersTheyWereCreatedUnder() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.pendingNextSpellCopyThisTurnCount.merge(player1.getId(), 1, Integer::sum);
        gd.spellCopyGrantsUntilEndOfTurn.merge(player1.getId(), 1, Integer::sum);
        gs.advanceStep(gd);
        gd.pendingNextSpellCopyThisTurnCount.merge(player1.getId(), 1, Integer::sum);
        gd.spellCopyGrantsUntilEndOfTurn.merge(player2.getId(), 1, Integer::sum);
        assertThat(gd.pendingNextSpellCopyThisTurnCount).containsEntry(player1.getId(), 2);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.pendingNextSpellCopyThisTurnCount).containsEntry(player1.getId(), 1);
        assertThat(gd.spellCopyGrantsUntilEndOfTurn).doesNotContainKey(player1.getId())
                .containsEntry(player2.getId(), 1);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.pendingNextSpellCopyThisTurnCount).isEmpty();
        assertThat(gd.spellCopyGrantsUntilEndOfTurn).isEmpty();
    }

    @Test
    void exileCastGrantsFromOtherPlayersPhaseOutliveActivePlayersCleanupUnlessUsed() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        var kept = new GameData.ExileCastPermission(UUID.randomUUID(), UUID.randomUUID(), player2.getId(),
                UUID.randomUUID(), true);
        var used = new GameData.ExileCastPermission(UUID.randomUUID(), UUID.randomUUID(), player2.getId(),
                UUID.randomUUID(), true);
        var foretold = new GameData.ForetoldCardCastPermission(player2.getId(), true);
        gd.exileCastPermissionsUntilEndOfTurn.add(kept);
        gd.exileCastPermissionsUntilEndOfTurn.add(used);
        gd.foretoldCardCastPermissionsThisTurn.add(foretold);
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        gd.exileCastPermissionsUntilEndOfTurn.remove(used);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.exileCastPermissionsUntilEndOfTurn).containsExactly(kept);
        assertThat(gd.foretoldCardCastPermissionsThisTurn).containsExactly(foretold);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.exileCastPermissionsUntilEndOfTurn).isEmpty();
        assertThat(gd.foretoldCardCastPermissionsThisTurn).isEmpty();
    }

    @Test
    void chosenSourcePreventionFromOtherPlayersPhaseSurvivesActivePlayersCleanup() {
        Permanent terror = harness.addToBattlefieldAndReturn(player1, new RunedTerror());
        harness.addToBattlefield(player2, new AuriokReplica());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, terror.getId());
        gd.playerSourceDamagePreventionRequiredColors
                .computeIfAbsent(player2.getId(), id -> new java.util.concurrent.ConcurrentHashMap<>())
                .put(terror.getId(), Set.of(CardColor.BLACK));
        gd.playerSourceDamagePreventionLifeGainIds
                .computeIfAbsent(player2.getId(), id -> java.util.concurrent.ConcurrentHashMap.newKeySet())
                .add(terror.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.playerSourceDamagePreventionIds.get(player2.getId())).containsExactly(terror.getId());
        assertThat(gd.playerSourceDamagePreventionRequiredColors.get(player2.getId()))
                .containsExactly(Map.entry(terror.getId(), Set.of(CardColor.BLACK)));
        assertThat(gd.playerSourceDamagePreventionLifeGainIds.get(player2.getId())).containsExactly(terror.getId());

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.playerSourceDamagePreventionIds).isEmpty();
        assertThat(gd.playerSourceDamagePreventionRequiredColors).isEmpty();
        assertThat(gd.playerSourceDamagePreventionLifeGainIds).isEmpty();
    }

    @Test
    void activePlayersOwnChosenSourcePreventionEndsAtTheirCleanup() {
        Permanent terror = harness.addToBattlefieldAndReturn(player1, new RunedTerror());
        harness.addToBattlefield(player1, new AuriokReplica());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, terror.getId());
        assertThat(gd.playerSourceDamagePreventionIds.get(player1.getId())).containsExactly(terror.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gd.playerSourceDamagePreventionIds).isEmpty();
    }

    @Test
    void nextMatchingSourceShieldsFromOtherPlayersPhaseOutliveActivePlayersCleanupUnlessUsed() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.playerNextDamageFromMatchingSourcesPrevented
                .computeIfAbsent(player1.getId(), id -> new java.util.concurrent.CopyOnWriteArrayList<>())
                .add(new PermanentIsCreaturePredicate());
        gs.advanceStep(gd);
        var kept = new PermanentIsCreaturePredicate();
        gd.playerNextDamageFromMatchingSourcesPrevented
                .computeIfAbsent(player2.getId(), id -> new java.util.concurrent.CopyOnWriteArrayList<>())
                .add(kept);
        gd.playerNextDamageFromMatchingSourcesPrevented.get(player2.getId()).add(new PermanentIsCreaturePredicate());
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        gd.playerNextDamageFromMatchingSourcesPrevented.get(player2.getId()).remove(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.playerNextDamageFromMatchingSourcesPrevented).doesNotContainKey(player1.getId());
        assertThat(gd.playerNextDamageFromMatchingSourcesPrevented.get(player2.getId())).containsExactly(kept);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.playerNextDamageFromMatchingSourcesPrevented).isEmpty();
    }

    @Test
    void pendingEnchantmentCreatureEntryCountersAreSplitBetweenTheActivePlayersTheyWereCreatedUnder() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.pendingAdditionalCountersForNextEnchantmentCreatureEntryThisTurn.merge(player1.getId(), 2, Integer::sum);
        gs.advanceStep(gd);
        gd.pendingAdditionalCountersForNextEnchantmentCreatureEntryThisTurn.merge(player1.getId(), 2, Integer::sum);
        gd.pendingAdditionalCountersForNextEnchantmentCreatureEntryThisTurn.merge(player2.getId(), 2, Integer::sum);
        assertThat(gd.pendingAdditionalCountersForNextEnchantmentCreatureEntryThisTurn)
                .containsEntry(player1.getId(), 4);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.pendingAdditionalCountersForNextEnchantmentCreatureEntryThisTurn)
                .containsEntry(player1.getId(), 2)
                .containsEntry(player2.getId(), 2);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.pendingAdditionalCountersForNextEnchantmentCreatureEntryThisTurn).isEmpty();
    }

    @Test
    void repeatedReverberationFromOtherPlayersPhaseSurvivesActivePlayersCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        harness.setHand(player2, List.of(new RepeatedReverberation()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0);
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount).containsEntry(player2.getId(), 2);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount).containsEntry(player2.getId(), 2);
        assertThat(gd.pendingNextLoyaltyAbilityCopyThisTurnCount).containsEntry(player2.getId(), 2);
        assertThat(gd.pendingNextLoyaltyAbilityCopySourcesThisTurn.get(player2.getId())).hasSize(2)
                .allMatch(source -> source.card() instanceof RepeatedReverberation);
        assertThat(gd.pendingNextSpellOrLoyaltyCopyLinkedThisTurn).containsExactly(player2.getId());

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount).isEmpty();
        assertThat(gd.pendingNextLoyaltyAbilityCopyThisTurnCount).isEmpty();
        assertThat(gd.pendingNextLoyaltyAbilityCopySourcesThisTurn).isEmpty();
        assertThat(gd.pendingNextSpellOrLoyaltyCopyLinkedThisTurn).isEmpty();
    }

    @Test
    void activePlayersOwnRepeatedReverberationEndsAtTheirCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new RepeatedReverberation()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);
        assertThat(gd.pendingNextLoyaltyAbilityCopyThisTurnCount).containsEntry(player1.getId(), 2);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount).isEmpty();
        assertThat(gd.pendingNextLoyaltyAbilityCopyThisTurnCount).isEmpty();
        assertThat(gd.pendingNextLoyaltyAbilityCopySourcesThisTurn).isEmpty();
        assertThat(gd.pendingNextSpellOrLoyaltyCopyLinkedThisTurn).isEmpty();
    }

    @Test
    void pendingInstantSorceryCopyTriggersAreSplitBetweenTheActivePlayersTheyWereCreatedUnder() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card firstSource = new GrizzlyBears();
        Card secondSource = new Shock();
        Fixed firstDynamic = new Fixed(2);
        Fixed secondDynamic = new Fixed(3);
        gd.pendingNextInstantSorceryCopyThisTurnCount.merge(player1.getId(), 1, Integer::sum);
        gd.pendingNextInstantSorceryCopyThisTurnSingleTriggerCount.merge(player1.getId(), 1, Integer::sum);
        gd.pendingNextInstantSorceryCopyThisTurnCountSource.put(player1.getId(), firstSource);
        gd.pendingNextInstantSorceryCopyThisTurnMaxManaValues
                .computeIfAbsent(player1.getId(), id -> new java.util.ArrayList<>()).add(3);
        gd.pendingNextInstantSorceryCopyThisTurnDynamicCounts
                .computeIfAbsent(player1.getId(), id -> new java.util.ArrayList<>()).add(firstDynamic);
        gd.pendingNextInstantSorceryCopyThisTurnDynamicSources
                .computeIfAbsent(player1.getId(), id -> new java.util.ArrayList<>()).add(firstSource);
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        gd.pendingNextInstantSorceryCopyThisTurnCount.merge(player1.getId(), 1, Integer::sum);
        gd.pendingNextInstantSorceryCopyThisTurnSingleTriggerCount.merge(player1.getId(), 1, Integer::sum);
        gd.pendingNextInstantSorceryCopyThisTurnCountSource.put(player1.getId(), secondSource);
        gd.pendingNextInstantSorceryCopyThisTurnMaxManaValues.get(player1.getId()).add(2);
        gd.pendingNextInstantSorceryCopyThisTurnDynamicCounts.get(player1.getId()).add(secondDynamic);
        gd.pendingNextInstantSorceryCopyThisTurnDynamicSources.get(player1.getId()).add(secondSource);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount).containsExactly(Map.entry(player1.getId(), 1));
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnSingleTriggerCount)
                .containsExactly(Map.entry(player1.getId(), 1));
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCountSource)
                .containsExactly(Map.entry(player1.getId(), secondSource));
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnMaxManaValues.get(player1.getId())).containsExactly(2);
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnDynamicCounts.get(player1.getId()))
                .containsExactly(secondDynamic);
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnDynamicSources.get(player1.getId()))
                .containsExactly(secondSource);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount).isEmpty();
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnSingleTriggerCount).isEmpty();
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCountSource).isEmpty();
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnMaxManaValues).isEmpty();
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnDynamicCounts).isEmpty();
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnDynamicSources).isEmpty();
    }

    @Test
    void pendingExhaustCopyFromOtherPlayersPhaseOutlivesActivePlayersCleanupUnlessUsed() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        var ownSource = new GameData.PendingCopySource(new GrizzlyBears(), null);
        gd.pendingNextExhaustAbilityCopyThisTurnCount.merge(player1.getId(), 1, Integer::sum);
        gd.pendingNextExhaustAbilityCopySourcesThisTurn
                .computeIfAbsent(player1.getId(), id -> new java.util.ArrayList<>()).add(ownSource);
        gs.advanceStep(gd);
        var otherSource = new GameData.PendingCopySource(new Shock(), null);
        gd.pendingNextExhaustAbilityCopyThisTurnCount.merge(player1.getId(), 1, Integer::sum);
        gd.pendingNextExhaustAbilityCopySourcesThisTurn.get(player1.getId()).add(otherSource);
        gd.pendingNextExhaustAbilityCopyThisTurnCount.merge(player2.getId(), 1, Integer::sum);
        gd.pendingNextExhaustAbilityCopySourcesThisTurn
                .computeIfAbsent(player2.getId(), id -> new java.util.ArrayList<>()).add(otherSource);
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        gd.pendingNextExhaustAbilityCopyThisTurnCount.remove(player2.getId());
        gd.pendingNextExhaustAbilityCopySourcesThisTurn.remove(player2.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.pendingNextExhaustAbilityCopyThisTurnCount).containsExactly(Map.entry(player1.getId(), 1));
        assertThat(gd.pendingNextExhaustAbilityCopySourcesThisTurn)
                .containsExactly(Map.entry(player1.getId(), List.of(otherSource)));

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.pendingNextExhaustAbilityCopyThisTurnCount).isEmpty();
        assertThat(gd.pendingNextExhaustAbilityCopySourcesThisTurn).isEmpty();
    }

    @Test
    void senTripletsHandPermissionFromOtherPlayersPhaseSurvivesActivePlayersCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.addToBattlefield(player2, new SenTriplets());
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.currentStep).isEqualTo(TurnStep.UNTAP);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.UPKEEP);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.canPlayFromHandOf(player2.getId(), player1.getId())).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.canPlayFromHandOf(player2.getId(), player1.getId())).isTrue();
        assertThat(gd.playersSilencedThisTurn).containsExactly(player1.getId());

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.senControllerPlayerId).isNull();
        assertThat(gd.senControlledPlayerId).isNull();
        assertThat(gd.playersSilencedThisTurn).isEmpty();
    }

    @Test
    void impulseAndGraveyardPlayPermissionsEndAtTheCleanupOfThePlayerActiveWhenTheyWereGranted() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.addToBattlefield(player2, new ArkOfHunger());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        List<Card> impulseCards = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player1, impulseCards);
        harness.castFromHand(player1, new ActOnImpulse(), "{2}{R}");
        harness.passBothPriorities();
        impulseCards.forEach(card -> assertThat(gd.exilePlayPermissions).containsEntry(card.getId(), player1.getId()));
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        Card milled = new Shock();
        harness.setLibrary(player2, List.of(milled));
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.graveyardPlayPermissions).containsEntry(milled.getId(), player2.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        impulseCards.forEach(card -> assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId()));
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).isEmpty();
        assertThat(gd.graveyardPlayPermissions).containsEntry(milled.getId(), player2.getId());
        assertThat(gd.graveyardPlayPermissionsExpireEndOfTurn).containsExactly(milled.getId());

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.graveyardPlayPermissions).doesNotContainKey(milled.getId());
        assertThat(gd.graveyardPlayPermissionsExpireEndOfTurn).isEmpty();
    }

    @Test
    void impulsePermissionFromOtherPlayersPhaseSurvivesActivePlayersCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        List<Card> impulseCards = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player2, impulseCards);
        harness.castFromHand(player2, new ActOnImpulse(), "{2}{R}");
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        impulseCards.forEach(card -> assertThat(gd.exilePlayPermissions).containsEntry(card.getId(), player2.getId()));

        harness.passUntil(player2, TurnStep.CLEANUP);
        impulseCards.forEach(card -> assertThat(gd.exilePlayPermissions).doesNotContainKey(card.getId()));
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).isEmpty();
    }

    @Test
    void playCardsExiledWithSourceGrantFromOtherPlayersPhaseSurvivesActivePlayersCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        Permanent musicBox = harness.addToBattlefieldAndReturn(player2, new KaylasMusicBox());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        Card exiled = new GrizzlyBears();
        gd.addToExile(player2.getId(), exiled, musicBox.getId());
        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.exilePlayUntilEndOfTurnGrantsBySource).containsKey(musicBox.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.exilePlayUntilEndOfTurnGrantsBySource).containsKey(musicBox.getId());
        assertThat(gd.exilePlayPermissions).containsEntry(exiled.getId(), player2.getId());

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.exilePlayUntilEndOfTurnGrantsBySource).isEmpty();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(exiled.getId());
    }

    @Test
    void partlyUsedAmountShieldFromOtherPlayersPhaseKeepsItsRestAfterActivePlayersCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.addToBattlefield(player2, new VengefulArchon());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.activateAbility(player2, 0, 3, player1.getId());
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 20);
        harness.assertLife(player1, 18);
        assertThat(gd.damageRedirectShields).singleElement()
                .extracting(DamageRedirectShield::remainingAmount).isEqualTo(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.damageRedirectShields).singleElement()
                .extracting(DamageRedirectShield::remainingAmount).isEqualTo(1);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.damageRedirectShields).isEmpty();
    }

    @Test
    void activePlayersOwnPartlyUsedAmountShieldEndsAtTheirCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.addToBattlefield(player1, new VengefulArchon());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 1, 3, player2.getId());
        harness.passBothPriorities();
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        assertThat(gd.damageRedirectShields).singleElement()
                .extracting(DamageRedirectShield::remainingAmount).isEqualTo(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gd.damageRedirectShields).isEmpty();
    }

    @Test
    void nextDamageFromTargetCreatureShieldFromOtherPlayersPhaseSurvivesActivePlayersCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        harness.setHand(player2, List.of(new AweStrike()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        assertThat(gd.sourceNextDamageToAnyTargetShields).hasSize(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.sourceNextDamageToAnyTargetShields).hasSize(1);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.sourceNextDamageToAnyTargetShields).isEmpty();
    }

    @Test
    void graveyardCastExileRiderFromOtherPlayersPhaseSurvivesActivePlayersCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        Permanent vohar = harness.addToBattlefieldAndReturn(player2, new VoharVodalianDesecrator());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        Card fog = new Fog();
        harness.setGraveyard(player2, List.of(fog));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(vohar), 1, null,
                fog.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        assertThat(gd.exileInsteadOfGraveyard).containsExactly(fog.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.graveyardPlayPermissions).containsEntry(fog.getId(), player2.getId());
        assertThat(gd.exileInsteadOfGraveyard).containsExactly(fog.getId());

        harness.passUntil(player2, TurnStep.END_STEP);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castFromGraveyard(player2, fog.getId());
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(fog);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(fog);
        assertThat(gd.exileInsteadOfGraveyard).isEmpty();
    }

    @Test
    void activePlayersOwnGraveyardCastExileRiderEndsAtTheirCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        Permanent vohar = harness.addToBattlefieldAndReturn(player1, new VoharVodalianDesecrator());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card fog = new Fog();
        harness.setGraveyard(player1, List.of(fog));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vohar), 1, null,
                fog.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        assertThat(gd.exileInsteadOfGraveyard).containsExactly(fog.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.graveyardPlayPermissions).doesNotContainKey(fog.getId());
        assertThat(gd.exileInsteadOfGraveyard).isEmpty();
    }

    @Test
    void outsideGamePlayPermissionsEndAtTheCleanupOfThePlayerActiveWhenTheyWereGranted() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card firstSideboardCard = new GrizzlyBears();
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(firstSideboardCard)));
        harness.castFromHand(player1, new Wish(), "{2}{R}");
        harness.passBothPriorities();
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        gd.playerSideboards.put(player2.getId(), new ArrayList<>(List.of(bears, forest)));
        harness.castFromHand(player2, new Wish(), "{2}{R}");
        harness.passBothPriorities();
        assertThat(gd.outsideGamePlayPermissions)
                .containsExactlyInAnyOrder(firstSideboardCard.getId(), bears.getId(), forest.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.outsideGamePlayPermissions).containsExactlyInAnyOrder(bears.getId(), forest.getId());
        assertThat(gd.outsideGamePlayPermissions.grants()).containsExactly(Set.of(bears.getId(), forest.getId()));

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.outsideGamePlayPermissions).isEmpty();
    }

    private void castQuicken(Player caster) {
        harness.setLibrary(caster, List.of(new GrizzlyBears()));
        harness.castFromHand(caster, new Quicken(), "{U}");
        harness.passBothPriorities();
    }

    private void activateAegis(Player controller, Permanent aegis) {
        harness.addMana(controller, ManaColor.COLORLESS, 1);
        harness.activateAbility(controller, gd.playerBattlefields.get(controller.getId()).indexOf(aegis), null, null);
        harness.passBothPriorities();
    }

    private void castAngelOfGrace() {
        harness.setHand(player1, List.of(new AngelOfGrace()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
