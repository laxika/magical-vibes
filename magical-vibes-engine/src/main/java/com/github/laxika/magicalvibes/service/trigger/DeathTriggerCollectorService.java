package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerLosesGameOnLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenWithDyingSourceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensForEachDyingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToBlockedAttackersOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForEachDyingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.effect.DyingCreatureControllerDiscardsCardEffect;
import com.github.laxika.magicalvibes.model.effect.DyingCreatureControllerMayDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedControllerSacrificesCreatureOnLeaveEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentLeavesConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MoveDyingSourceCountersToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetForEachDyingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEqualToDyingPowerEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveLinkedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedCreatureOnLeaveEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingCreatureToBattlefieldAndAttachSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDyingOpponentCreatureUnderYourControlEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToBattlefieldUnderOwnersControlOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToOwnerHandOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToOpponentCreatureOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceAuraToSharedTypeCreatureOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringLandFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.StealDyingOpponentPermanentUnlessPaysLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.model.effect.UntapEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEqualToPowerEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Trigger collectors for death and leaves-battlefield events.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeathTriggerCollectorService {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;

    // ── ON_DEATH (dying card's own death triggers) ─────────────────────

    @CollectsTrigger(value = DealDamageToBlockedAttackersOnDeathEffect.class, slot = EffectSlot.ON_DEATH)
    boolean handleDealDamageToBlockedAttackers(TriggerMatchContext match,
            DealDamageToBlockedAttackersOnDeathEffect deathDmg, TriggerContext ctx) {
        TriggerContext.SelfDeath sd = (TriggerContext.SelfDeath) ctx;
        Permanent dyingPermanent = sd.dyingPermanent();
        TurnStep step = match.gameData().currentStep;
        if (dyingPermanent == null || step == null
                || step.ordinal() < TurnStep.BEGINNING_OF_COMBAT.ordinal()
                || step.ordinal() > TurnStep.END_OF_COMBAT.ordinal()
                || dyingPermanent.getBlockingTargetIds().isEmpty()) {
            return false;
        }
        List<UUID> targetIds = new ArrayList<>(dyingPermanent.getBlockingTargetIds());
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sd.dyingCard(),
                sd.controllerId(),
                sd.dyingCard().getName() + "'s ability",
                new ArrayList<>(List.of(deathDmg)),
                0,
                targetIds
        ));
        return true;
    }

    @CollectsTrigger(value = CreateTokenWithDyingSourceCountersEffect.class, slot = EffectSlot.ON_DEATH)
    boolean handleCreateTokenWithDyingSourceCounters(TriggerMatchContext match,
            CreateTokenWithDyingSourceCountersEffect effect, TriggerContext ctx) {
        TriggerContext.SelfDeath sd = (TriggerContext.SelfDeath) ctx;
        Permanent dyingPermanent = sd.dyingPermanent();
        if (dyingPermanent == null) {
            return false;
        }
        // Intervening-if: only fires if it had one or more +1/+1 counters on it.
        int counters = dyingPermanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);
        if (counters < 1) {
            return false;
        }

        CreateTokenEffect t = effect.tokenTemplate();
        CreateTokenEffect resolved = new CreateTokenEffect(
                t.primaryType(), t.amount(), t.tokenName(), t.power(), t.toughness(),
                t.color(), t.colors(), t.subtypes(), t.keywords(), t.additionalTypes(),
                t.tappedAndAttacking(), t.tapped(), t.tokenEffects(), t.tokenAbilities(),
                t.exileAtEndOfCombat(), t.exileAtEndStep(), t.legendary(), counters,
                t.grantedKeywordsUntilEndOfTurn());

        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sd.dyingCard(),
                sd.controllerId(),
                sd.dyingCard().getName() + "'s ability",
                new ArrayList<>(List.of(resolved))
        ));
        return true;
    }

    @CollectsTrigger(value = CreateTokensForEachDyingSourceCounterEffect.class, slot = EffectSlot.ON_DEATH)
    boolean handleCreateTokensForEachDyingSourceCounter(TriggerMatchContext match,
            CreateTokensForEachDyingSourceCounterEffect effect, TriggerContext ctx) {
        TriggerContext.SelfDeath sd = (TriggerContext.SelfDeath) ctx;
        Permanent dyingPermanent = sd.dyingPermanent();
        if (dyingPermanent == null) {
            return false;
        }
        // Snapshot the total counter count across every concrete counter type (ANY and SILVER are
        // wildcard categories, not stored on a permanent) — one token is created per counter.
        int counters = 0;
        for (CounterType type : CounterType.values()) {
            if (type == CounterType.ANY || type == CounterType.SILVER) {
                continue;
            }
            counters += dyingPermanent.getCounterCount(type);
        }
        if (counters < 1) {
            return false;
        }

        CreateTokenEffect resolved = effect.tokenTemplate().withAmount(counters);

        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sd.dyingCard(),
                sd.controllerId(),
                sd.dyingCard().getName() + "'s ability",
                new ArrayList<>(List.of(resolved))
        ));
        return true;
    }

    @CollectsTrigger(value = DrawCardForEachDyingSourceCounterEffect.class, slot = EffectSlot.ON_DEATH)
    boolean handleDrawCardForEachDyingSourceCounter(TriggerMatchContext match,
            DrawCardForEachDyingSourceCounterEffect effect, TriggerContext ctx) {
        TriggerContext.SelfDeath sd = (TriggerContext.SelfDeath) ctx;
        Permanent dyingPermanent = sd.dyingPermanent();
        if (dyingPermanent == null) {
            return false;
        }
        // Snapshot the counter count at death — the permanent is already off the battlefield by the
        // time this resolves, so resolve into a plain draw for that fixed amount.
        int counters = dyingPermanent.getCounterCount(effect.counterType());
        if (counters < 1) {
            return false;
        }

        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sd.dyingCard(),
                sd.controllerId(),
                sd.dyingCard().getName() + "'s ability",
                new ArrayList<>(List.of(new DrawCardEffect(counters)))
        ));
        return true;
    }

    @CollectsTrigger(value = PutCounterOnTargetForEachDyingSourceCounterEffect.class, slot = EffectSlot.ON_DEATH)
    boolean handlePutCounterOnTargetForEachDyingSourceCounter(TriggerMatchContext match,
            PutCounterOnTargetForEachDyingSourceCounterEffect effect, TriggerContext ctx) {
        TriggerContext.SelfDeath sd = (TriggerContext.SelfDeath) ctx;
        Permanent dyingPermanent = sd.dyingPermanent();
        if (dyingPermanent == null) {
            return false;
        }
        // Snapshot the counter count at death — the permanent is off the battlefield by the time this
        // resolves, so bake the fixed amount into the effect carried to target selection.
        int counters = dyingPermanent.getCounterCount(effect.counterType());
        CardEffect baked = new PutCounterOnTargetForEachDyingSourceCounterEffect(
                effect.counterType(), counters, effect.optional());
        // "you may …" (Soulstinger): the target is still chosen now (CR 603.3d), but the controller
        // may decline placing the counters when the trigger resolves — gate it behind a MayEffect.
        CardEffect queued = effect.optional()
                ? new MayEffect(baked, "put a -1/-1 counter on target creature for each -1/-1 counter on "
                        + sd.dyingCard().getName() + "?")
                : baked;
        match.gameData().queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                sd.dyingCard(), sd.controllerId(), new ArrayList<>(List.of(queued))
        ));
        return true;
    }

    @CollectsTrigger(value = MoveDyingSourceCountersToTargetCreatureEffect.class, slot = EffectSlot.ON_DEATH)
    boolean handleMoveDyingSourceCounters(TriggerMatchContext match,
            MoveDyingSourceCountersToTargetCreatureEffect effect, TriggerContext ctx) {
        TriggerContext.SelfDeath sd = (TriggerContext.SelfDeath) ctx;
        Permanent dyingPermanent = sd.dyingPermanent();
        if (dyingPermanent == null) {
            return false;
        }
        // Intervening-if: only fires if it had one or more counters on it. Snapshot every concrete
        // counter type (ANY and SILVER are wildcard categories, not stored on a permanent).
        Map<CounterType, Integer> snapshot = new EnumMap<>(CounterType.class);
        for (CounterType type : CounterType.values()) {
            if (type == CounterType.ANY || type == CounterType.SILVER) {
                continue;
            }
            int count = dyingPermanent.getCounterCount(type);
            if (count > 0) {
                snapshot.put(type, count);
            }
        }
        if (snapshot.isEmpty()) {
            return false;
        }

        match.gameData().queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                sd.dyingCard(), sd.controllerId(),
                new ArrayList<>(List.of(new MoveDyingSourceCountersToTargetCreatureEffect(snapshot)))
        ));
        return true;
    }

    @CollectsTrigger(value = MayPayManaEffect.class, slot = EffectSlot.ON_DEATH)
    boolean handleDeathMayPayMana(TriggerMatchContext match,
            MayPayManaEffect mayPay, TriggerContext ctx) {
        TriggerContext.SelfDeath sd = (TriggerContext.SelfDeath) ctx;
        match.gameData().queueMayAbility(sd.dyingCard(), sd.controllerId(), mayPay, null);
        return true;
    }

    @CollectsTrigger(value = TargetPlayerLosesLifeEqualToPowerEffect.class, slot = EffectSlot.ON_DEATH)
    boolean handleLosesLifeEqualToPower(TriggerMatchContext match,
            TargetPlayerLosesLifeEqualToPowerEffect effect, TriggerContext ctx) {
        TriggerContext.SelfDeath sd = (TriggerContext.SelfDeath) ctx;
        Permanent dyingPermanent = sd.dyingPermanent();
        int power = dyingPermanent != null ? dyingPermanent.getEffectivePower()
                : (sd.dyingCard().getPower() != null ? sd.dyingCard().getPower() : 0);
        CardEffect resolved = new LoseLifeEffect(Math.max(0, power), LoseLifeRecipient.TARGET_PLAYER);
        match.gameData().queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                sd.dyingCard(), sd.controllerId(), new ArrayList<>(List.of(resolved))
        ));
        return true;
    }

    @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_DEATH)
    boolean handleDeathMayEffect(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        TriggerContext.SelfDeath sd = (TriggerContext.SelfDeath) ctx;
        // CR 603.3d: targeted "may" abilities need the target chosen when stacking
        if (may.targetSpec().category().includesPermanents() || may.targetSpec().category().includesPlayers()) {
            match.gameData().queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                    sd.dyingCard(), sd.controllerId(), new ArrayList<>(List.of(may))
            ));
        } else {
            match.gameData().queueMayAbility(sd.dyingCard(), sd.controllerId(), may);
        }
        return true;
    }

    @CollectsTrigger(value = ReturnAllCardsExiledWithSourceEffect.class, slot = EffectSlot.ON_DEATH)
    boolean handleReturnAllCardsExiledWithSource(TriggerMatchContext match,
            ReturnAllCardsExiledWithSourceEffect effect, TriggerContext ctx) {
        TriggerContext.SelfDeath sd = (TriggerContext.SelfDeath) ctx;
        Permanent dyingPermanent = sd.dyingPermanent();
        if (dyingPermanent == null) {
            return false;
        }
        // Carry the dying permanent's ID so resolution can look up cards exiled with it.
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sd.dyingCard(),
                sd.controllerId(),
                sd.dyingCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                dyingPermanent.getId()
        ));
        return true;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_DEATH)
    boolean handleDeathDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.SelfDeath sd = (TriggerContext.SelfDeath) ctx;
        // Graveyard-targeting death triggers (e.g. Ruin Rat: "exile target card from an opponent's
        // graveyard") also queue as a DeathTriggerTarget; processNextDeathTriggerTarget routes them
        // to a graveyard card choice made as the trigger goes on the stack.
        if (effect.targetSpec().category().includesPermanents() || effect.targetSpec().category().includesPlayers()
                || effect.targetSpec().category().isGraveyard()) {
            match.gameData().queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                    sd.dyingCard(), sd.controllerId(), new ArrayList<>(List.of(effect))
            ));
        } else {
            match.gameData().stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    sd.dyingCard(),
                    sd.controllerId(),
                    sd.dyingCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect))
            ));
        }
        return true;
    }

    // ── ON_ALLY_CREATURE_DIES (may effects only — non-may effects are batched by orchestrator) ──

    @CollectsTrigger(value = MayPayManaEffect.class, slot = EffectSlot.ON_ALLY_CREATURE_DIES)
    boolean handleAllyCreatureMayPay(TriggerMatchContext match,
            MayPayManaEffect mayPay, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        match.gameData().queueMayAbility(match.permanent().getCard(), cd.dyingCreatureControllerId(), mayPay, null);
        return true;
    }

    @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_ALLY_CREATURE_DIES)
    boolean handleAllyCreatureMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        // Colfenor's Urn: bind the exile-and-track effect to the specific dying creature card so
        // resolution knows which graveyard card to exile with this artifact.
        MayEffect resolvedMay = may;
        if (may.wrapped() instanceof ExileTriggeringCreatureAndTrackWithSourceEffect exile
                && exile.dyingCardId() == null && cd.dyingCard() != null) {
            resolvedMay = new MayEffect(
                    new ExileTriggeringCreatureAndTrackWithSourceEffect(cd.dyingCard().getId()),
                    may.prompt());
        }
        match.gameData().queueMayAbility(match.permanent().getCard(), cd.dyingCreatureControllerId(), resolvedMay, null, match.permanent().getId());
        return true;
    }

    // ── ON_EQUIPPED_CREATURE_DIES ──────────────────────────────────────

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_EQUIPPED_CREATURE_DIES)
    boolean handleEquippedCreatureDeathDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        GameData gameData = match.gameData();
        if (effect.targetSpec().category().includesPermanents() || effect.targetSpec().category().includesPlayers()) {
            gameData.queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(effect))
            ));
        } else {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    match.permanent().getCard(),
                    match.controllerId(),
                    match.permanent().getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect))
            ));
        }
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(match.permanent().getCard(),
                "'s ability triggers (equipped creature died)."));
        log.info("Game {} - {} triggers (equipped creature died)", gameData.id, match.permanent().getCard().getName());
        return true;
    }

    // ── ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD ──────────────────────

    @CollectsTrigger(value = ReturnSourceAuraToOpponentCreatureOnDeathEffect.class, slot = EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleReturnSourceAura(TriggerMatchContext match,
            ReturnSourceAuraToOpponentCreatureOnDeathEffect effect, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentDeath epd = (TriggerContext.EnchantedPermanentDeath) ctx;
        CardEffect effectForStack = epd.dyingPermanentControllerId() != null
                ? new ReturnSourceAuraToOpponentCreatureOnDeathEffect(epd.dyingPermanentControllerId())
                : effect;
        addEnchantedPermanentDeathEntry(match, effectForStack);
        return true;
    }

    @CollectsTrigger(value = ReturnSourceAuraToSharedTypeCreatureOnDeathEffect.class, slot = EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleReturnSourceAuraSharedType(TriggerMatchContext match,
            ReturnSourceAuraToSharedTypeCreatureOnDeathEffect effect, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentDeath epd = (TriggerContext.EnchantedPermanentDeath) ctx;
        CardEffect baked = epd.dyingCreatureCardId() != null
                ? new ReturnSourceAuraToSharedTypeCreatureOnDeathEffect(epd.dyingCreatureCardId())
                : effect;
        // "you may return this card ..." — gate the return behind a MayEffect for the aura's controller.
        MayEffect may = new MayEffect(baked,
                "return it to the battlefield attached to a creature that shares a creature type with the creature that died?");
        addEnchantedPermanentDeathEntry(match, may);
        return true;
    }

    @CollectsTrigger(value = ReturnEnchantedCreatureToOwnerHandOnDeathEffect.class, slot = EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleReturnEnchantedCreature(TriggerMatchContext match,
            ReturnEnchantedCreatureToOwnerHandOnDeathEffect effect, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentDeath epd = (TriggerContext.EnchantedPermanentDeath) ctx;
        CardEffect effectForStack = epd.dyingCreatureCardId() != null
                ? new ReturnEnchantedCreatureToOwnerHandOnDeathEffect(epd.dyingCreatureCardId())
                : effect;
        addEnchantedPermanentDeathEntry(match, effectForStack);
        return true;
    }

    @CollectsTrigger(value = ReturnEnchantedCreatureToBattlefieldUnderOwnersControlOnDeathEffect.class, slot = EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleReturnEnchantedCreatureToBattlefield(TriggerMatchContext match,
            ReturnEnchantedCreatureToBattlefieldUnderOwnersControlOnDeathEffect effect, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentDeath epd = (TriggerContext.EnchantedPermanentDeath) ctx;
        CardEffect effectForStack = epd.dyingCreatureCardId() != null
                ? new ReturnEnchantedCreatureToBattlefieldUnderOwnersControlOnDeathEffect(epd.dyingCreatureCardId())
                : effect;
        addEnchantedPermanentDeathEntry(match, effectForStack);
        return true;
    }

    @CollectsTrigger(value = EnchantedCreatureControllerLosesLifeEffect.class, slot = EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleEnchantedCreatureControllerLosesLife(TriggerMatchContext match,
            EnchantedCreatureControllerLosesLifeEffect effect, TriggerContext ctx) {
        // Banewasp Affliction: the dying creature's controller loses life equal to its toughness.
        // Snapshot the last-known toughness and controller at trigger time (the creature has already
        // left the battlefield); life loss can't be negative.
        TriggerContext.EnchantedPermanentDeath epd = (TriggerContext.EnchantedPermanentDeath) ctx;
        CardEffect baked = new EnchantedCreatureControllerLosesLifeEffect(
                Math.max(0, epd.dyingCreatureToughness()), epd.dyingPermanentControllerId());
        addEnchantedPermanentDeathEntry(match, baked);
        return true;
    }

    @CollectsTrigger(value = DealDamageToPlayersEffect.class, slot = EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleEnchantedCreatureDeathDamageController(TriggerMatchContext match,
            DealDamageToPlayersEffect effect, TriggerContext ctx) {
        // Creature Bond: the dying creature's controller is dealt damage equal to its toughness.
        // The creature has already left the battlefield, so snapshot its last-known toughness as the
        // fixed damage amount (>= 0) and bake its controller as the stack targetId — the
        // ENCHANTED_PERMANENT_CONTROLLER recipient reads that player from the stack entry.
        TriggerContext.EnchantedPermanentDeath epd = (TriggerContext.EnchantedPermanentDeath) ctx;
        DealDamageToPlayersEffect baked = new DealDamageToPlayersEffect(
                Math.max(0, epd.dyingCreatureToughness()), effect.recipient());
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(baked)),
                epd.dyingPermanentControllerId(),
                match.permanent().getId()
        ));
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                "'s ability triggers (enchanted permanent put into graveyard)."));
        log.info("Game {} - {} triggers (enchanted permanent put into graveyard)", match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)
    boolean handleEnchantedPermanentDeathDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        addEnchantedPermanentDeathEntry(match, effect);
        return true;
    }

    private void addEnchantedPermanentDeathEntry(TriggerMatchContext match, CardEffect effect) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect))
        ));
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                "'s ability triggers (enchanted permanent put into graveyard)."));
        log.info("Game {} - {} triggers (enchanted permanent put into graveyard)", match.gameData().id, match.permanent().getCard().getName());
    }

    // ── ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD ──────────────────────

    @CollectsTrigger(value = EnchantedPermanentLeavesConditionalEffect.class, slot = EffectSlot.ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD)
    boolean handleEnchantedPermanentLeavesConditional(TriggerMatchContext match,
            EnchantedPermanentLeavesConditionalEffect conditional, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentLeaves epl = (TriggerContext.EnchantedPermanentLeaves) ctx;
        if (conditional.permanentFilter() != null
                && !predicateEvaluationService.matchesCardPredicate(epl.leavingPermanent().getCard(), conditional.permanentFilter(), null)) {
            return false;
        }
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(conditional.resolvedEffects())
        ));
        logEnchantedPermanentLTB(match);
        return true;
    }

    @CollectsTrigger(value = EnchantedControllerSacrificesCreatureOnLeaveEffect.class, slot = EffectSlot.ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD)
    boolean handleEnchantedControllerSacrificesCreatureOnLeave(TriggerMatchContext match,
            EnchantedControllerSacrificesCreatureOnLeaveEffect effect, TriggerContext ctx) {
        TriggerContext.EnchantedPermanentLeaves epl = (TriggerContext.EnchantedPermanentLeaves) ctx;
        // Bake in the player who controlled the leaving creature (its controller sacrifices — not the
        // Aura controller, which differs when the Aura enchants an opponent's creature).
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(
                        new EnchantedControllerSacrificesCreatureOnLeaveEffect(epl.leavingControllerId())))
        ));
        logEnchantedPermanentLTB(match);
        return true;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_ENCHANTED_PERMANENT_LEAVES_BATTLEFIELD)
    boolean handleEnchantedPermanentLeavesDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect))
        ));
        logEnchantedPermanentLTB(match);
        return true;
    }

    private void logEnchantedPermanentLTB(TriggerMatchContext match) {
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                "'s ability triggers (enchanted permanent left the battlefield)."));
        log.info("Game {} - {} triggers (enchanted permanent left the battlefield)", match.gameData().id, match.permanent().getCard().getName());
    }

    // ── ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD ────────────

    @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleArtifactGraveyardMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        match.gameData().queueMayAbility(match.permanent().getCard(), match.controllerId(), may);
        logArtifactGraveyard(match);
        return true;
    }

    @CollectsTrigger(value = DealDamageToPlayersEffect.class, slot = EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleArtifactGraveyardDamageController(TriggerMatchContext match,
            DealDamageToPlayersEffect effect, TriggerContext ctx) {
        TriggerContext.ArtifactGraveyard ag = (TriggerContext.ArtifactGraveyard) ctx;
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                ag.artifactControllerId(),
                match.permanent().getId()
        ));
        logArtifactGraveyard(match);
        return true;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleArtifactGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        logArtifactGraveyard(match);
        return true;
    }

    private void logArtifactGraveyard(TriggerMatchContext match) {
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (artifact put into graveyard from battlefield)", match.gameData().id, match.permanent().getCard().getName());
    }

    // ── ON_ANY_LAND_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD ────────────────

    @CollectsTrigger(value = DealDamageToPlayersEffect.class, slot = EffectSlot.ON_ANY_LAND_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleLandGraveyardDamageController(TriggerMatchContext match,
            DealDamageToPlayersEffect effect, TriggerContext ctx) {
        TriggerContext.AnyLandGraveyard lg = (TriggerContext.AnyLandGraveyard) ctx;
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                lg.landControllerId(),
                match.permanent().getId()
        ));
        logLandGraveyard(match);
        return true;
    }

    private void logLandGraveyard(TriggerMatchContext match) {
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (land put into graveyard from battlefield)", match.gameData().id, match.permanent().getCard().getName());
    }

    // ── ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD ───────

    @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleOpponentArtifactGraveyardMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        match.gameData().queueMayAbility(match.permanent().getCard(), match.controllerId(), may);
        logOpponentArtifactGraveyard(match);
        return true;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_ARTIFACT_PUT_INTO_OPPONENT_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleOpponentArtifactGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        logOpponentArtifactGraveyard(match);
        return true;
    }

    private void logOpponentArtifactGraveyard(TriggerMatchContext match) {
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (opponent artifact put into graveyard from battlefield)", match.gameData().id, match.permanent().getCard().getName());
    }

    // ── ON_BLACK_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE ────────

    @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_BLACK_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE)
    boolean handleBlackCardOpponentGraveyardMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        match.gameData().queueMayAbility(match.permanent().getCard(), match.controllerId(), may);
        logBlackCardOpponentGraveyard(match);
        return true;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_BLACK_CARD_PUT_INTO_OPPONENT_GRAVEYARD_FROM_ANYWHERE)
    boolean handleBlackCardOpponentGraveyardDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        logBlackCardOpponentGraveyard(match);
        return true;
    }

    private void logBlackCardOpponentGraveyard(TriggerMatchContext match) {
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (black card put into opponent's graveyard from anywhere)", match.gameData().id, match.permanent().getCard().getName());
    }

    // ── ON_ALLY_LAND_PUT_INTO_GRAVEYARD_BY_OPPONENT ────────────────────

    @CollectsTrigger(value = ReturnTriggeringLandFromGraveyardToBattlefieldEffect.class,
            slot = EffectSlot.ON_ALLY_LAND_PUT_INTO_GRAVEYARD_BY_OPPONENT)
    boolean handleLandGraveyardReturn(TriggerMatchContext match,
            ReturnTriggeringLandFromGraveyardToBattlefieldEffect effect, TriggerContext ctx) {
        TriggerContext.LandPutIntoGraveyard lpg = (TriggerContext.LandPutIntoGraveyard) ctx;
        Card landCard = lpg.landCard();
        ReturnTriggeringLandFromGraveyardToBattlefieldEffect concrete =
                new ReturnTriggeringLandFromGraveyardToBattlefieldEffect(landCard.getId());
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(concrete)),
                null,
                match.permanent().getId()
        ));

        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.cardTextCard(match.permanent().getCard(),
                "'s ability triggers (", landCard, " was put into a graveyard from the battlefield by an opponent)."));
        log.info("Game {} - {} triggers (land {} put into graveyard by opponent)",
                match.gameData().id, match.permanent().getCard().getName(), landCard.getName());
        return true;
    }

    // ── ON_OPPONENT_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD ──────

    @CollectsTrigger(value = StealDyingOpponentPermanentUnlessPaysLifeEffect.class,
            slot = EffectSlot.ON_OPPONENT_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleOpponentPermanentGraveyardSteal(TriggerMatchContext match,
            StealDyingOpponentPermanentUnlessPaysLifeEffect effect, TriggerContext ctx) {
        TriggerContext.OpponentPermanentGraveyard opg = (TriggerContext.OpponentPermanentGraveyard) ctx;
        // Bake the dying card id and the opponent who may pay the life. The "unless pays" branch is a
        // resolution-time choice made by that opponent, so this stacks as a plain triggered ability.
        StealDyingOpponentPermanentUnlessPaysLifeEffect baked =
                new StealDyingOpponentPermanentUnlessPaysLifeEffect(
                        effect.lifeCost(), opg.dyingCard().getId(), opg.dyingControllerId());
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(baked))
        ));
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (opponent permanent put into graveyard)",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    // ── ON_ANY_CREATURE_DIES ───────────────────────────────────────────

    @CollectsTrigger(value = PutCountersOnSourceEffect.class, slot = EffectSlot.ON_ANY_CREATURE_DIES)
    boolean handleAnyCreatureDeathPutCounters(TriggerMatchContext match,
            PutCountersOnSourceEffect effect, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        logAnyCreatureDeath(match);
        return true;
    }

    @CollectsTrigger(value = PutCountersOnSourceEqualToDyingPowerEffect.class, slot = EffectSlot.ON_ANY_CREATURE_DIES)
    boolean handleAnyCreatureDeathPutCountersEqualToPower(TriggerMatchContext match,
            PutCountersOnSourceEqualToDyingPowerEffect effect, TriggerContext ctx) {
        // Kresh the Bloodbraided: "you may put X +1/+1 counters on this creature, where X is that
        // creature's power." The dying creature's last-known power is snapshotted into the context;
        // bake it into a concrete PutCountersOnSourceEffect that resolves onto this permanent.
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        int power = Math.max(0, cd.dyingCreaturePower());
        var counters = new PutCountersOnSourceEffect(effect.powerModifier(), effect.toughnessModifier(), power);
        if (effect.optional()) {
            var may = new MayEffect(counters, "Put " + power + " +1/+1 counter(s) on this creature?");
            match.gameData().queueMayAbility(match.permanent().getCard(), match.controllerId(), may,
                    null, match.permanent().getId());
        } else {
            match.gameData().stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    match.permanent().getCard(),
                    match.controllerId(),
                    match.permanent().getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(counters)),
                    null,
                    match.permanent().getId()));
        }
        logAnyCreatureDeath(match);
        return true;
    }

    @CollectsTrigger(value = UntapEquippedCreatureEffect.class, slot = EffectSlot.ON_ANY_CREATURE_DIES)
    boolean handleAnyCreatureDeathUntapEquipped(TriggerMatchContext match,
            UntapEquippedCreatureEffect effect, TriggerContext ctx) {
        // Equipment-granted untap trigger needs its source permanent id to locate the equipment.
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        logAnyCreatureDeath(match);
        return true;
    }

    @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_ANY_CREATURE_DIES)
    boolean handleAnyCreatureDeathMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        // CR 603.3d: a targeted "may" ability (e.g. Vicious Shadows' "you may have this deal damage
        // to target player") needs its target chosen when the trigger is put on the stack.
        if (may.targetSpec().category().includesPermanents() || may.targetSpec().category().includesPlayers()) {
            match.gameData().queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(may))
            ));
        } else {
            // Pass the source permanent id so wrapped source-referencing effects (e.g.
            // PutCountersOnSourceEffect for Scavenger Drake's optional +1/+1 counter) can resolve.
            match.gameData().queueMayAbility(match.permanent().getCard(), match.controllerId(), may,
                    null, match.permanent().getId());
        }
        logAnyCreatureDeath(match);
        return true;
    }

    @CollectsTrigger(value = DyingCreatureControllerMayDrawCardEffect.class, slot = EffectSlot.ON_ANY_CREATURE_DIES)
    boolean handleAnyCreatureDeathDyingControllerDraws(TriggerMatchContext match,
            DyingCreatureControllerMayDrawCardEffect effect, TriggerContext ctx) {
        // Fecundity: the dying creature's controller (not the source's controller) may draw.
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        match.gameData().queueMayAbility(match.permanent().getCard(), cd.dyingCreatureControllerId(),
                new MayEffect(new DrawCardEffect(), "Draw a card?"));
        logAnyCreatureDeath(match);
        return true;
    }

    @CollectsTrigger(value = BecomeCopyOfDyingCreatureEffect.class, slot = EffectSlot.ON_ANY_CREATURE_DIES)
    boolean handleAnyCreatureDeathBecomeCopy(TriggerMatchContext match,
            BecomeCopyOfDyingCreatureEffect effect, TriggerContext ctx) {
        // Cemetery Puca: "you may pay {1}. If you do, this creature becomes a copy of that creature,
        // except it has this ability." Bake the dying creature's card id so resolution copies its
        // last-known information, and gate the copy behind the mana payment.
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        if (cd.dyingCard() == null) {
            return false;
        }
        MayPayManaEffect rawMayPay = (MayPayManaEffect) match.rawEffect();
        var copyEffect = new BecomeCopyOfDyingCreatureEffect(cd.dyingCard().getId());
        match.gameData().pendingMayAbilities.add(new PendingMayAbility(
                match.permanent().getCard(),
                match.controllerId(),
                List.of(copyEffect),
                match.permanent().getCard().getName() + " — Pay " + rawMayPay.manaCost()
                        + " to become a copy of " + cd.dyingCard().getName() + "?",
                null,
                rawMayPay.manaCost()
        ));
        logAnyCreatureDeath(match);
        return true;
    }

    @CollectsTrigger(value = DyingCreatureControllerDiscardsCardEffect.class, slot = EffectSlot.ON_ANY_CREATURE_DIES)
    boolean handleAnyCreatureDeathDyingControllerDiscards(TriggerMatchContext match,
            DyingCreatureControllerDiscardsCardEffect effect, TriggerContext ctx) {
        // Bereavement: the dying creature's controller (not the source's controller) discards a card.
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                cd.dyingCreatureControllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new DiscardEffect(1, DiscardRecipient.CONTROLLER)))
        ));
        logAnyCreatureDeath(match);
        return true;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_ANY_CREATURE_DIES)
    boolean handleAnyCreatureDeathDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        GameData gameData = match.gameData();
        if (effect.targetSpec().category().includesPermanents() || effect.targetSpec().category().includesPlayers()) {
            gameData.queueInteraction(new PermanentChoiceContext.DeathTriggerTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(List.of(effect))
            ));
        } else {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    match.permanent().getCard(),
                    match.controllerId(),
                    match.permanent().getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect))
            ));
        }
        logAnyCreatureDeath(match);
        return true;
    }

    @CollectsTrigger(value = DestroyEnchantedPermanentEffect.class, slot = EffectSlot.ON_ANY_CREATURE_DIES)
    boolean handleAnyCreatureDeathDestroyEnchanted(TriggerMatchContext match,
            DestroyEnchantedPermanentEffect effect, TriggerContext ctx) {
        // Yoke of the Damned: needs its source Aura's permanent id to re-derive the enchanted creature.
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        logAnyCreatureDeath(match);
        return true;
    }

    private void logAnyCreatureDeath(TriggerMatchContext match) {
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (any creature died)", match.gameData().id, match.permanent().getCard().getName());
    }

    // ── ON_ALLY_NONTOKEN_CREATURE_DIES ─────────────────────────────────

    @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES)
    boolean handleAllyNontokenMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        match.gameData().queueMayAbility(match.permanent().getCard(), cd.dyingCreatureControllerId(), may);
        logAllyNontokenCreatureDeath(match);
        return true;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES)
    boolean handleAllyNontokenDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                cd.dyingCreatureControllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        ));
        logAllyNontokenCreatureDeath(match);
        return true;
    }

    private void logAllyNontokenCreatureDeath(TriggerMatchContext match) {
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (ally nontoken creature died)", match.gameData().id, match.permanent().getCard().getName());
    }

    // ── ON_ANY_NONTOKEN_CREATURE_DIES ──────────────────────────────────

    @CollectsTrigger(value = ImprintDyingCreatureEffect.class, slot = EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES)
    boolean handleImprintDyingCreature(TriggerMatchContext match,
            ImprintDyingCreatureEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        ImprintDyingCreatureEffect imprintEffect = new ImprintDyingCreatureEffect(cd.dyingCard().getId());
        MayEffect rawMay = (MayEffect) match.rawEffect();
        match.gameData().pendingMayAbilities.add(new PendingMayAbility(
                match.permanent().getCard(),
                match.controllerId(),
                List.of(imprintEffect),
                match.permanent().getCard().getName() + " — " + rawMay.prompt()
        ));
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                "'s imprint ability triggers."));
        log.info("Game {} - {} imprint triggers (nontoken creature died)", match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsTrigger(value = ReturnDyingCreatureToBattlefieldAndAttachSourceEffect.class, slot = EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES)
    boolean handleReturnDyingCreatureMayPay(TriggerMatchContext match,
            ReturnDyingCreatureToBattlefieldAndAttachSourceEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        GameData gameData = match.gameData();
        List<Card> playerGraveyard = gameData.playerGraveyards.get(match.controllerId());
        if (playerGraveyard == null || playerGraveyard.stream().noneMatch(c -> c.getId().equals(cd.dyingCard().getId()))) {
            return false;
        }
        MayPayManaEffect rawMayPay = (MayPayManaEffect) match.rawEffect();
        var returnEffect = new ReturnDyingCreatureToBattlefieldAndAttachSourceEffect(cd.dyingCard().getId());
        gameData.pendingMayAbilities.add(new PendingMayAbility(
                match.permanent().getCard(),
                match.controllerId(),
                List.of(returnEffect),
                match.permanent().getCard().getName() + " — Pay " + rawMayPay.manaCost() + " to return " + cd.dyingCard().getName() + " to the battlefield?",
                cd.dyingCard().getId(),
                rawMayPay.manaCost()
        ));
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(match.permanent().getCard(),
                "'s ability triggers (", cd.dyingCard(), " died)."));
        log.info("Game {} - {} return trigger fires (nontoken creature {} died)", gameData.id, match.permanent().getCard().getName(), cd.dyingCard().getName());
        return true;
    }

    // ── ON_OPPONENT_CREATURE_DIES ──────────────────────────────────────

    @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_OPPONENT_CREATURE_DIES)
    boolean handleOpponentCreatureDeathMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        match.gameData().queueMayAbility(match.permanent().getCard(), match.controllerId(), may);
        logOpponentCreatureDeath(match);
        return true;
    }

    @CollectsTrigger(value = ReturnDyingOpponentCreatureUnderYourControlEffect.class, slot = EffectSlot.ON_OPPONENT_CREATURE_DIES)
    boolean handleOpponentCreatureReturnUnderControl(TriggerMatchContext match,
            ReturnDyingOpponentCreatureUnderYourControlEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        GameData gameData = match.gameData();
        Card dyingCard = cd.dyingCard();
        // The dying card must still be in its owner's graveyard for the return to have a target.
        List<Card> graveyard = dyingCard == null ? null : gameData.playerGraveyards.get(cd.dyingCreatureControllerId());
        if (graveyard == null || graveyard.stream().noneMatch(c -> c.getId().equals(dyingCard.getId()))) {
            return false;
        }
        // "You may return that card ..." — a resolution-time optional. Bake the dying card id into
        // the effect itself (not the stack entry's targetId, which the engine would validate as an
        // on-battlefield permanent target and fizzle).
        MayEffect may = new MayEffect(
                new ReturnDyingOpponentCreatureUnderYourControlEffect(dyingCard.getId()),
                "return " + dyingCard.getName() + " to the battlefield under your control?");
        gameData.queueMayAbility(match.permanent().getCard(), match.controllerId(), may);
        logOpponentCreatureDeath(match);
        return true;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_OPPONENT_CREATURE_DIES)
    boolean handleOpponentCreatureDeathDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDeath cd = (TriggerContext.CreatureDeath) ctx;
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                cd.dyingCreatureControllerId(),
                match.permanent().getId()
        ));
        logOpponentCreatureDeath(match);
        return true;
    }

    private void logOpponentCreatureDeath(TriggerMatchContext match) {
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers (opponent creature died)", match.gameData().id, match.permanent().getCard().getName());
    }

    // ── ON_SELF_LEAVES_BATTLEFIELD ─────────────────────────────────────

    @CollectsTrigger(value = ControllerLosesGameOnLeavesEffect.class, slot = EffectSlot.ON_SELF_LEAVES_BATTLEFIELD)
    boolean handleControllerLosesGameOnLeaves(TriggerMatchContext match,
            ControllerLosesGameOnLeavesEffect effect, TriggerContext ctx) {
        TriggerContext.SelfLeaves sl = (TriggerContext.SelfLeaves) ctx;
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                sl.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new TargetPlayerLosesGameEffect(sl.controllerId())))
        ));
        logSelfLeaves(match);
        return true;
    }

    @CollectsTrigger(value = RemoveLinkedPermanentEffect.class, slot = EffectSlot.ON_SELF_LEAVES_BATTLEFIELD)
    boolean handleRemoveLinkedPermanent(TriggerMatchContext match,
            RemoveLinkedPermanentEffect effect, TriggerContext ctx) {
        TriggerContext.SelfLeaves sl = (TriggerContext.SelfLeaves) ctx;
        // The partner id is stored on the leaving permanent; if the bond was never forged (no token was
        // created) or was already broken, the trigger simply does nothing (CR 603.4-style no-op).
        UUID linkedId = match.permanent().getChosenPermanentId();
        if (linkedId == null) {
            return false;
        }
        // Bake the linked id into the effect (not the stack entry's targetId, which resolution would
        // validate against the source card's spell target filter and fizzle — Dance of Many's ETB filter
        // is "nontoken creature", which the token itself fails).
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                sl.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new RemoveLinkedPermanentEffect(effect.mode(), linkedId)))
        ));
        logSelfLeaves(match);
        return true;
    }

    @CollectsTrigger(value = SacrificeEnchantedCreatureOnLeaveEffect.class, slot = EffectSlot.ON_SELF_LEAVES_BATTLEFIELD)
    boolean handleSacrificeEnchantedCreatureOnLeave(TriggerMatchContext match,
            SacrificeEnchantedCreatureOnLeaveEffect effect, TriggerContext ctx) {
        TriggerContext.SelfLeaves sl = (TriggerContext.SelfLeaves) ctx;
        // Capture the enchanted creature's permanent id before the Aura is gone; if the Aura wasn't
        // attached (never resolved onto a creature) the trigger simply does nothing (CR 603.4 no-op).
        UUID enchantedId = match.permanent().getAttachedTo();
        if (enchantedId == null) {
            return false;
        }
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                sl.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new SacrificeEnchantedCreatureOnLeaveEffect(enchantedId)))
        ));
        logSelfLeaves(match);
        return true;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_SELF_LEAVES_BATTLEFIELD)
    boolean handleSelfLeavesDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.SelfLeaves sl = (TriggerContext.SelfLeaves) ctx;
        // Graveyard-targeting self-leaves triggers (e.g. Offalsnout) also queue as a
        // SelfLeavesTriggerTarget; the queue processor routes them to a graveyard card choice.
        if (effect.targetSpec().category().includesPermanents() || effect.targetSpec().category().includesPlayers()
                || effect.targetSpec().category().isGraveyard()) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SelfLeavesTriggerTarget(
                    match.permanent().getCard(), sl.controllerId(), new ArrayList<>(List.of(effect))
            ));
        } else {
            match.gameData().stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    match.permanent().getCard(),
                    sl.controllerId(),
                    match.permanent().getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect))
            ));
        }
        logSelfLeaves(match);
        return true;
    }

    private void logSelfLeaves(TriggerMatchContext match) {
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                "'s ability triggers (left the battlefield)."));
        log.info("Game {} - {} triggers (left the battlefield)", match.gameData().id, match.permanent().getCard().getName());
    }

    // ── ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD ──

    @CollectsTrigger(value = RegisterDelayedReturnCardFromGraveyardToHandEffect.class, slot = EffectSlot.ON_ALLY_AURA_OR_EQUIPMENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD)
    boolean handleRegisterDelayedReturn(TriggerMatchContext match,
            RegisterDelayedReturnCardFromGraveyardToHandEffect effect, TriggerContext ctx) {
        TriggerContext.AllyAuraOrEquipmentGraveyard aaeg = (TriggerContext.AllyAuraOrEquipmentGraveyard) ctx;
        Card dyingCard = aaeg.dyingCard();
        RegisterDelayedReturnCardFromGraveyardToHandEffect delayedEffect =
                new RegisterDelayedReturnCardFromGraveyardToHandEffect(dyingCard.getId());
        MayEffect may = new MayEffect(delayedEffect,
                "Return " + dyingCard.getName() + " to its owner's hand at the beginning of the next end step?");
        match.gameData().queueMayAbility(match.permanent().getCard(), aaeg.controllerId(), may);

        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.cardTextCard(match.permanent().getCard(),
                "'s ability triggers (", dyingCard, " was put into a graveyard from the battlefield)."));
        log.info("Game {} - {} triggers (ally Aura/Equipment {} put into graveyard from battlefield)",
                match.gameData().id, match.permanent().getCard().getName(), dyingCard.getName());
        return true;
    }
}
