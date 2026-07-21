package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CasterLosesLifeOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.CastFromGraveyardTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayTapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherSubtypePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEqualToSpellManaValueToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.effect.KickedSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.KnowledgePoolCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.KnowledgePoolExileAndCastEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnLandControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureToBattlefieldOrMayBottomEffect;
import com.github.laxika.magicalvibes.model.effect.ChosenSubtypeSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfByCastSpellManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastLifeDrainEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.condition.SpellManaSpentAtLeast;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.effect.SunbirdsInvocationRevealAndCastEffect;
import com.github.laxika.magicalvibes.model.effect.SunbirdsInvocationTriggerEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.github.laxika.magicalvibes.model.GameLog;
/**
 * Trigger collectors for spell-cast events (ON_ANY_PLAYER_CASTS_SPELL,
 * ON_CONTROLLER_CASTS_SPELL, ON_OPPONENT_CASTS_SPELL).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpellCastTriggerCollectorService {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final TargetLegalityService targetLegalityService;
    private final AmountEvaluationService amountEvaluationService;

    // ── ON_ANY_PLAYER_CASTS_SPELL ──────────────────────────────────────

    @CollectsTrigger(value = SpellCastTriggerEffect.class, slot = EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)
    private boolean handleAnyPlayerSpellCastTrigger(TriggerMatchContext match, SpellCastTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        return handleGenericSpellCastTrigger(match, trigger, sc.spellCard(), sc.castingPlayerId());
    }

    @CollectsTrigger(value = PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect.class, slot = EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)
    private boolean handleAnyPlayerColorCounter(TriggerMatchContext match,
            PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (sc.spellCard().getColor() == null) return false;
        if (!trigger.triggerColors().contains(sc.spellCard().getColor())) return false;
        if (trigger.onlyOwnSpells()) return false;
        return addColorCounterTrigger(match, trigger);
    }

    @CollectsTrigger(value = KnowledgePoolCastTriggerEffect.class, slot = EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)
    private boolean handleKnowledgePoolCast(TriggerMatchContext match,
            KnowledgePoolCastTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (!sc.castFromHand()) return false;

        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new KnowledgePoolExileAndCastEffect(
                        sc.spellCard().getId(), match.permanent().getId(), sc.castingPlayerId())))
        ));
        return true;
    }

    @CollectsTrigger(value = CopySpellForEachOtherSubtypePermanentEffect.class, slot = EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)
    private boolean handleCopySpellForSubtype(TriggerMatchContext match,
            CopySpellForEachOtherSubtypePermanentEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (trigger.spellSnapshot() != null) return false;

        Card spellCard = sc.spellCard();
        if (!spellCard.hasType(CardType.INSTANT) && !spellCard.hasType(CardType.SORCERY)) return false;

        // Find the spell on the stack
        StackEntry spellEntry = null;
        for (StackEntry se : match.gameData().stack) {
            if (se.getCard().getId().equals(spellCard.getId())) {
                spellEntry = se;
                break;
            }
        }
        if (spellEntry == null) return false;

        // Determine the single unique target
        UUID singleTargetId = null;
        if (spellEntry.getTargetId() != null
                && spellEntry.getTargetZone() == null
                && spellEntry.getTargetIds().isEmpty()) {
            singleTargetId = spellEntry.getTargetId();
        } else if (spellEntry.getTargetId() == null
                && !spellEntry.getTargetIds().isEmpty()
                && spellEntry.getTargetIds().stream().distinct().count() == 1) {
            singleTargetId = spellEntry.getTargetIds().getFirst();
        }
        if (singleTargetId == null) return false;
        if (match.gameData().playerIds.contains(singleTargetId)) return false;

        Permanent targetPerm = gameQueryService.findPermanentById(match.gameData(), singleTargetId);
        if (targetPerm == null) return false;
        if (!targetPerm.getCard().getSubtypes().contains(trigger.subtype())) return false;

        StackEntry snapshot = new StackEntry(spellEntry);
        CopySpellForEachOtherSubtypePermanentEffect resolutionEffect =
                new CopySpellForEachOtherSubtypePermanentEffect(
                        trigger.subtype(), snapshot, sc.castingPlayerId(), singleTargetId);

        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(resolutionEffect))
        ));
        return true;
    }

    @CollectsTrigger(value = CopySpellForEachOtherPlayerEffect.class, slot = EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)
    private boolean handleCopySpellForEachOtherPlayer(TriggerMatchContext match,
            CopySpellForEachOtherPlayerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (trigger.spellSnapshot() != null) return false;

        // Find the spell on the stack
        StackEntry spellEntry = null;
        for (StackEntry se : match.gameData().stack) {
            if (se.getCard().getId().equals(sc.spellCard().getId())) {
                spellEntry = se;
                break;
            }
        }
        if (spellEntry == null) return false;

        // The spell filter fully expresses what triggers the copy — instant/sorcery type, plus
        // (for Curse of Echoes) controlled-by-the-enchanted-player. Evaluated against the cast
        // spell's stack entry, with the source aura's attachedTo as the enchanted-player context.
        if (trigger.spellFilter() != null
                && !predicateEvaluationService.matchesStackEntryPredicate(spellEntry, trigger.spellFilter(),
                        match.permanent().getAttachedTo())) {
            return false;
        }

        StackEntry snapshot = new StackEntry(spellEntry);
        CopySpellForEachOtherPlayerEffect resolutionEffect =
                new CopySpellForEachOtherPlayerEffect(snapshot, sc.castingPlayerId(), trigger.optional());

        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(resolutionEffect))
        ));
        return true;
    }

    @CollectsTrigger(value = CasterLosesLifeOnSpellCastEffect.class, slot = EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)
    private boolean handleCasterLosesLifeOnSpellCast(TriggerMatchContext match,
            CasterLosesLifeOnSpellCastEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (trigger.spellFilter() != null
                && !predicateEvaluationService.matchesCardPredicate(sc.spellCard(), trigger.spellFilter(), null,
                        match.gameData(), sc.castingPlayerId())) {
            return false;
        }
        // "that player" = the caster; preset the target so the loss falls on them, not a choice.
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new LoseLifeEffect(trigger.amount(), LoseLifeRecipient.TARGET_PLAYER)))
        );
        entry.setTargetId(sc.castingPlayerId());
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsTrigger(value = ReturnLandControlledByPlayerToHandEffect.class, slot = EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)
    private boolean handleReturnLandOnSpellCast(TriggerMatchContext match,
            ReturnLandControlledByPlayerToHandEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        // "that player returns a land they control" — carry the casting player on targetId so the
        // resolution handler prompts them (not the enchantment's controller).
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger)));
        // Mana Breach doesn't target — targetId only carries the acting (casting) player.
        entry.setTargetId(sc.castingPlayerId());
        entry.setNonTargeting(true);
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsTrigger(value = DiscardEffect.class, slot = EffectSlot.ON_ANY_PLAYER_CASTS_SPELL)
    private boolean handleDiscardOnSpellCast(TriggerMatchContext match,
            DiscardEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        // "that player discards a card" — carry the casting player on targetId so the
        // TARGET_PLAYER discard lands on them (not the enchantment's controller). Oppression.
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger)));
        // Oppression doesn't target — targetId only carries the acting (casting) player.
        entry.setTargetId(sc.castingPlayerId());
        entry.setNonTargeting(true);
        match.gameData().stack.add(entry);
        return true;
    }

    // ── ON_CONTROLLER_CASTS_SPELL ──────────────────────────────────────

    @CollectsTrigger(value = PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect.class, slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleControllerColorCounter(TriggerMatchContext match,
            PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (sc.spellCard().getColor() == null) return false;
        if (!trigger.triggerColors().contains(sc.spellCard().getColor())) return false;
        return addColorCounterTrigger(match, trigger);
    }

    @CollectsTrigger(value = SpellCastTriggerEffect.class, slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleControllerSpellCastTrigger(TriggerMatchContext match, SpellCastTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        return handleGenericSpellCastTrigger(match, trigger, sc.spellCard(), sc.castingPlayerId());
    }

    @CollectsTrigger(value = BoostEquippedCreatureUntilEndOfTurnEffect.class, slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleBoostEquippedOnSpellCast(TriggerMatchContext match,
            BoostEquippedCreatureUntilEndOfTurnEffect trigger, TriggerContext ctx) {
        // "Whenever you cast a spell, equipped creature gets +X/+Y until end of turn" (Leering Emblem).
        // Carry the source permanent id so the handler can find the equipment and its equipped creature
        // (the effect fizzles at resolution if the Equipment is no longer attached).
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                null,
                match.permanent().getId()
        ));
        log.info("Game {} - {} spell-cast equipped-boost trigger queued",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsTrigger(value = CopyControllerCastSpellOnSpellCastEffect.class, slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleCopyControllerCastSpellOnSpellCast(TriggerMatchContext match,
            CopyControllerCastSpellOnSpellCastEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;

        if (!predicateEvaluationService.matchesCardPredicate(sc.spellCard(), trigger.spellFilter(), null,
                match.gameData(), sc.castingPlayerId())) {
            return false;
        }

        StackEntry spellEntry = null;
        for (StackEntry se : match.gameData().stack) {
            if (se.getCard().getId().equals(sc.spellCard().getId())) {
                spellEntry = se;
                break;
            }
        }
        if (spellEntry == null) return false;

        StackEntry snapshot = new StackEntry(spellEntry);
        CopyControllerCastSpellEffect copyEffect =
                new CopyControllerCastSpellEffect(snapshot, sc.castingPlayerId());

        CardEffect resolutionEffect;
        if (trigger.tapCost() != null) {
            resolutionEffect = new MayPayTapPermanentsEffect(
                    trigger.tapCost(),
                    copyEffect,
                    "Tap " + trigger.tapCost().count() + " untapped creatures you control to copy "
                            + sc.spellCard().getName() + "?");
        } else if (trigger.manaCost() != null) {
            resolutionEffect = new MayPayManaEffect(
                    trigger.manaCost(),
                    copyEffect,
                    "Pay " + trigger.manaCost() + " to copy " + sc.spellCard().getName() + "?");
        } else {
            resolutionEffect = copyEffect;
        }

        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(resolutionEffect)),
                null,
                match.permanent().getId()
        ));
        return true;
    }

    @CollectsTrigger(value = ChosenSubtypeSpellCastTriggerEffect.class, slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleChosenSubtypeSpellCastTrigger(TriggerMatchContext match,
            ChosenSubtypeSpellCastTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        CardSubtype chosenSubtype = match.permanent().getChosenSubtype();
        if (chosenSubtype == null) return false;

        // Must be a spell of the chosen type (optionally restricted to creature spells)
        CardPredicate subtypeFilter = trigger.creatureSpellOnly()
                ? new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardSubtypePredicate(chosenSubtype)))
                : new CardSubtypePredicate(chosenSubtype);
        if (!predicateEvaluationService.matchesCardPredicate(sc.spellCard(),
                subtypeFilter, null, match.gameData(), sc.castingPlayerId())) return false;

        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(trigger.resolvedEffects()),
                null,
                match.permanent().getId()
        ));

        log.info("Game {} - {} chosen-subtype spell-cast trigger queued",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsTrigger(value = NthSpellCastTriggerEffect.class, slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleNthSpellCastTrigger(TriggerMatchContext match, NthSpellCastTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        int spellsCast = match.gameData().getSpellsCastThisTurnCount(sc.castingPlayerId());
        if (spellsCast != trigger.spellNumber()) return false;

        List<CardEffect> resolved = new ArrayList<>(trigger.resolvedEffects());
        boolean selfTarget = resolved.stream().anyMatch(e -> e.targetSpec().selfTargeting());

        if (match.rawEffect() instanceof MayEffect may) {
            match.gameData().pendingMayAbilities.add(new PendingMayAbility(
                    match.permanent().getCard(),
                    match.controllerId(),
                    resolved,
                    match.permanent().getCard().getName() + " — " + may.prompt(),
                    null,
                    null,
                    match.permanent().getId()));
        } else {
            StackEntry entry = selfTarget
                    ? new StackEntry(StackEntryType.TRIGGERED_ABILITY, match.permanent().getCard(), match.controllerId(),
                        match.permanent().getCard().getName() + "'s ability", resolved, null, match.permanent().getId())
                    : new StackEntry(StackEntryType.TRIGGERED_ABILITY, match.permanent().getCard(), match.controllerId(),
                        match.permanent().getCard().getName() + "'s ability", resolved);
            match.gameData().stack.add(entry);
        }

        log.info("Game {} - {} Nth-spell-cast trigger fired (spell #{})",
                match.gameData().id, match.permanent().getCard().getName(), trigger.spellNumber());
        return true;
    }

    @CollectsTrigger(value = KickedSpellCastTriggerEffect.class, slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleKickedSpellCastTrigger(TriggerMatchContext match,
            KickedSpellCastTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;

        // Check if the spell on the stack was kicked
        boolean isKicked = false;
        for (StackEntry se : match.gameData().stack) {
            if (se.getCard().getId().equals(sc.spellCard().getId())) {
                isKicked = se.isKicked();
                break;
            }
        }
        if (!isKicked) return false;

        List<CardEffect> resolved = new ArrayList<>(trigger.resolvedEffects());

        // The trigger's source permanent is always carried on the entry — source-relative
        // effects (put counters on source, damage equal to counters on source) need it.
        StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, match.permanent().getCard(), match.controllerId(),
                match.permanent().getCard().getName() + "'s ability", resolved, null, match.permanent().getId());
        match.gameData().stack.add(entry);

        log.info("Game {} - {} kicked-spell-cast trigger queued",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsTrigger(value = CastFromGraveyardTriggerEffect.class, slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleCastFromGraveyardTrigger(TriggerMatchContext match,
            CastFromGraveyardTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (sc.castFromHand()) return false;

        boolean needsAnyTarget = trigger.resolvedEffects().stream()
                .anyMatch(e -> e.targetSpec().category().includesPlayers() || e.targetSpec().category().includesPermanents());

        if (needsAnyTarget) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(trigger.resolvedEffects())
            ));
            gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                    "'s triggered ability triggers — choose a target."));
        } else {
            match.gameData().stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    match.permanent().getCard(),
                    match.controllerId(),
                    match.permanent().getCard().getName() + "'s ability",
                    new ArrayList<>(trigger.resolvedEffects())
            ));
        }
        log.info("Game {} - {} cast-from-graveyard trigger queued",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsTrigger(value = SunbirdsInvocationTriggerEffect.class, slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleSunbirdsInvocationCast(TriggerMatchContext match,
            SunbirdsInvocationTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (!sc.castFromHand()) return false;

        int manaValue = sc.spellCard().getManaValue();

        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new SunbirdsInvocationRevealAndCastEffect(manaValue)))
        ));

        log.info("Game {} - Sunbird's Invocation trigger queued (mana value {})",
                match.gameData().id, manaValue);
        return true;
    }

    @CollectsTrigger(value = DealDamageEqualToSpellManaValueToAnyTargetEffect.class, slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleManaValueDamage(TriggerMatchContext match,
            DealDamageEqualToSpellManaValueToAnyTargetEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (!predicateEvaluationService.matchesCardPredicate(sc.spellCard(), trigger.spellFilter(), null,
                match.gameData(), sc.castingPlayerId())) return false;

        int manaValue = sc.spellCard().getManaValue();
        List<CardEffect> resolvedEffects = List.of(new DealDamageToAnyTargetEffect(manaValue));
        match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                match.permanent().getCard(), match.controllerId(), new ArrayList<>(resolvedEffects)
        ));
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                "'s triggered ability triggers — choose a target for " + manaValue + " damage."));
        log.info("Game {} - {} spell-cast mana-value trigger queued ({} damage)",
                match.gameData().id, match.permanent().getCard().getName(), manaValue);
        return true;
    }

    @CollectsTrigger(value = BoostSelfByCastSpellManaValueEffect.class, slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleManaValueSelfBoost(TriggerMatchContext match,
            BoostSelfByCastSpellManaValueEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (!predicateEvaluationService.matchesCardPredicate(sc.spellCard(), trigger.spellFilter(), null,
                match.gameData(), sc.castingPlayerId())) return false;

        int manaValue = sc.spellCard().getManaValue();
        List<CardEffect> resolved = new ArrayList<>(List.of(new BoostSelfEffect(manaValue, manaValue)));
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                resolved,
                null,
                match.permanent().getId()));
        log.info("Game {} - {} spell-cast mana-value self-boost trigger queued (+{}/+{})",
                match.gameData().id, match.permanent().getCard().getName(), manaValue, manaValue);
        return true;
    }

    @CollectsTrigger(value = GivePoisonCountersEffect.class, slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handlePoisonOnSpellCast(TriggerMatchContext match,
            GivePoisonCountersEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (trigger.spellFilter() == null) return false;
        if (!predicateEvaluationService.matchesCardPredicate(sc.spellCard(), trigger.spellFilter(), null,
                match.gameData(), sc.castingPlayerId())) return false;

        List<CardEffect> resolvedEffects = List.of(new GivePoisonCountersEffect(trigger.amount(), PoisonRecipient.TARGET_PLAYER));
        match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                match.permanent().getCard(), match.controllerId(), new ArrayList<>(resolvedEffects), true
        ));
        gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                "'s triggered ability triggers — choose target player for poison counter."));
        log.info("Game {} - {} spell-cast poison trigger queued",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    // ── ON_OPPONENT_CASTS_SPELL ────────────────────────────────────────

    @CollectsTrigger(value = SpellCastTriggerEffect.class, slot = EffectSlot.ON_OPPONENT_CASTS_SPELL)
    private boolean handleOpponentSpellCastTrigger(TriggerMatchContext match, SpellCastTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        return handleGenericSpellCastTrigger(match, trigger, sc.spellCard(), sc.castingPlayerId());
    }

    @CollectsTrigger(value = LoseLifeUnlessDiscardEffect.class, slot = EffectSlot.ON_OPPONENT_CASTS_SPELL)
    private boolean handleLoseLifeUnlessDiscard(TriggerMatchContext match,
            LoseLifeUnlessDiscardEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger))
        );
        entry.setTargetId(sc.castingPlayerId());
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsTrigger(value = DealDamageToPlayersEffect.class, slot = EffectSlot.ON_OPPONENT_CASTS_SPELL)
    private boolean handleDamageToCastingOpponent(TriggerMatchContext match,
            DealDamageToPlayersEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger))
        );
        entry.setTargetId(sc.castingPlayerId());
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsTrigger(value = DrawCardForTargetPlayerEffect.class, slot = EffectSlot.ON_OPPONENT_CASTS_SPELL)
    private boolean handleCastingOpponentDraws(TriggerMatchContext match,
            DrawCardForTargetPlayerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger))
        );
        entry.setTargetId(sc.castingPlayerId());
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsTrigger(value = MillEffect.class, slot = EffectSlot.ON_OPPONENT_CASTS_SPELL)
    private boolean handleCastingOpponentMills(TriggerMatchContext match,
            MillEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        // "That player mills N cards" — carry the casting opponent on targetId so the
        // TARGET_PLAYER mill lands on them (not a chosen target). Memory Erosion.
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger))
        );
        entry.setTargetId(sc.castingPlayerId());
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsTrigger(value = CounterUnlessPaysEffect.class, slot = EffectSlot.ON_OPPONENT_CASTS_SPELL)
    private boolean handleCounterUnlessPays(TriggerMatchContext match,
            CounterUnlessPaysEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                sc.spellCard().getId(),
                Zone.STACK
        );
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsTrigger(value = RevealTopCardCreatureToBattlefieldOrMayBottomEffect.class, slot = EffectSlot.ON_OPPONENT_CASTS_SPELL)
    private boolean handleRevealTopCardCreatureToBattlefield(TriggerMatchContext match,
            RevealTopCardCreatureToBattlefieldOrMayBottomEffect trigger, TriggerContext ctx) {
        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger))
        ));
        return true;
    }

    @CollectsTrigger(value = LoseLifeUnlessPaysEffect.class, slot = EffectSlot.ON_OPPONENT_CASTS_SPELL)
    private boolean handleLoseLifeUnlessPays(TriggerMatchContext match,
            LoseLifeUnlessPaysEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (trigger.spellFilter() != null
                && !predicateEvaluationService.matchesCardPredicate(sc.spellCard(), trigger.spellFilter(), null,
                        match.gameData(), sc.castingPlayerId())) {
            return false;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger))
        );
        entry.setTargetId(sc.castingPlayerId());
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsTrigger(value = DamageUnlessPaysEffect.class, slot = EffectSlot.ON_OPPONENT_CASTS_SPELL)
    private boolean handleDamageUnlessPays(TriggerMatchContext match,
            DamageUnlessPaysEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (trigger.spellFilter() != null
                && !predicateEvaluationService.matchesCardPredicate(sc.spellCard(), trigger.spellFilter(), null,
                        match.gameData(), sc.castingPlayerId())) {
            return false;
        }
        // "that player" = the casting opponent (target of the damage / decision maker). Carry the source
        // permanent so the damage resolves with the correct source (prevention keys off it).
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                sc.castingPlayerId(),
                match.permanent().getId()
        );
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsTrigger(value = SpellCastLifeDrainEffect.class, slot = EffectSlot.ON_OPPONENT_CASTS_SPELL)
    private boolean handleOpponentSpellCastDrain(TriggerMatchContext match,
            SpellCastLifeDrainEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (trigger.spellFilter() != null
                && !predicateEvaluationService.matchesCardPredicate(sc.spellCard(), trigger.spellFilter(), null,
                        match.gameData(), sc.castingPlayerId())) {
            return false;
        }
        // "That player loses N life and you gain M life" — carry the casting opponent on targetId so the
        // TARGET_PLAYER life loss lands on them (the casting player is not a chosen target), then the
        // controller gains the fixed amount. Omit the gain step when the card only drains life.
        List<CardEffect> drainEffects = new ArrayList<>();
        drainEffects.add(new LoseLifeEffect(trigger.lifeLoss(), LoseLifeRecipient.TARGET_PLAYER));
        if (trigger.lifeGain() > 0) {
            drainEffects.add(new GainLifeEffect(trigger.lifeGain()));
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                drainEffects
        );
        entry.setTargetId(sc.castingPlayerId());
        entry.setNonTargeting(true);
        match.gameData().stack.add(entry);
        return true;
    }

    @CollectsTrigger(value = PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect.class, slot = EffectSlot.ON_OPPONENT_CASTS_SPELL)
    private boolean handleOpponentColorCounter(TriggerMatchContext match,
            PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (sc.spellCard().getColor() == null) return false;
        if (!trigger.triggerColors().contains(sc.spellCard().getColor())) return false;
        return addColorCounterTrigger(match, trigger);
    }

    // ── Shared helpers ─────────────────────────────────────────────────

    private boolean handleGenericSpellCastTrigger(TriggerMatchContext match, SpellCastTriggerEffect trigger,
                                                    Card spellCard, UUID castingPlayerId) {
        // "Whenever you cast a spell during an opponent's turn" — the source's controller must not be
        // the active player when the spell is cast (Glen Elendra Pranksters).
        if (trigger.onlyDuringOpponentTurn()
                && match.controllerId().equals(match.gameData().activePlayerId)) return false;

        // "Whenever an opponent casts a spell during your turn" — the source's controller must be
        // the active player when the spell is cast (Eyes of the Wisent).
        if (trigger.onlyDuringControllerTurn()
                && !match.controllerId().equals(match.gameData().activePlayerId)) return false;

        if (!predicateEvaluationService.matchesCardPredicate(spellCard, trigger.spellFilter(), null,
                match.gameData(), castingPlayerId)) return false;

        // Repartee-style condition on the cast spell's chosen targets (e.g. "targets a creature").
        if (trigger.castSpellTargetCondition() != null) {
            StackEntry spellEntry = findStackEntryForCard(match.gameData(), spellCard.getId());
            if (spellEntry == null) return false;
            if (!targetLegalityService.matchesStackEntryPredicate(match.gameData(), spellEntry,
                    trigger.castSpellTargetCondition(), castingPlayerId)) return false;
        }

        List<CardEffect> resolved = new ArrayList<>(trigger.resolvedEffects());
        boolean selfTarget = resolved.stream().anyMatch(e -> e.targetSpec().selfTargeting());
        boolean needsPlayerTarget = resolved.stream().anyMatch(e -> e.targetSpec().category().includesPlayers());
        boolean needsPermanentTarget = resolved.stream().anyMatch(e -> e.targetSpec().category().includesPermanents());
        boolean needsGraveyardTarget = resolved.stream().anyMatch(e -> e.targetSpec().category().isGraveyard());
        boolean needsTargeting = needsPlayerTarget || needsPermanentTarget;
        boolean playerTargetOnly = needsPlayerTarget && !needsPermanentTarget;
        boolean needsSpellManaSpentX = resolved.stream().anyMatch(this::effectNeedsSpellManaSpentX);
        int spellManaSpentX = needsSpellManaSpentX
                ? match.gameData().getSpellCastManaSpent(spellCard.getId()) : 0;

        if (match.rawEffect() instanceof MayEffect may) {
            match.gameData().pendingMayAbilities.add(new PendingMayAbility(
                    match.permanent().getCard(),
                    match.controllerId(),
                    resolved,
                    match.permanent().getCard().getName() + " — " + may.prompt(),
                    null,
                    trigger.manaCost(),
                    match.permanent().getId()));
        } else if (needsGraveyardTarget) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    match.permanent().getCard(), match.controllerId(), resolved
            ));
            log.info("Game {} - {} spell-cast graveyard-target trigger queued",
                    match.gameData().id, match.permanent().getCard().getName());
        } else if (needsTargeting) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    match.permanent().getCard(), match.controllerId(), resolved, playerTargetOnly, trigger.targetFilter(),
                    spellManaSpentX
            ));
            gameBroadcastService.logAndBroadcast(match.gameData(), GameLog.cardThen(match.permanent().getCard(),
                    "'s triggered ability triggers — choose a target."));
        } else {
            StackEntry entry;
            if (selfTarget) {
                entry = spellManaSpentX > 0
                        ? new StackEntry(StackEntryType.TRIGGERED_ABILITY, match.permanent().getCard(), match.controllerId(),
                            match.permanent().getCard().getName() + "'s ability", resolved, spellManaSpentX,
                            match.permanent().getId())
                        : new StackEntry(StackEntryType.TRIGGERED_ABILITY, match.permanent().getCard(), match.controllerId(),
                            match.permanent().getCard().getName() + "'s ability", resolved, null, match.permanent().getId());
            } else {
                entry = spellManaSpentX > 0
                        ? new StackEntry(StackEntryType.TRIGGERED_ABILITY, match.permanent().getCard(), match.controllerId(),
                            match.permanent().getCard().getName() + "'s ability", resolved, spellManaSpentX)
                        : new StackEntry(StackEntryType.TRIGGERED_ABILITY, match.permanent().getCard(), match.controllerId(),
                            match.permanent().getCard().getName() + "'s ability", resolved);
            }
            match.gameData().stack.add(entry);
        }
        return true;
    }

    private StackEntry findStackEntryForCard(com.github.laxika.magicalvibes.model.GameData gameData, UUID cardId) {
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(cardId)) {
                return se;
            }
        }
        return null;
    }

    private boolean effectNeedsSpellManaSpentX(CardEffect effect) {
        if (effect instanceof BoostSelfEffect boost
                && (amountEvaluationService.referencesXValue(boost.powerBoost())
                || amountEvaluationService.referencesXValue(boost.toughnessBoost()))) {
            return true;
        }
        if (effect instanceof ConditionalEffect conditional) {
            return conditional.condition() instanceof SpellManaSpentAtLeast
                    || effectNeedsSpellManaSpentX(conditional.wrapped());
        }
        if (effect instanceof ConditionalReplacementEffect replacement) {
            return replacement.condition() instanceof SpellManaSpentAtLeast
                    || effectNeedsSpellManaSpentX(replacement.baseEffect())
                    || effectNeedsSpellManaSpentX(replacement.upgradedEffect());
        }
        return false;
    }

    private boolean addColorCounterTrigger(TriggerMatchContext match,
            PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect trigger) {
        List<CardEffect> resolvedEffects = List.of(new PutCountersOnSourceEffect(1, 1, trigger.amount()));

        if (match.rawEffect() instanceof MayEffect may) {
            match.gameData().pendingMayAbilities.add(new PendingMayAbility(
                    match.permanent().getCard(),
                    match.controllerId(),
                    resolvedEffects,
                    match.permanent().getCard().getName() + " — " + may.prompt(),
                    null,
                    null,
                    match.permanent().getId()
            ));
        } else {
            match.gameData().stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    match.permanent().getCard(),
                    match.controllerId(),
                    match.permanent().getCard().getName() + "'s ability",
                    new ArrayList<>(resolvedEffects),
                    null,
                    match.permanent().getId()
            ));
        }
        return true;
    }
}
