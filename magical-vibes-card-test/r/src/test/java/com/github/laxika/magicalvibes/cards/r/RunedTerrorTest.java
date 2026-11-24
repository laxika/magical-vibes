package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.effect.UnattachEquipmentIfAttachedToControlledCreatureEffect;
import com.github.laxika.magicalvibes.model.RegenerationShield;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

import com.github.laxika.magicalvibes.model.effect.OutsideGamePlayPermissionEffect;
import com.github.laxika.magicalvibes.model.effect.SkipChosenStepOrPhaseEffect;
import com.github.laxika.magicalvibes.testutil.FloatingCombatRules;
import com.github.laxika.magicalvibes.model.effect.DoubleControllerDamageToOpponentsAndTheirPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectNextInstantOrSorceryDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.CombatDamagePreventionTokenShield;
import com.github.laxika.magicalvibes.model.PlayerSourceDamagePrevention;
import com.github.laxika.magicalvibes.model.SourceNextDamageToAnyTargetShield;
import com.github.laxika.magicalvibes.model.ComeuppanceDamagePreventionShield;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToSpellsThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LimitControllerToOneMoreSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PreventionScope;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.testutil.Prevention;
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
import com.github.laxika.magicalvibes.model.PendingMysticReflection;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesEnterWithAdditionalCountersThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.NextControlledEnchantmentCreatureEntryWithAdditionalCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentsEnterTappedThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectDrawsEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterMysticReflectionEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterNextDrawCreateBearReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterNextDrawDamageReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterNextDrawExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterNextDrawGainLifeReplacementEffect;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ControllerCantSacrificeTargetThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleCombatDamageToCreaturesThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageFromTargetPermanentThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantGainLifeThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SkipStepOrPhaseKind;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.effect.CantLoseGameEffect;
import com.github.laxika.magicalvibes.model.effect.DamageLifeFloorEffect;

