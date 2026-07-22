package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AttachSourceAuraToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.AttachSourceAuraToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCategory;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEqualToEnteringPowerPutOneOnTopRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEqualToEnteringPowerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SoulbondPairWithEnteringEffect;
import com.github.laxika.magicalvibes.model.effect.TransformEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TransformTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.github.laxika.magicalvibes.model.GameLog;
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
            @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD),
    })
    private boolean handleEnterDefault(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        // A permanent-targeting effect (e.g. Reaper King's "destroy target permanent") can't be pushed
        // straight onto the stack with a pre-set target — queue a pending choice so the controller picks it.
        if (effect.targetSpec().category().includesPermanents()) {
            Card sourceCard = match.permanent().getCard();
            match.gameData().queueInteraction(new PermanentChoiceContext.EntersTriggerTarget(
                    sourceCard, match.controllerId(), new ArrayList<>(List.of(effect)), match.permanent().getId()));
            logTriggered(match);
            return true;
        }
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
        // "You may gain life equal to that creature's toughness" (e.g. Orchard Warden): read the
        // entering creature's toughness now, since the wrapped effect loses that context once queued.
        if (may.wrapped() instanceof GainLifeEqualToToughnessEffect) {
            may = new MayEffect(new GainLifeEffect(pe.enteringCard().getToughness()), may.prompt());
        }
        // Always bind the source permanent so a "may put a counter on this creature" wrapper
        // (e.g. Godtracker of Jund) resolves against the source; ally scans leave the target
        // player unset (null), which is harmless for player-directed wrapped effects.
        match.gameData().queueMayAbility(sourceCard, match.controllerId(), may,
                pe.defaultTargetPlayerId(), match.permanent().getId());
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (may effect)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    @CollectsTriggers({
            @CollectsTrigger(value = MayPayManaEffect.class, slot = EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD),
            @CollectsTrigger(value = MayPayManaEffect.class, slot = EffectSlot.ON_ALLY_NONTOKEN_ARTIFACT_ENTERS_BATTLEFIELD),
            @CollectsTrigger(value = MayPayManaEffect.class, slot = EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD),
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
        Card sourceCard = match.permanent().getCard();
        String cardName = sourceCard.getName();
        enqueue(match, new GainLifeEffect(amount), pe.defaultTargetPlayerId(), pe.perEffectTriggerCount());
        String controllerName = gameData.playerIdToName.get(match.controllerId());
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(sourceCard,
                " triggers — " + controllerName + " will gain " + amount + " life."));
        log.info("Game {} - {} triggers for {} entering (gain {} life)",
                gameData.id, cardName, pe.enteringCard().getName(), amount);
        return true;
    }

    @CollectsTriggers({
            @CollectsTrigger(value = DealDamageToPlayersEffect.class, slot = EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD),
            @CollectsTrigger(value = DealDamageToPlayersEffect.class, slot = EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD),
    })
    private boolean handleDealDamageToEnteringController(TriggerMatchContext match,
            DealDamageToPlayersEffect damageEffect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        var gameData = match.gameData();
        Card sourceCard = match.permanent().getCard();
        String cardName = sourceCard.getName();
        UUID targetPlayerId = pe.enteringControllerId();
        enqueue(match, new DealDamageToPlayersEffect(damageEffect.amount(), DamageRecipient.TARGET_PLAYER), targetPlayerId,
                pe.perEffectTriggerCount());
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(sourceCard,
                " triggers — deals " + damageEffect.amount() + " damage to " + targetName + "."));
        log.info("Game {} - {} triggers for {} entering (deal {} damage to controller)",
                gameData.id, cardName, pe.enteringCard().getName(), damageEffect.amount());
        return true;
    }

    /**
     * "Whenever a creature enters, its controller sacrifices a [permanent] of their choice" (Tainted
     * Aether). The authored effect already carries {@code SacrificeRecipient.TARGET_PLAYER}; here we
     * queue it with the entering creature's controller as the sacrificing player, mirroring the
     * damage-to-entering-controller handler above.
     */
    @CollectsTrigger(value = SacrificePermanentsEffect.class,
            slot = EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAnyCreatureSacrifice(TriggerMatchContext match,
            SacrificePermanentsEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        enqueue(match, effect, pe.enteringControllerId(), pe.perEffectTriggerCount());
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (controller sacrifices)",
                match.gameData().id, match.permanent().getCard().getName(), pe.enteringCard().getName());
        return true;
    }

    /**
     * "Whenever a creature enters, this enchantment deals N damage to it" (Aether Flash). The entering
     * creature is the (non-chosen) recipient, so we resolve its permanent id now and queue a normal
     * {@link DealDamageToTargetCreatureEffect} with that id baked in as the target — the source being
     * this permanent, so prevention/protection/damage-triggers all key off it.
     */
    @CollectsTrigger(value = DealDamageToTargetCreatureEffect.class,
            slot = EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAnyCreatureDealDamageToEntering(TriggerMatchContext match,
            DealDamageToTargetCreatureEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            // The creature already left the battlefield; nothing to damage.
            return true;
        }
        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            match.gameData().stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    enteringPermanentId,
                    match.permanent().getId()));
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (deal damage to entering creature)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    /**
     * "Whenever a nontoken [creature] you control enters, create a token that's a copy of that
     * creature" (Necroduality). The entering creature is fixed at trigger time — bake its id as
     * {@code targetId} rather than routing through the EntersTriggerTarget choice pipeline that
     * the permanent-targeting default would use. Subtype gating (e.g. Zombie) is applied upstream
     * by {@code TriggeringCardConditionalEffect}; tokens are already excluded by the slot.
     */
    @CollectsTrigger(value = CreateTokenCopyOfTargetPermanentEffect.class,
            slot = EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAllyNontokenCreatureCreateTokenCopy(TriggerMatchContext match,
            CreateTokenCopyOfTargetPermanentEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        UUID enteringPermanentId = pe.mayPayTargetCardId();
        if (enteringPermanentId == null) {
            enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        }
        if (enteringPermanentId == null) {
            // The creature already left the battlefield; nothing to copy.
            return true;
        }
        Card sourceCard = match.permanent().getCard();
        for (int i = 0; i < pe.perEffectTriggerCount(); i++) {
            match.gameData().stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    enteringPermanentId,
                    match.permanent().getId()));
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (create token copy of entering creature)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    @CollectsTrigger(value = PutCountersOnSourceEqualToEnteringPowerEffect.class,
            slot = EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAnyCreaturePutCountersEqualToPower(TriggerMatchContext match,
            PutCountersOnSourceEqualToEnteringPowerEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        int power = Math.max(0, pe.enteringCard().getPower());
        Card sourceCard = match.permanent().getCard();
        var counters = new PutCountersOnSourceEffect(effect.powerModifier(), effect.toughnessModifier(), power);
        if (effect.optional()) {
            var may = new MayEffect(counters, "Put " + power + " counter(s) on " + sourceCard.getName() + "?");
            match.gameData().queueMayAbility(sourceCard, match.controllerId(), may, null, match.permanent().getId());
        } else {
            enqueue(match, counters, pe.defaultTargetPlayerId(), pe.perEffectTriggerCount());
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (put {} +1/+1 counter(s))",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName(), power);
        return true;
    }

    /**
     * "Whenever a [subtype] you control enters, [you may] transform it" (Vildin-Pack Alpha). The
     * subtype gate is applied upstream by {@code TriggeringCardConditionalEffect}; here we resolve
     * the entering permanent and either queue a "you may" ({@code optional}) or bake a mandatory
     * transform onto the stack — both whose {@code targetId} is that creature.
     */
    @CollectsTrigger(value = TransformEnteringCreatureEffect.class,
            slot = EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAllyTransformEntering(TriggerMatchContext match,
            TransformEnteringCreatureEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        Card sourceCard = match.permanent().getCard();
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            return true;
        }
        var transform = new TransformTargetPermanentEffect();
        if (effect.optional()) {
            var may = new MayEffect(transform, "Transform " + pe.enteringCard().getName() + "?");
            match.gameData().queueMayAbility(sourceCard, match.controllerId(), may,
                    enteringPermanentId, match.permanent().getId());
        } else {
            match.gameData().stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(transform)),
                    enteringPermanentId,
                    match.permanent().getId()));
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering ({} transform it)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName(),
                effect.optional() ? "may" : "mandatory");
        return true;
    }

    /**
     * "Whenever a creature you control [gated] enters, [you may] put M +1/+1 counters on it"
     * (Mighty Emergence "you may", Sigil Captain mandatory). The gate is applied upstream by an
     * {@code EnterCreatureConditionalEffect}; here we resolve the entering permanent and either queue a
     * "you may" ({@code optional}) or bake a mandatory counter placement onto the stack — both whose
     * {@code targetId} is that creature, resolved via {@link PutCounterOnTargetPermanentEffect}.
     */
    @CollectsTrigger(value = PutCountersOnEnteringCreatureEffect.class,
            slot = EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAllyPutCountersOnEntering(TriggerMatchContext match,
            PutCountersOnEnteringCreatureEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        Card sourceCard = match.permanent().getCard();
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            // The creature already left the battlefield; nothing to add counters to.
            return true;
        }
        var counters = new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, effect.count());
        if (effect.optional()) {
            var may = new MayEffect(counters,
                    "Put " + effect.count() + " +1/+1 counter(s) on " + pe.enteringCard().getName() + "?");
            match.gameData().queueMayAbility(sourceCard, match.controllerId(), may,
                    enteringPermanentId, match.permanent().getId());
        } else {
            match.gameData().stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sourceCard,
                    match.controllerId(),
                    sourceCard.getName() + "'s ability",
                    new ArrayList<>(List.of(counters)),
                    enteringPermanentId,
                    match.permanent().getId()));
        }
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering ({} put {} +1/+1 counter(s) on it)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName(),
                effect.optional() ? "may" : "mandatory", effect.count());
        return true;
    }

    /**
     * Soulbond "whenever another unpaired creature enters" (CR 702.94a). Intervening-if: both this
     * permanent and the entering creature are unpaired creatures under the controller. Queues a may
     * with the entering permanent baked as {@code targetId}.
     */
    @CollectsTrigger(value = SoulbondPairWithEnteringEffect.class,
            slot = EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAllySoulbondPairWithEntering(TriggerMatchContext match,
            SoulbondPairWithEnteringEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        Card sourceCard = match.permanent().getCard();
        if (match.permanent().getPairedWithId() != null) {
            return true;
        }
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            return true;
        }
        var entering = new Permanent[1];
        match.gameData().forEachPermanent((playerId, perm) -> {
            if (entering[0] == null && perm.getId().equals(enteringPermanentId)) {
                entering[0] = perm;
            }
        });
        if (entering[0] == null || entering[0].getPairedWithId() != null) {
            return true;
        }
        var may = new MayEffect(effect, "Pair " + sourceCard.getName() + " with " + pe.enteringCard().getName() + "?");
        match.gameData().queueMayAbility(sourceCard, match.controllerId(), may,
                enteringPermanentId, match.permanent().getId());
        logTriggered(match);
        log.info("Game {} - {} soulbond may-pair with entering {}",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    @CollectsTrigger(value = LookAtTopCardsEqualToEnteringPowerPutOneOnTopRestOnBottomEffect.class,
            slot = EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAllyLookAtTopEqualToEnteringPower(TriggerMatchContext match,
            LookAtTopCardsEqualToEnteringPowerPutOneOnTopRestOnBottomEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        int power = Math.max(0, pe.enteringCard().getPower());
        Card sourceCard = match.permanent().getCard();
        // X = 0: looking at zero cards accomplishes nothing, so skip the "you may" prompt entirely.
        if (power <= 0) {
            logTriggered(match);
            return true;
        }
        var look = LookAtTopCardsEffect.putOneOnTopRestOnBottom(power);
        var may = new MayEffect(look, "Look at the top " + power + " card(s) of your library?");
        match.gameData().queueMayAbility(sourceCard, match.controllerId(), may, null, match.permanent().getId());
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (look at top {})",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName(), power);
        return true;
    }

    /**
     * "Whenever a [subtype] creature enters, you may attach this Equipment to it" (Cloak and Dagger).
     * The subtype gate is applied upstream by {@code TriggeringCardConditionalEffect}; here we resolve
     * the entering permanent (which may be under any player's control) and queue a "you may attach"
     * whose {@code targetId} is that creature and {@code sourcePermanentId} is this Equipment.
     */
    @CollectsTrigger(value = AttachSourceEquipmentToEnteringCreatureEffect.class,
            slot = EffectSlot.ON_ANY_OTHER_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleAnyCreatureAttachEquipment(TriggerMatchContext match,
            AttachSourceEquipmentToEnteringCreatureEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        Card sourceCard = match.permanent().getCard();
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            // The creature already left the battlefield; nothing to attach to.
            return true;
        }
        var may = new MayEffect(new AttachSourceEquipmentToTargetCreatureEffect(),
                "Attach " + sourceCard.getName() + " to " + pe.enteringCard().getName() + "?");
        match.gameData().queueMayAbility(sourceCard, match.controllerId(), may,
                enteringPermanentId, match.permanent().getId());
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (may attach equipment)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    /**
     * "Whenever a creature an opponent controls enters, you may attach this Aura to that creature"
     * (Prison Term). Resolves the entering permanent (under an opponent's control) and queues a
     * "you may attach" whose {@code targetId} is that creature and {@code sourcePermanentId} is this
     * Aura, so the Aura's controller chooses whether to move it.
     */
    @CollectsTrigger(value = AttachSourceAuraToEnteringCreatureEffect.class,
            slot = EffectSlot.ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD)
    private boolean handleOpponentCreatureAttachAura(TriggerMatchContext match,
            AttachSourceAuraToEnteringCreatureEffect effect, TriggerContext ctx) {
        TriggerContext.PermanentEnters pe = (TriggerContext.PermanentEnters) ctx;
        Card sourceCard = match.permanent().getCard();
        UUID enteringPermanentId = findEnteringPermanentId(match, pe.enteringCard());
        if (enteringPermanentId == null) {
            // The creature already left the battlefield; nothing to attach to.
            return true;
        }
        var may = new MayEffect(new AttachSourceAuraToTargetCreatureEffect(),
                "Attach " + sourceCard.getName() + " to " + pe.enteringCard().getName() + "?");
        match.gameData().queueMayAbility(sourceCard, match.controllerId(), may,
                enteringPermanentId, match.permanent().getId());
        logTriggered(match);
        log.info("Game {} - {} triggers for {} entering (may attach aura)",
                match.gameData().id, sourceCard.getName(), pe.enteringCard().getName());
        return true;
    }

    private UUID findEnteringPermanentId(TriggerMatchContext match, Card enteringCard) {
        UUID[] found = new UUID[1];
        match.gameData().forEachPermanent((playerId, perm) -> {
            if (found[0] == null && perm.getCard() == enteringCard) {
                found[0] = perm.getId();
            }
        });
        return found[0];
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
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
    }

    private static boolean isTargeting(CardEffect effect) {
        TargetCategory category = effect.targetSpec().category();
        return category.includesPlayers()
                || category.includesPermanents()
                || EffectResolution.targetsSpellOnStack(effect)
                || category.isGraveyard();
    }
}
