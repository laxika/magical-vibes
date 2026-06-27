package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;

import java.util.List;

/**
 * Single source of truth for instantiating the migrated {@link NormalEffectHandlerBean} handlers in
 * non-Spring sites (e.g. {@code GameTestHarness}, {@code GameSimulator}).
 *
 * <p>In Spring, these handlers are discovered as {@code @Component}s; outside Spring, callers use
 * {@link #createAll} to build the same set manually and {@link #registerAll} to register them. As
 * more domains migrate, only this factory needs updating — non-Spring sites never change again.
 *
 * <p>Currently holds the <b>Life</b>, <b>Boost</b>, and <b>Damage</b> domain handlers.
 */
public final class NormalEffectHandlerBeanFactory {

    private NormalEffectHandlerBeanFactory() {
    }

    public static List<NormalEffectHandlerBean> createAll(LifeSupport lifeSupport,
                                                          DamageSupport damageSupport,
                                                          GameQueryService gameQueryService,
                                                          GameBroadcastService gameBroadcastService,
                                                          GameOutcomeService gameOutcomeService,
                                                          GraveyardService graveyardService,
                                                          PermanentRemovalService permanentRemovalService,
                                                          TriggerCollectionService triggerCollectionService,
                                                          PlayerInputService playerInputService) {
        return List.of(
                new GainLifeEffectHandler(lifeSupport),
                new PayXManaGainXLifeEffectHandler(lifeSupport, gameBroadcastService, playerInputService),
                new GainLifePerCardsInHandEffectHandler(lifeSupport, gameBroadcastService),
                new GainLifePerCreatureOnBattlefieldEffectHandler(lifeSupport, gameQueryService, gameBroadcastService),
                new GainLifePerMatchingPermanentOnBattlefieldEffectHandler(lifeSupport, gameQueryService, gameBroadcastService),
                new GainLifePerControlledMatchingPermanentEffectHandler(lifeSupport, gameQueryService, gameBroadcastService),
                new GainLifeForEachSubtypeOnBattlefieldEffectHandler(lifeSupport),
                new GainLifePerControlledCreatureEffectHandler(lifeSupport, gameQueryService, gameBroadcastService),
                new GainLifePerGraveyardCardEffectHandler(lifeSupport, gameBroadcastService),
                new GainLifePerCreatureCardInGraveyardEffectHandler(lifeSupport, gameBroadcastService),
                new GainLifeEqualToTargetToughnessEffectHandler(lifeSupport, gameQueryService),
                new GainLifeEqualToXValueEffectHandler(lifeSupport),
                new GainLifeMultipliedByXValueEffectHandler(lifeSupport),
                new GainLifeEqualToGreatestPowerAmongOwnCreaturesEffectHandler(lifeSupport, gameQueryService),
                new GainLifeEqualToChargeCountersOnSourceEffectHandler(lifeSupport, gameBroadcastService),
                new DoubleTargetPlayerLifeEffectHandler(lifeSupport, gameBroadcastService),
                new SetTargetPlayerLifeToHalfStartingEffectHandler(lifeSupport, gameBroadcastService),
                new SetTargetPlayerLifeToSpecificValueEffectHandler(lifeSupport, gameBroadcastService),
                new ExchangeTargetPlayersLifeTotalsEffectHandler(gameQueryService, gameBroadcastService, triggerCollectionService),
                new ExchangeLifeTotalWithCreatureStatEffectHandler(gameQueryService, gameBroadcastService, triggerCollectionService),
                new EnchantedCreatureControllerLosesLifeEffectHandler(gameQueryService, gameBroadcastService),
                new LoseLifeEffectHandler(lifeSupport),
                new TargetSpellControllerLosesLifeEffectHandler(lifeSupport),
                new EachOpponentLosesLifeEffectHandler(lifeSupport),
                new EachOpponentLosesLifeAndControllerGainsLifeLostEffectHandler(lifeSupport),
                new EachOpponentLosesXLifeAndControllerGainsLifeLostEffectHandler(lifeSupport),
                new EachPlayerLosesLifeEffectHandler(lifeSupport),
                new EachPlayerLosesFractionOfLifeRoundedUpEffectHandler(lifeSupport),
                new EachPlayerLosesLifePerCreatureControlledEffectHandler(lifeSupport),
                new TargetPlayerLosesLifeAndControllerGainsLifeEffectHandler(lifeSupport, gameQueryService, gameBroadcastService),
                new DrainLifePerControlledPermanentEffectHandler(lifeSupport, gameQueryService, gameBroadcastService),
                new TargetPlayerLosesLifePerControlledPermanentEffectHandler(gameQueryService, gameBroadcastService),
                new TargetPlayerLosesLifeEffectHandler(gameQueryService, gameBroadcastService),
                new TargetPlayerGainsLifeEffectHandler(lifeSupport),
                new EachTargetPlayerGainsLifeEffectHandler(lifeSupport),
                new GiveEnchantedPermanentControllerPoisonCountersEffectHandler(gameQueryService, gameBroadcastService),
                new DamageSourceControllerGetsPoisonCounterEffectHandler(gameQueryService, gameBroadcastService),
                new GiveControllerPoisonCountersEffectHandler(lifeSupport),
                new GiveEachPlayerPoisonCountersEffectHandler(lifeSupport),
                new GiveTargetPlayerPoisonCountersEffectHandler(lifeSupport),
                new GiveControllerPoisonCountersOnTargetDeathThisTurnEffectHandler(gameQueryService),
                new AwardRestrictedManaEffectHandler(gameBroadcastService),
                new AwardManaEffectHandler(gameBroadcastService),
                new AddManaPerControlledPermanentEffectHandler(gameQueryService, gameBroadcastService),
                new BoostSelfEffectHandler(gameQueryService, gameBroadcastService),
                new DoubleSelfPowerToughnessEffectHandler(gameQueryService, gameBroadcastService),
                new BoostSelfPerBlockingCreatureEffectHandler(gameQueryService, gameBroadcastService),
                new BoostSelfPerOtherAttackingSubtypeEffectHandler(gameQueryService, gameBroadcastService),
                new BoostSelfPerControlledPermanentEffectHandler(gameQueryService, gameBroadcastService),
                new BoostTargetCreaturePerControlledPermanentEffectHandler(gameQueryService, gameBroadcastService),
                new BoostTargetCreatureEffectHandler(gameQueryService, gameBroadcastService),
                new BoostTargetCreatureXEffectHandler(gameQueryService, gameBroadcastService),
                new SwitchPowerToughnessEffectHandler(gameQueryService, gameBroadcastService),
                new BoostFirstTargetCreatureEffectHandler(gameQueryService, gameBroadcastService),
                new BoostSecondTargetCreatureEffectHandler(gameQueryService, gameBroadcastService),
                new BoostAllOwnCreaturesEffectHandler(gameQueryService, gameBroadcastService),
                new BoostAllOwnCreaturesByGreatestPowerEffectHandler(gameQueryService, gameBroadcastService),
                new BoostAllCreaturesEffectHandler(gameQueryService, gameBroadcastService),
                new BoostAllCreaturesXEffectHandler(gameQueryService, gameBroadcastService),
                new TapSubtypeBoostSelfAndDamageDefenderEffectHandler(gameQueryService, gameBroadcastService, playerInputService),
                new SetBasePowerToughnessUntilEndOfTurnEffectHandler(gameQueryService, gameBroadcastService),
                new BoostAllOwnCreaturesByCreatureCardsInGraveyardEffectHandler(gameQueryService, gameBroadcastService),
                new DealXDamageToTargetCreatureEffectHandler(damageSupport, gameQueryService),
                new DealDamageToTargetCreatureEffectHandler(damageSupport, gameQueryService),
                new DealDamageToBlockedAttackersOnDeathEffectHandler(damageSupport, gameQueryService),
                new DealDamageToTargetCreatureEqualToControlledSubtypeCountEffectHandler(damageSupport, gameQueryService, lifeSupport),
                new DealDamageToAnyTargetEqualToControlledSubtypeCountAndGainLifeEffectHandler(damageSupport, gameQueryService, gameOutcomeService, lifeSupport),
                new DealXDamageDividedAmongTargetAttackingCreaturesEffectHandler(damageSupport, gameQueryService),
                new MassDamageEffectHandler(damageSupport, gameQueryService, gameOutcomeService),
                new DealDamageToEachPlayerEffectHandler(damageSupport, gameQueryService, gameOutcomeService),
                new DealDamageToEachOpponentEffectHandler(damageSupport, gameQueryService, gameOutcomeService),
                new DealDamageToEachCreatureAndPlaneswalkerOpponentsControlEffectHandler(damageSupport, gameQueryService, gameBroadcastService, gameOutcomeService),
                new DealXDamageToAnyTargetEffectHandler(damageSupport, gameQueryService, gameOutcomeService),
                new DealXDamageDividedEvenlyAmongTargetsEffectHandler(damageSupport, gameQueryService, gameBroadcastService, gameOutcomeService),
                new DealXDamageToEachTargetEffectHandler(damageSupport, gameQueryService, gameBroadcastService, gameOutcomeService),
                new DealXDamageToAnyTargetAndGainXLifeEffectHandler(damageSupport, gameQueryService, gameOutcomeService, lifeSupport),
                new DealDamageToTargetPlayerEffectHandler(damageSupport, gameQueryService, gameOutcomeService),
                new DealDamageToSecondaryTargetEffectHandler(damageSupport, gameQueryService, gameOutcomeService),
                new DealDamageToTriggeringPermanentControllerEffectHandler(damageSupport, gameQueryService, gameOutcomeService),
                new DealDamageToTargetPlayerByHandSizeEffectHandler(damageSupport, gameQueryService, gameOutcomeService),
                new DealDamageToTargetPlayerEqualToCardTypeCountInGraveyardEffectHandler(damageSupport, gameQueryService, gameOutcomeService),
                new DealDamageIfFewCardsInHandEffectHandler(damageSupport, gameQueryService, gameBroadcastService, gameOutcomeService),
                new DealDamageToAnyTargetEffectHandler(damageSupport, gameQueryService, gameOutcomeService),
                new DealDamageToAttackedTargetEffectHandler(damageSupport, gameQueryService, gameBroadcastService, gameOutcomeService),
                new DealDamageToTargetOpponentOrPlaneswalkerEffectHandler(damageSupport, gameQueryService, gameOutcomeService),
                new DealDamageToTargetOpponentAndUpToCreaturesThatPlayerControlsEffectHandler(damageSupport, gameQueryService, gameOutcomeService),
                new MillControllerAndDealDamageByHighestManaValueEffectHandler(damageSupport, gameQueryService, gameBroadcastService, gameOutcomeService, graveyardService),
                new DealDividedDamageToAnyTargetsEffectHandler(damageSupport),
                new DealDividedDamageAmongTargetCreaturesEffectHandler(damageSupport, gameQueryService, gameOutcomeService),
                new DealXDamageDividedAmongTargetCreaturesCantBlockEffectHandler(damageSupport, gameQueryService, gameBroadcastService, gameOutcomeService),
                new DealDividedDamageAmongAnyTargetsEffectHandler(damageSupport, gameQueryService, gameBroadcastService, gameOutcomeService),
                new DealDamageToTargetAndTheirCreaturesEffectHandler(damageSupport, gameQueryService, gameBroadcastService, gameOutcomeService),
                new DealDamageToEachCreatureDamagedPlayerControlsEffectHandler(damageSupport, gameQueryService, gameBroadcastService, gameOutcomeService),
                new DealDamageToTargetCreatureOrPlaneswalkerEffectHandler(damageSupport, gameQueryService),
                new DealDamageToAllCreaturesAndPlaneswalkersTargetControlsEffectHandler(damageSupport, gameQueryService, gameBroadcastService, gameOutcomeService),
                new DealDamageToAllCreaturesTargetControlsEffectHandler(damageSupport, gameQueryService, gameBroadcastService, gameOutcomeService),
                new DealDamageToAnyTargetEqualToChargeCountersOnSourceEffectHandler(damageSupport, gameQueryService, gameBroadcastService, gameOutcomeService),
                new DealDamageEqualToSourcePowerToAnyTargetEffectHandler(damageSupport, gameQueryService, gameOutcomeService),
                new DealDamageEqualToSourceToughnessToTargetCreatureEffectHandler(damageSupport, gameQueryService),
                new SourceFightsTargetCreatureEffectHandler(damageSupport, gameQueryService, gameBroadcastService, gameOutcomeService),
                new PlaneswalkerDealDamageAndReceivePowerDamageEffectHandler(damageSupport, gameQueryService, gameBroadcastService, gameOutcomeService),
                new PackHuntEffectHandler(damageSupport, gameQueryService, gameBroadcastService, gameOutcomeService),
                new DealOrderedDamageToAnyTargetsEffectHandler(damageSupport, gameQueryService, gameBroadcastService, gameOutcomeService),
                new DealDamageToAnyTargetAndGainLifeEffectHandler(damageSupport, gameQueryService, gameOutcomeService, lifeSupport),
                new FirstTargetDealsPowerDamageToSecondTargetEffectHandler(damageSupport, gameQueryService, gameBroadcastService),
                new FirstTargetFightsSecondTargetEffectHandler(damageSupport, gameQueryService, gameBroadcastService),
                new MassFightTargetCreatureEffectHandler(damageSupport, gameQueryService, gameBroadcastService, gameOutcomeService),
                new RevealTopCardDealManaValueDamageEffectHandler(damageSupport, gameQueryService, gameBroadcastService, gameOutcomeService),
                new DealDamageToControllerEffectHandler(damageSupport, gameQueryService, gameBroadcastService, gameOutcomeService),
                new ExileUntilNonlandToHandRepeatIfHighMVEffectHandler(damageSupport, gameQueryService, gameBroadcastService, gameOutcomeService),
                new DealDamageToEnchantedPlayerEffectHandler(damageSupport, gameQueryService, gameOutcomeService),
                new EnchantedCreatureDealsDamageToItsOwnerEffectHandler(damageSupport, gameQueryService, gameOutcomeService),
                new EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffectHandler(damageSupport, gameQueryService, gameOutcomeService),
                new DealDamageToTargetControllerIfTargetHasKeywordEffectHandler(damageSupport, gameQueryService, gameBroadcastService, gameOutcomeService),
                new DealDamageToTargetCreatureControllerEffectHandler(damageSupport, gameQueryService, gameBroadcastService, gameOutcomeService),
                new DealDamageToEachOpponentEqualToCardsDrawnThisTurnEffectHandler(damageSupport, gameQueryService, gameBroadcastService, gameOutcomeService),
                new DealDamageToEachOpponentEqualToPlusOnePlusOneCountersOnSourceEffectHandler(damageSupport, gameQueryService, gameBroadcastService, gameOutcomeService),
                new SacrificeSelfAndDealDamageToDamagedPlayerEffectHandler(damageSupport, gameQueryService, gameBroadcastService, gameOutcomeService, permanentRemovalService),
                new BoostColorSourceDamageThisTurnEffectHandler(gameBroadcastService)
        );
    }

    public static void registerAll(List<NormalEffectHandlerBean> beans, EffectHandlerRegistry registry) {
        for (NormalEffectHandlerBean bean : beans) {
            registry.register(bean.handledEffect(), bean);
        }
    }
}