import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.effect.GrantedCardAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashToNextMatchingSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GrantConvokeToNextSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MakeNextInstantSorceryUncounterableThisTurnEffect;

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
        assertThat(gd.hasFloatingEffectOnPermanent(terror.getId(), CantBlockEffect.class)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.hasFloatingEffectOnPermanent(terror.getId(), CantBlockEffect.class)).isTrue();

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.hasFloatingEffectOnPermanent(terror.getId(), CantBlockEffect.class)).isFalse();
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

        assertThat(gd.hasFloatingEffectOnPermanent(terror.getId(), CantBlockEffect.class)).isFalse();
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
        assertThat(gd.permanentDamagePreventionShield(terror.getId())).isEqualTo(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.permanentDamagePreventionShield(terror.getId())).isEqualTo(1);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.permanentDamagePreventionShield(terror.getId())).isZero();
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
        gd.usePermanentDamagePreventionShields(terror.getId(), false, 1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gd.permanentDamagePreventionShield(terror.getId())).isZero();
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
        assertThat(gd.playerDamagePreventionShields().get(player2.getId())).isEqualTo(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.playerDamagePreventionShields().get(player2.getId())).isEqualTo(1);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.playerDamagePreventionShields().getOrDefault(player2.getId(), 0)).isZero();
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
        assertThat(gd.hasPrevention(PreventionScope.ALL_COMBAT)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.hasPrevention(PreventionScope.ALL_COMBAT)).isTrue();

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.hasPrevention(PreventionScope.ALL_COMBAT)).isFalse();
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

        assertThat(gd.hasPrevention(PreventionScope.ALL_COMBAT)).isFalse();
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
        assertThat(Prevention.playersWithAllDamagePrevented(gd)).containsExactly(player2.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(Prevention.playersWithAllDamagePrevented(gd)).containsExactly(player2.getId());

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(Prevention.playersWithAllDamagePrevented(gd)).isEmpty();
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
        assertThat(gd.preventionEffectsOnPlayer(player2.getId(),
                PreventionScope.ALL_TO_CONTROLLER_AND_CREATURES_FROM_MATCHING_SOURCES)).hasSize(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.preventionEffectsOnPlayer(player2.getId(),
                PreventionScope.ALL_TO_CONTROLLER_AND_CREATURES_FROM_MATCHING_SOURCES)).hasSize(1);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.preventionEffects(PreventionScope.ALL_TO_CONTROLLER_AND_CREATURES_FROM_MATCHING_SOURCES))
                .isEmpty();
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
        assertThat(gd.hasFloatingEffectOnPlayer(null, PlayersCantGainLifeThisTurnEffect.class)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.hasFloatingEffectOnPlayer(null, PlayersCantGainLifeThisTurnEffect.class)).isTrue();

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.hasFloatingEffectOnPlayer(null, PlayersCantGainLifeThisTurnEffect.class)).isFalse();
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
        assertThat(silencedPlayers()).containsExactly(player1.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(silencedPlayers()).containsExactly(player1.getId());

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(silencedPlayers()).isEmpty();
    }

    @Test
    void activePlayersOwnSilenceEndsAtTheirCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Silence()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0);
        assertThat(silencedPlayers()).containsExactly(player2.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(silencedPlayers()).isEmpty();
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
        assertThat(gqs.playerHasShroud(gd, player2.getId())).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gqs.playerHasShroud(gd, player2.getId())).isTrue();

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gqs.playerHasShroud(gd, player2.getId())).isFalse();
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
        assertThat(gd.hasFloatingEffectOnPlayer(player2.getId(), GrantFlashToSpellsThisTurnEffect.class)).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.hasFloatingEffectOnPlayer(player2.getId(), GrantFlashToSpellsThisTurnEffect.class)).isTrue();

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.hasFloatingEffectOnPlayer(player2.getId(), GrantFlashToSpellsThisTurnEffect.class)).isFalse();
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
        assertThat(gd.regenerationShieldCount(terror.getId())).isEqualTo(1);
        assertThat(gd.regenerationShields(terror.getId()))
                .extracting(shield -> ((RegenerationShield) shield.effect()).rider(), FloatingContinuousEffect::controllerId)
                .containsExactly(tuple(RegenerationShield.Rider.GAIN_CONTROL, player2.getId()));

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

        assertThat(gd.regenerationShieldCount(terror.getId())).isZero();
        assertThat(gd.regenerationShields(terror.getId())).isEmpty();
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
        assertThat(gd.regenerationShields(scavengers.getId()))
                .extracting(shield -> ((RegenerationShield) shield.effect()).rider())
                .containsExactly(RegenerationShield.Rider.PLUS_ONE_COUNTER);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.regenerationShieldCount(scavengers.getId())).isEqualTo(1);
        assertThat(gd.regenerationShields(scavengers.getId()))
                .extracting(shield -> ((RegenerationShield) shield.effect()).rider(), FloatingContinuousEffect::controllerId)
                .containsExactly(tuple(RegenerationShield.Rider.PLUS_ONE_COUNTER, player2.getId()));

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.regenerationShieldCount(scavengers.getId())).isZero();
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
        assertThat(gd.regenerationShields(sentry.getId()))
                .extracting(shield -> ((RegenerationShield) shield.effect()).rider())
                .containsExactly(RegenerationShield.Rider.OPPONENT_DRAW);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.regenerationShieldCount(sentry.getId())).isEqualTo(1);
        assertThat(gd.regenerationShields(sentry.getId()))
                .extracting(shield -> ((RegenerationShield) shield.effect()).playerId(), FloatingContinuousEffect::controllerId)
                .containsExactly(tuple(player1.getId(), player2.getId()));

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.regenerationShields(sentry.getId())).isEmpty();
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
        assertThat(gd.floatingEffectsOnPlayer(null, DoubleCombatDamageToCreaturesThisTurnEffect.class)).hasSize(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.floatingEffectsOnPlayer(null, DoubleCombatDamageToCreaturesThisTurnEffect.class)).hasSize(1);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.floatingEffectsOnPlayer(null, DoubleCombatDamageToCreaturesThisTurnEffect.class)).isEmpty();
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
        assertThat(gd.floatingEffectsOnPermanent(bears.getId(),
                DoubleDamageFromTargetPermanentThisTurnEffect.class)).hasSize(2);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.floatingEffectsOnPermanent(bears.getId(),
                DoubleDamageFromTargetPermanentThisTurnEffect.class)).hasSize(1);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.floatingEffectsOnPermanent(bears.getId(),
                DoubleDamageFromTargetPermanentThisTurnEffect.class)).isEmpty();
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
        assertThat(gd.floatingEffectsOnPlayer(player2.getId(),
                ControlledCreaturesEnterWithAdditionalCountersThisTurnEffect.class)).hasSize(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.floatingEffectsOnPlayer(player2.getId(),
                ControlledCreaturesEnterWithAdditionalCountersThisTurnEffect.class)).hasSize(1);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.floatingEffectsOfTypes(
                ControlledCreaturesEnterWithAdditionalCountersThisTurnEffect.class)).isEmpty();
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
        assertThat(gd.floatingEffectsOfTypes(PermanentsEnterTappedThisTurnEffect.class)).hasSize(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.floatingEffectsOfTypes(PermanentsEnterTappedThisTurnEffect.class)).hasSize(1);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.floatingEffectsOfTypes(PermanentsEnterTappedThisTurnEffect.class)).isEmpty();
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
        assertThat(gd.floatingPlayerEffects(player2.getId(), CantLoseGameEffect.class,
                DamageLifeFloorEffect.class)).hasSize(2);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.floatingPlayerEffects(player2.getId(), CantLoseGameEffect.class,
                DamageLifeFloorEffect.class)).hasSize(2);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.floatingPlayerEffects(player2.getId(), CantLoseGameEffect.class,
                DamageLifeFloorEffect.class)).isEmpty();
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
        assertThat(gd.spellLimitThisTurn(player2.getId())).isEqualTo(2);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.spellLimitThisTurn(player2.getId())).isEqualTo(2);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.spellLimitThisTurn(player1.getId())).isEqualTo(Integer.MAX_VALUE);
        assertThat(gd.spellLimitThisTurn(player2.getId())).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void looserSpellLimitHiddenByTighterOneOutlivesTheTighterOnesCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.addFloatingEffect(FloatingContinuousEffect.onPlayer("Test", null, player1.getId(),
                new LimitControllerToOneMoreSpellThisTurnEffect(1), player1.getId(), Duration.UNTIL_END_OF_TURN));
        gs.advanceStep(gd);
        gd.addFloatingEffect(FloatingContinuousEffect.onPlayer("Test", null, player1.getId(),
                new LimitControllerToOneMoreSpellThisTurnEffect(3), player1.getId(), Duration.UNTIL_END_OF_TURN));
        assertThat(gd.spellLimitThisTurn(player1.getId())).isEqualTo(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.spellLimitThisTurn(player1.getId())).isEqualTo(3);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.spellLimitThisTurn(player1.getId())).isEqualTo(Integer.MAX_VALUE);
        assertThat(gd.spellLimitThisTurn(player2.getId())).isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void lifeFloorRepeatedDuringOtherPlayersPhaseOutlivesActivePlayersCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        castAngelOfGrace();
        gs.advanceStep(gd);
        castAngelOfGrace();
        assertThat(Prevention.damageLifeFloor(gd, player1.getId())).isEqualTo(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(Prevention.damageLifeFloor(gd, player1.getId())).isEqualTo(1);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(Prevention.damageLifeFloor(gd, player1.getId())).isZero();
    }

    @Test
    void activePlayersOwnLifeFloorEndsAtTheirCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        castAngelOfGrace();
        assertThat(Prevention.damageLifeFloor(gd, player1.getId())).isEqualTo(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(Prevention.damageLifeFloor(gd, player1.getId())).isZero();
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
        assertThat(gd.shields(ComeuppanceDamagePreventionShield.class)).hasSize(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.shields(ComeuppanceDamagePreventionShield.class)).hasSize(1);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.shields(ComeuppanceDamagePreventionShield.class)).isEmpty();
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
        assertThat(gd.playersWithFloatingEffect(CombatDamagePreventionTokenShield.class)).containsOnly(player2.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.playersWithFloatingEffect(CombatDamagePreventionTokenShield.class)).containsOnly(player2.getId());

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.hasShields(CombatDamagePreventionTokenShield.class)).isFalse();
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
        assertThat(Prevention.playersOf(gd, RedirectNextInstantOrSorceryDamageToControllerEffect.class))
                .containsExactly(player2.getId(), player2.getId());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(Prevention.playersOf(gd, RedirectNextInstantOrSorceryDamageToControllerEffect.class)).containsExactly(player2.getId());

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.hasShields(RedirectNextInstantOrSorceryDamageToControllerEffect.class)).isFalse();
    }

    @Test
    void activePlayersOwnOneShotRedirectShieldEndsAtTheirCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        Permanent aegis = harness.addToBattlefieldAndReturn(player1, new AegisOfHonor());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        activateAegis(player1, aegis);
        assertThat(Prevention.playersOf(gd, RedirectNextInstantOrSorceryDamageToControllerEffect.class)).containsExactly(player1.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gd.hasShields(RedirectNextInstantOrSorceryDamageToControllerEffect.class)).isFalse();
    }

    @Test
    void nextSpellFlashGrantFromOtherPlayersPhaseSurvivesActivePlayersCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gs.advanceStep(gd);
        castQuicken(player2);
        assertThat(gd.floatingEffectsOnPlayer(player2.getId(), GrantFlashToNextMatchingSpellEffect.class)).hasSize(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.floatingEffectsOnPlayer(player2.getId(), GrantFlashToNextMatchingSpellEffect.class)).hasSize(1);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.floatingEffectsOfTypes(GrantFlashToNextMatchingSpellEffect.class)).isEmpty();
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
        assertThat(gd.floatingEffectsOnPlayer(player2.getId(), GrantFlashToNextMatchingSpellEffect.class)).isEmpty();

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gd.floatingEffectsOnPlayer(player2.getId(), GrantFlashToNextMatchingSpellEffect.class)).isEmpty();
    }

    @Test
    void nextSpellCountGrantsAreSplitBetweenTheActivePlayersTheyWereCreatedUnder() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.addFloatingEffect(FloatingContinuousEffect.onPlayer("Test", null, player1.getId(),
                new GrantConvokeToNextSpellThisTurnEffect(), player1.getId(), Duration.UNTIL_MATCHING_SPELL_CAST));
        gs.advanceStep(gd);
        gd.addFloatingEffect(FloatingContinuousEffect.onPlayer("Test", null, player1.getId(),
                new GrantConvokeToNextSpellThisTurnEffect(), player1.getId(), Duration.UNTIL_MATCHING_SPELL_CAST));
        assertThat(gd.floatingEffectsOnPlayer(player1.getId(), GrantConvokeToNextSpellThisTurnEffect.class)).hasSize(2);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.floatingEffectsOnPlayer(player1.getId(), GrantConvokeToNextSpellThisTurnEffect.class)).hasSize(1);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.floatingEffectsOfTypes(GrantConvokeToNextSpellThisTurnEffect.class)).isEmpty();
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
        assertThat(grantedCards(GrantedCardAbilityEffect.Ability.FLASHBACK)).contains(shock.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(grantedCards(GrantedCardAbilityEffect.Ability.FLASHBACK)).contains(shock.getId());

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(grantedCards(GrantedCardAbilityEffect.Ability.FLASHBACK)).isEmpty();
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
        assertThat(grantedCards(GrantedCardAbilityEffect.Ability.FLASHBACK)).contains(shock.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(grantedCards(GrantedCardAbilityEffect.Ability.FLASHBACK)).isEmpty();
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
        assertThat(gd.floatingEffectsOnPlayer(player2.getId(), MakeNextInstantSorceryUncounterableThisTurnEffect.class)).hasSize(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.floatingEffectsOnPlayer(player2.getId(), MakeNextInstantSorceryUncounterableThisTurnEffect.class)).hasSize(1);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.floatingEffectsOfTypes(MakeNextInstantSorceryUncounterableThisTurnEffect.class)).isEmpty();
    }

    @Test
    void activePlayersOwnNextInstantSorceryUncounterableGrantEndsAtTheirCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new Overmaster(), "{R}");
        harness.passBothPriorities();
        assertThat(gd.floatingEffectsOnPlayer(player1.getId(), MakeNextInstantSorceryUncounterableThisTurnEffect.class)).hasSize(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gd.floatingEffectsOfTypes(MakeNextInstantSorceryUncounterableThisTurnEffect.class)).isEmpty();
    }

    @Test
    void pendingCopyAbilitiesAreSplitBetweenTheActivePlayersTheyWereCreatedUnderUnlessUsed() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        TriggeredAbility copyNext = RegisterDelayedControllerSpellCastTriggerEffect.nextThisTurn(null,
                List.of(new CopyTriggeringSpellEffect().permanentSpellCopiesAreTokens())).delayedAbility();
        TriggeredAbility copyEach = RegisterDelayedControllerSpellCastTriggerEffect.nextThisTurn(
                CardPredicateUtils.instantOrSorcery(), List.of(new CopyTriggeringSpellEffect())).delayedAbility();
        var firstNext = TemporaryGlobalTriggeredAbility.nextThisTurn(player1.getId(), new GrizzlyBears(), copyNext,
                null);
        var firstEach = new TemporaryGlobalTriggeredAbility(player1.getId(), new GrizzlyBears(), copyEach);
        gd.temporaryGlobalTriggeredAbilities.addAll(List.of(firstNext, firstEach));
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        var secondNext = TemporaryGlobalTriggeredAbility.nextThisTurn(player1.getId(), new Shock(), copyNext, null);
        var secondEach = new TemporaryGlobalTriggeredAbility(player2.getId(), new Shock(), copyEach);
        var usedNext = TemporaryGlobalTriggeredAbility.nextThisTurn(player2.getId(), new Shock(), copyNext, null);
        gd.temporaryGlobalTriggeredAbilities.addAll(List.of(secondNext, secondEach, usedNext));
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        gd.temporaryGlobalTriggeredAbilities.remove(usedNext);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        // Each ability made under player 2's phase outlives player 1's cleanup step; the others end there, and a
        // used one isn't restored.
        assertThat(gd.temporaryGlobalTriggeredAbilities).containsExactlyInAnyOrder(secondNext, secondEach);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.temporaryGlobalTriggeredAbilities).isEmpty();
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
        gd.grantExileCastPermission(kept);
        gd.grantExileCastPermission(used);
        gd.grantForetoldCardCastPermission(foretold);
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        gd.endExileCastGrant(used.grantId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.exileCastPermissions()).containsExactly(kept);
        assertThat(gd.foretoldCardCastPermissions()).containsExactly(foretold);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.exileCastPermissions()).isEmpty();
        assertThat(gd.foretoldCardCastPermissions()).isEmpty();
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
        var richer = new PlayerSourceDamagePrevention(Set.of(terror.getId()), Set.of(CardColor.BLACK), true);
        gd.shieldEffects(PlayerSourceDamagePrevention.class).forEach(fe -> gd.reduceShield(fe, richer));

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(Prevention.sourceIds(gd, player2.getId())).containsExactly(terror.getId());
        assertThat(gd.shields(PlayerSourceDamagePrevention.class)).containsExactly(richer);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.hasShields(PlayerSourceDamagePrevention.class)).isFalse();
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
        assertThat(Prevention.sourceIds(gd, player1.getId())).containsExactly(terror.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gd.hasShields(PlayerSourceDamagePrevention.class)).isFalse();
    }

    @Test
    void nextMatchingSourceShieldsFromOtherPlayersPhaseOutliveActivePlayersCleanupUnlessUsed() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addNextFromMatchingSource(player1, new PermanentIsCreaturePredicate());
        gs.advanceStep(gd);
        var kept = new PermanentIsCreaturePredicate();
        addNextFromMatchingSource(player2, kept);
        var used = addNextFromMatchingSource(player2, new PermanentIsCreaturePredicate());
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        gd.endFloatingEffect(used);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.preventionEffectsOnPlayer(player1.getId(), PreventionScope.NEXT_TO_CONTROLLER_FROM_MATCHING_SOURCES))
                .isEmpty();
        assertThat(gd.preventionEffectsOnPlayer(player2.getId(), PreventionScope.NEXT_TO_CONTROLLER_FROM_MATCHING_SOURCES))
                .extracting(fe -> ((PreventDamageEffect) fe.effect()).sourcePredicate()).containsExactly(kept);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.hasPrevention(PreventionScope.NEXT_TO_CONTROLLER_FROM_MATCHING_SOURCES)).isFalse();
    }

    @Test
    void pendingEnchantmentCreatureEntryCountersAreSplitBetweenTheActivePlayersTheyWereCreatedUnder() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addUntilEndOfTurn(player1, new NextControlledEnchantmentCreatureEntryWithAdditionalCountersEffect(2));
        gs.advanceStep(gd);
        addUntilEndOfTurn(player1, new NextControlledEnchantmentCreatureEntryWithAdditionalCountersEffect(2));
        addUntilEndOfTurn(player2, new NextControlledEnchantmentCreatureEntryWithAdditionalCountersEffect(2));
        assertThat(gd.floatingEffectsOnPlayer(player1.getId(),
                NextControlledEnchantmentCreatureEntryWithAdditionalCountersEffect.class)).hasSize(2);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.floatingEffectsOnPlayer(player1.getId(),
                NextControlledEnchantmentCreatureEntryWithAdditionalCountersEffect.class)).hasSize(1);
        assertThat(gd.floatingEffectsOnPlayer(player2.getId(),
                NextControlledEnchantmentCreatureEntryWithAdditionalCountersEffect.class)).hasSize(1);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.floatingEffectsOfTypes(
                NextControlledEnchantmentCreatureEntryWithAdditionalCountersEffect.class)).isEmpty();
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
        assertThat(pendingNextAbilityCount(player2)).isEqualTo(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.temporaryGlobalTriggeredAbilities).singleElement()
                .satisfies(delayed -> assertThat(delayed.sourceCard()).isInstanceOf(RepeatedReverberation.class));
        assertThat(pendingNextAbilityCount(player2)).isEqualTo(1);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(pendingNextAbilityCount(player2)).isZero();
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
        assertThat(pendingNextAbilityCount(player1)).isEqualTo(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(pendingNextAbilityCount(player1)).isZero();
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
        assertThat(silencedPlayers()).containsExactly(player1.getId());

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.handPlayableBy(player1.getId())).isNull();
        assertThat(silencedPlayers()).isEmpty();
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
        impulseCards.forEach(card -> assertThat(gd.exilePlayPermissionHolders()).containsEntry(card.getId(), player1.getId()));
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        Card milled = new Shock();
        harness.setLibrary(player2, List.of(milled));
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        assertThat(graveyardPlayPermissionsByCard()).containsEntry(milled.getId(), player2.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        impulseCards.forEach(card -> assertThat(gd.exilePlayPermissionHolders()).doesNotContainKey(card.getId()));
        assertThat(gd.exilePlayCardsWithDuration(Duration.UNTIL_END_OF_TURN)).isEmpty();
        assertThat(graveyardPlayPermissionsByCard()).containsEntry(milled.getId(), player2.getId());
        assertThat(graveyardPlayPermissionsByCard().keySet()).containsExactly(milled.getId());

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(graveyardPlayPermissionsByCard()).doesNotContainKey(milled.getId());
        assertThat(graveyardPlayPermissionsByCard().keySet()).isEmpty();
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
        impulseCards.forEach(card -> assertThat(gd.exilePlayPermissionHolders()).containsEntry(card.getId(), player2.getId()));

        harness.passUntil(player2, TurnStep.CLEANUP);
        impulseCards.forEach(card -> assertThat(gd.exilePlayPermissionHolders()).doesNotContainKey(card.getId()));
        assertThat(gd.exilePlayCardsWithDuration(Duration.UNTIL_END_OF_TURN)).isEmpty();
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
        assertThat(gd.exilePlayGrantBySource(musicBox.getId())).isNotNull();

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.exilePlayGrantBySource(musicBox.getId())).isNotNull();
        assertThat(gd.exilePlayPermissionHolders()).containsEntry(exiled.getId(), player2.getId());

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.exilePlayGrantBySource(musicBox.getId())).isNull();
        assertThat(gd.exilePlayPermissionHolders()).doesNotContainKey(exiled.getId());
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
        assertThat(gd.shields(DamageRedirectShield.class)).singleElement()
                .extracting(DamageRedirectShield::remainingAmount).isEqualTo(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.shields(DamageRedirectShield.class)).singleElement()
                .extracting(DamageRedirectShield::remainingAmount).isEqualTo(1);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.shields(DamageRedirectShield.class)).isEmpty();
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
        assertThat(gd.shields(DamageRedirectShield.class)).singleElement()
                .extracting(DamageRedirectShield::remainingAmount).isEqualTo(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);

        assertThat(gd.shields(DamageRedirectShield.class)).isEmpty();
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
        assertThat(gd.shields(SourceNextDamageToAnyTargetShield.class)).hasSize(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.shields(SourceNextDamageToAnyTargetShield.class)).hasSize(1);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.shields(SourceNextDamageToAnyTargetShield.class)).isEmpty();
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
        assertThat(gd.exilesInsteadOfGraveyard(fog.getId())).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(graveyardPlayPermissionsByCard()).containsEntry(fog.getId(), player2.getId());
        assertThat(gd.exilesInsteadOfGraveyard(fog.getId())).isTrue();

        harness.passUntil(player2, TurnStep.END_STEP);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castFromGraveyard(player2, fog.getId());
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(fog);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(fog);
        assertThat(gd.exilesInsteadOfGraveyard(fog.getId())).isFalse();
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
        assertThat(gd.exilesInsteadOfGraveyard(fog.getId())).isTrue();

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(graveyardPlayPermissionsByCard()).doesNotContainKey(fog.getId());
        assertThat(gd.exilesInsteadOfGraveyard(fog.getId())).isFalse();
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
        assertThat(List.of(firstSideboardCard, bears, forest))
                .allMatch(card -> gd.mayPlayFromOutsideGame(card.getId()));

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.mayPlayFromOutsideGame(firstSideboardCard.getId())).isFalse();
        assertThat(gd.floatingPlayerEffects(player2.getId(), OutsideGamePlayPermissionEffect.class))
                .containsExactly(new OutsideGamePlayPermissionEffect(Set.of(bears.getId(), forest.getId()), true, false));

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(List.of(bears, forest)).noneMatch(card -> gd.mayPlayFromOutsideGame(card.getId()));
    }

    @Test
    @DisplayName("\"This turn\" delayed abilities end at the cleanup of the player active when they were made")
    void thisTurnDelayedAbilitiesEndAtTheCleanupOfThePlayerActiveWhenTheyWereRegistered() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card source = new GrizzlyBears();
        var effect = new GainLifeEffect(1);
        gd.temporaryGlobalTriggeredAbilities.add(new TemporaryGlobalTriggeredAbility(
                player1.getId(), source, new TriggeredAbility(Triggers.creatureBlocks(), effect)));
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        var globalTrigger = new TemporaryGlobalTriggeredAbility(
                player2.getId(), source, new TriggeredAbility(Triggers.creatureBlocks(), effect));
        gd.temporaryGlobalTriggeredAbilities.add(globalTrigger);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.temporaryGlobalTriggeredAbilities).containsExactly(globalTrigger);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.temporaryGlobalTriggeredAbilities).isEmpty();
    }

    @Test
    void pendingNextDrawReplacementsAreSplitBetweenTheActivePlayersTheyWereCreatedUnder() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addUntilEndOfTurn(player1, new RegisterNextDrawGainLifeReplacementEffect());
        addUntilEndOfTurn(player1, new RegisterNextDrawCreateBearReplacementEffect("ULG"));
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        addUntilEndOfTurn(player1, new RegisterNextDrawGainLifeReplacementEffect());
        addUntilEndOfTurn(player1, new RegisterNextDrawCreateBearReplacementEffect("MB2"));
        addUntilEndOfTurn(player2, new RegisterNextDrawExileTopCardMayPlayThisTurnEffect());
        FloatingContinuousEffect usedExile = addUntilEndOfTurn(player2,
                new RegisterNextDrawExileTopCardMayPlayThisTurnEffect());
        var damage = new RegisterNextDrawDamageReplacementEffect(new GrizzlyBears(), player1.getId());
        addUntilEndOfTurn(player2, damage);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var reflection = new RegisterMysticReflectionEffect(
                new PendingMysticReflection(bears.getId(), bears.getCard(), bears));
        addUntilEndOfTurn(player2, reflection);
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        gd.endFloatingEffect(usedExile);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.floatingPlayerEffects(player1.getId(), RegisterNextDrawGainLifeReplacementEffect.class))
                .hasSize(1);
        assertThat(gd.floatingPlayerEffects(player1.getId(), RegisterNextDrawCreateBearReplacementEffect.class))
                .containsExactly(new RegisterNextDrawCreateBearReplacementEffect("MB2"));
        assertThat(gd.floatingPlayerEffects(player2.getId(), RegisterNextDrawExileTopCardMayPlayThisTurnEffect.class))
                .hasSize(1);
        assertThat(gd.floatingPlayerEffects(player2.getId(), RegisterNextDrawDamageReplacementEffect.class))
                .containsExactly(damage);
        assertThat(gd.floatingEffectsOfTypes(RegisterMysticReflectionEffect.class))
                .extracting(FloatingContinuousEffect::effect).containsExactly(reflection);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.floatingEffectsOfTypes(RegisterNextDrawGainLifeReplacementEffect.class,
                RegisterNextDrawCreateBearReplacementEffect.class,
                RegisterNextDrawExileTopCardMayPlayThisTurnEffect.class,
                RegisterNextDrawDamageReplacementEffect.class,
                RegisterMysticReflectionEffect.class)).isEmpty();
    }

    private FloatingContinuousEffect addUntilEndOfTurn(com.github.laxika.magicalvibes.model.Player player,
                                                       com.github.laxika.magicalvibes.model.effect.CardEffect effect) {
        return gd.addFloatingEffect(FloatingContinuousEffect.onPlayer("Test", null, player.getId(), effect,
                player.getId(), Duration.UNTIL_END_OF_TURN));
    }

    @Test
    void thisTurnDamageMultipliersAndDrawRedirectsEndAtTheCleanupOfThePlayerActiveWhenTheyWereRegistered() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addMultiplier(player1, false);
        gd.addFloatingEffect(FloatingContinuousEffect.onPlayer("Test", null, player1.getId(),
                new RedirectDrawsEffect(), player2.getId(), Duration.UNTIL_END_OF_TURN));
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        var multiplier = addMultiplier(player2, true);
        gd.addFloatingEffect(FloatingContinuousEffect.onPlayer("Test", null, player2.getId(),
                new RedirectDrawsEffect(), player1.getId(), Duration.UNTIL_END_OF_TURN));
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.floatingEffectsOfTypes(DoubleControllerDamageToOpponentsAndTheirPermanentsEffect.class))
                .containsExactly(multiplier);
        assertThat(gd.floatingEffectsOfTypes(RedirectDrawsEffect.class)).singleElement()
                .satisfies(redirect -> {
                    assertThat(redirect.affectedPlayerId()).isEqualTo(player1.getId());
                    assertThat(redirect.controllerId()).isEqualTo(player2.getId());
                });

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.floatingEffectsOfTypes(DoubleControllerDamageToOpponentsAndTheirPermanentsEffect.class))
                .isEmpty();
        assertThat(gd.floatingEffectsOfTypes(RedirectDrawsEffect.class)).isEmpty();
    }

    @Test
    void turnHistoryRecordsEndAtTheCleanupOfThePlayerActiveWhenTheyWereMade() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent regenerated = addCreatureReady(player1, new GrizzlyBears());
        regenerated.setTimesRegeneratedThisTurn(1);
        gd.playersWhoForetoldThisTurn.add(player1.getId());
        gd.cardsExiledThisTurn = 2;
        gd.creatureExileCountThisTurn.merge(player1.getId(), 1, Integer::sum);
        FloatingCombatRules.skipThisTurn(gd, player1.getId(), SkipStepOrPhaseKind.DRAW_STEP);
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        regenerated.setTimesRegeneratedThisTurn(regenerated.getTimesRegeneratedThisTurn() + 1);
        gd.playersWhoForetoldThisTurn.add(player2.getId());
        gd.playersWhoseSpeedIncreasedThisTurn.add(player2.getId());
        gd.playersWhoPlayedCardFromExileThisTurn.add(player2.getId());
        gd.playersWhoPlayedOrCastFromOutsideHandThisTurn.add(player2.getId());
        gd.playersWhoseCardsLeftGraveyardThisTurn.add(player2.getId());
        gd.playersWhoseCreatureCardsLeftGraveyardThisTurn.add(player2.getId());
        gd.cardsLeftGraveyardCountThisTurn.merge(player2.getId(), 3, Integer::sum);
        gd.cardsExiledThisTurn++;
        gd.creatureExileCountThisTurn.merge(player1.getId(), 1, Integer::sum);
        FloatingCombatRules.skipThisTurn(gd, player2.getId(), SkipStepOrPhaseKind.COMBAT_PHASE);
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(regenerated);
        assertThat(regenerated.getTimesRegeneratedThisTurn()).isEqualTo(1);
        assertThat(gd.playersWhoForetoldThisTurn).containsExactly(player2.getId());
        assertThat(gd.playersWhoseSpeedIncreasedThisTurn).containsExactly(player2.getId());
        assertThat(gd.playersWhoPlayedCardFromExileThisTurn).containsExactly(player2.getId());
        assertThat(gd.playersWhoPlayedOrCastFromOutsideHandThisTurn).containsExactly(player2.getId());
        assertThat(gd.playersWhoseCardsLeftGraveyardThisTurn).containsExactly(player2.getId());
        assertThat(gd.playersWhoseCreatureCardsLeftGraveyardThisTurn).containsExactly(player2.getId());
        assertThat(gd.cardsLeftGraveyardCountThisTurn).containsExactly(Map.entry(player2.getId(), 3));
        assertThat(gd.cardsExiledThisTurn).isEqualTo(1);
        assertThat(gd.creatureExileCountThisTurn).containsExactly(Map.entry(player1.getId(), 1));
        assertThat(gd.skipsStepOrPhase(player1.getId(), SkipStepOrPhaseKind.DRAW_STEP)).isFalse();
        assertThat(gd.skipsStepOrPhase(player2.getId(), SkipStepOrPhaseKind.COMBAT_PHASE)).isTrue();

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(regenerated.getTimesRegeneratedThisTurn()).isZero();
        assertThat(gd.playersWhoForetoldThisTurn).isEmpty();
        assertThat(gd.playersWhoseSpeedIncreasedThisTurn).isEmpty();
        assertThat(gd.playersWhoPlayedCardFromExileThisTurn).isEmpty();
        assertThat(gd.playersWhoPlayedOrCastFromOutsideHandThisTurn).isEmpty();
        assertThat(gd.playersWhoseCardsLeftGraveyardThisTurn).isEmpty();
        assertThat(gd.playersWhoseCreatureCardsLeftGraveyardThisTurn).isEmpty();
        assertThat(gd.cardsLeftGraveyardCountThisTurn).isEmpty();
        assertThat(gd.cardsExiledThisTurn).isZero();
        assertThat(gd.creatureExileCountThisTurn).isEmpty();
        assertThat(gd.floatingEffectsOfTypes(SkipChosenStepOrPhaseEffect.class)).isEmpty();
    }

    @Test
    void tappedForAbilityAndCrewRecordsFromOtherPlayersPhaseSurviveActivePlayersCleanup() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent warMachine = addCreatureReady(player1, new GrizzlyBears());
        UUID ownMerfolk = UUID.randomUUID();
        UUID otherMerfolk = UUID.randomUUID();
        warMachine.recordTappedPermanentForAbility(ownMerfolk);
        warMachine.recordCreatureThatCrewedThisTurn(ownMerfolk);
        gs.advanceStep(gd);
        warMachine.recordTappedPermanentForAbility(otherMerfolk);
        warMachine.recordCreatureThatCrewedThisTurn(otherMerfolk);
        gs.advanceStep(gd);

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(warMachine.getTappedPermanentsForAbilityThisTurn()).containsExactly(otherMerfolk);
        assertThat(warMachine.getCreaturesThatCrewedThisTurn()).containsExactly(otherMerfolk);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(warMachine.getTappedPermanentsForAbilityThisTurn()).isEmpty();
        assertThat(warMachine.getCreaturesThatCrewedThisTurn()).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice restrictions and control-loss delayed abilities end at the cleanup of the player active "
            + "when they began")
    void sacrificeRestrictionsAndControlLossTriggersEndAtTheCleanupOfThePlayerActiveWhenTheyBegan() {
        harness.addToBattlefield(player1, new RunedTerror());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card source = new GrizzlyBears();
        UUID ownCreature = UUID.randomUUID();
        UUID otherCreature = UUID.randomUUID();
        UUID ownEquipment = UUID.randomUUID();
        UUID otherEquipment = UUID.randomUUID();
        gd.addFloatingEffect(FloatingContinuousEffect.onPermanent("Test", null, player1.getId(),
                new ControllerCantSacrificeTargetThisTurnEffect(), ownCreature, Duration.UNTIL_END_OF_TURN));
        gd.temporaryGlobalTriggeredAbilities.add(controlLossUnattach(player1, source, ownEquipment));
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        gd.addFloatingEffect(FloatingContinuousEffect.onPermanent("Test", null, player2.getId(),
                new ControllerCantSacrificeTargetThisTurnEffect(), otherCreature, Duration.UNTIL_END_OF_TURN));
        var otherUnattach = controlLossUnattach(player2, source, otherEquipment);
        gd.temporaryGlobalTriggeredAbilities.add(otherUnattach);
        gs.advanceStep(gd);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());

        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(gd.hasFloatingEffectOnPermanent(ownCreature, ControllerCantSacrificeTargetThisTurnEffect.class))
                .isFalse();
        assertThat(gd.hasFloatingEffectOnPermanent(otherCreature, ControllerCantSacrificeTargetThisTurnEffect.class))
                .isTrue();
        assertThat(gd.temporaryGlobalTriggeredAbilities).containsExactly(otherUnattach);

        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.hasFloatingEffectOnPermanent(otherCreature, ControllerCantSacrificeTargetThisTurnEffect.class))
                .isFalse();
        assertThat(gd.temporaryGlobalTriggeredAbilities).isEmpty();
    }

    /** "When you lose control of that Equipment this turn, unattach it", of {@code controller}. */
    private static TemporaryGlobalTriggeredAbility controlLossUnattach(Player controller, Card source,
                                                                       UUID equipmentId) {
        return TemporaryGlobalTriggeredAbility.about(controller.getId(), source,
                new TriggeredAbility(Triggers.youLoseControlOfThis(),
                        new UnattachEquipmentIfAttachedToControlledCreatureEffect(equipmentId)),
                equipmentId, null, true);
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

    private FloatingContinuousEffect addNextFromMatchingSource(Player player, PermanentPredicate sourcePredicate) {
        return gd.addFloatingEffect(FloatingContinuousEffect.onPlayer("Test", null, player.getId(),
                PreventDamageEffect.nextToControllerFromMatchingSources(sourcePredicate), player.getId(),
                Duration.UNTIL_END_OF_TURN));
    }

    private FloatingContinuousEffect addMultiplier(Player controller, boolean withPermanents) {
        return gd.addFloatingEffect(FloatingContinuousEffect.onPlayer("Test", null, controller.getId(),
                new DoubleControllerDamageToOpponentsAndTheirPermanentsEffect(2, false, withPermanents),
                controller.getId(), Duration.UNTIL_END_OF_TURN));
    }
}
