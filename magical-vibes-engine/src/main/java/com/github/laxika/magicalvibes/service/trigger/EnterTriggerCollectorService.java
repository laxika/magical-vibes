package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Trigger collectors for enter-the-battlefield events. Mirrors the other {@code *CollectorService}
 * beans: each {@link CollectsTrigger}-annotated method handles one (slot, effect class) pair and the
 * per-slot {@code CardEffect.class} default puts the effect onto the stack. The scan orchestration
 * (which permanents/slots to visit, self-exclusion, conditional gating, Naban doubling, the
 * "skip targeting effects" rule) lives in {@link TriggerCollectionService}.
 */
@Slf4j
@Service
public class EnterTriggerCollectorService {

    private final GameBroadcastService gameBroadcastService;
    private final AmountEvaluationService amountEvaluationService;

    public EnterTriggerCollectorService(GameBroadcastService gameBroadcastService,
                                        AmountEvaluationService amountEvaluationService) {
        this.gameBroadcastService = gameBroadcastService;
        this.amountEvaluationService = amountEvaluationService;
    }

    // ── Default "put it on the stack" fallbacks (one per registry-backed slot) ─────────

    @CollectsTriggers({
            @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD),
            @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD),
            @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD),
            @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD),
    })
    private boolean handleEnterDefault(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        enqueue(match, effect, pe.defaultTargetPlayerId(), pe.perEffectTriggerCount());
        logTriggered(match);
        return true;
    }

    /**
     * The "any other creature enters" default only auto-queues non-targeting triggers (plus the
     * explicitly handled life-gain / damage triggers below): a generic targeting effect in that
     * slot is skipped rather than queued with a stray target.
     */
    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAnyCreatureEnterDefault(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        if (isTargeting(effect)) {
            return false;
        }
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        enqueue(match, effect, pe.defaultTargetPlayerId(), pe.perEffectTriggerCount());
        logTriggered(match);
        return true;
    }

    // ── "May" wrappers (queued as a may-ability, not unwrapped onto the stack) ──────────

    @CollectsTriggers({
            @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD),
            @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD),
            @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD),
    })
    private boolean handleEnterMay(TriggerMatchContext match, MayEffect may, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        Card sourceCard = match.permanent().getCard();
        if (pe.defaultTargetPlayerId() != null) {
            match.gameData().queueMayAbility(sourceCard, match.controllerId(), may,
                    pe.defaultTargetPlayerId(), match.permanent().getId());
        } else {
            match.gameData().queueMayAbility(sourceCard, match.controllerId(), may);
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (may effect)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    @CollectsTriggers({
            @CollectsTrigger(value = MayPayManaEffect.class, slot = EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD),
            @CollectsTrigger(value = MayPayManaEffect.class, slot = EffectSlot.ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD),
    })
    private boolean handleEnterMayPay(TriggerMatchContext match, MayPayManaEffect mayPay, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        Card sourceCard = match.permanent().getCard();
        match.gameData().queueMayAbility(sourceCard, match.controllerId(), mayPay, pe.mayPayTargetCardId());
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (may pay mana)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    // ── Value-materialising effects ─────────────────────────────────────────────────────

    @CollectsTrigger(value = GainLifeEffect.class, slot = EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAnyCreatureGainLife(TriggerMatchContext match, GainLifeEffect gainLife, TriggerContext ctx) {
        int amount = amountEvaluationService.evaluate(match.gameData(), gainLife.amount(),
                new AmountContext(match.controllerId(), match.permanent(), null, 0, 0, false));
        return enqueueGainLife(match, ctx, amount);
    }

    @CollectsTrigger(value = GainLifeEqualToToughnessEffect.class, slot = EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAllyCreatureGainLifeEqualToToughness(TriggerMatchContext match,
            GainLifeEqualToToughnessEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        return enqueueGainLife(match, ctx, pe.enteringCard().getToughness());
    }

    private boolean enqueueGainLife(TriggerMatchContext match, TriggerContext ctx, int amount) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();
        enqueue(match, new GainLifeEffect(amount), pe.defaultTargetPlayerId(), pe.perEffectTriggerCount());
        String controllerName = gameData.playerIdToName.get(match.controllerId());
        gameBroadcastService.logAndBroadcast(gameData,
                cardName + " triggers — " + controllerName + " will gain " + amount + " life.");
        log.info("Game {} - {} triggers for {} entering (gain {} life)",
                gameData.id, cardName, pe.enteringCard().getName(), amount);
        return true;
    }

    @CollectsTriggers({
            @CollectsTrigger(value = DealDamageToTargetPlayerEffect.class, slot = EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD),
            @CollectsTrigger(value = DealDamageToTargetPlayerEffect.class, slot = EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD),
    })
    private boolean handleDealDamageToEnteringController(TriggerMatchContext match,
            DealDamageToTargetPlayerEffect damageEffect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        var gameData = match.gameData();
        String cardName = match.permanent().getCard().getName();
        UUID targetPlayerId = pe.enteringControllerId();
        enqueue(match, new DealDamageToTargetPlayerEffect(damageEffect.damage()), targetPlayerId,
                pe.perEffectTriggerCount());
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        gameBroadcastService.logAndBroadcast(gameData,
                cardName + " triggers — deals " + damageEffect.damage() + " damage to " + targetName + ".");
        log.info("Game {} - {} triggers for {} entering (deal {} damage to controller)",
                gameData.id, cardName, pe.enteringCard().getName(), damageEffect.damage());
        return true;
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────────────

    private void enqueue(TriggerMatchContext match, CardEffect effect, UUID targetPlayerId, int count) {
        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < count; i++) {
            match.gameData().stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    targetPlayerId,
                    match.permanent().getId()
            ));
        }
    }

    private void logTriggered(TriggerMatchContext match) {
        gameBroadcastService.logAndBroadcast(match.gameData(),
                match.permanent().getCard().getName() + "'s ability triggers.");
    }

    private static boolean isTargeting(CardEffect effect) {
        return effect.canTargetPlayer()
                || effect.canTargetPermanent()
                || effect.canTargetSpell()
                || effect.canTargetGraveyard()
                || effect.canTargetAnyGraveyard();
    }
}
