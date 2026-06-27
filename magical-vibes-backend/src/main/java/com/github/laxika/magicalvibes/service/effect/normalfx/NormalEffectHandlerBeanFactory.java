package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
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
 * <p>Currently holds the <b>Life</b> and <b>Boost</b> domain handlers; later prompts append their
 * domain's handlers here.
 */
public final class NormalEffectHandlerBeanFactory {

    private NormalEffectHandlerBeanFactory() {
    }

    public static List<NormalEffectHandlerBean> createAll(LifeSupport lifeSupport,
                                                          GameQueryService gameQueryService,
                                                          GameBroadcastService gameBroadcastService,
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
                new BoostAllOwnCreaturesByCreatureCardsInGraveyardEffectHandler(gameQueryService, gameBroadcastService)
        );
    }

    public static void registerAll(List<NormalEffectHandlerBean> beans, EffectHandlerRegistry registry) {
        for (NormalEffectHandlerBean bean : beans) {
            registry.register(bean.handledEffect(), bean);
        }
    }
}
