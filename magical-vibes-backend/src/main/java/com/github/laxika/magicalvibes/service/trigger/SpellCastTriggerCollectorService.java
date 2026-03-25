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
import com.github.laxika.magicalvibes.model.effect.CastFromGraveyardTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherSubtypePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEqualToSpellManaValueToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GiveTargetPlayerPoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.KickedSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.KnowledgePoolCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.KnowledgePoolExileAndCastEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureToBattlefieldOrMayBottomEffect;
import com.github.laxika.magicalvibes.model.effect.ChosenSubtypeSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Trigger collectors for spell-cast events (ON_ANY_PLAYER_CASTS_SPELL,
 * ON_CONTROLLER_CASTS_SPELL, ON_OPPONENT_CASTS_SPELL).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpellCastTriggerCollectorService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

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

        StackEntry snapshot = new StackEntry(spellEntry);
        CopySpellForEachOtherPlayerEffect resolutionEffect =
                new CopySpellForEachOtherPlayerEffect(snapshot, sc.castingPlayerId());

        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(resolutionEffect))
        ));
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

    @CollectsTrigger(value = ChosenSubtypeSpellCastTriggerEffect.class, slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleChosenSubtypeSpellCastTrigger(TriggerMatchContext match,
            ChosenSubtypeSpellCastTriggerEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        CardSubtype chosenSubtype = match.permanent().getChosenSubtype();
        if (chosenSubtype == null) return false;

        // Must be a creature spell of the chosen type
        if (!gameQueryService.matchesCardPredicate(sc.spellCard(),
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardSubtypePredicate(chosenSubtype)
                )), null, match.gameData(), sc.castingPlayerId())) return false;

        match.gameData().stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(trigger.resolvedEffects())
        ));

        log.info("Game {} - {} chosen-subtype spell-cast trigger queued",
                match.gameData().id, match.permanent().getCard().getName());
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
        boolean selfTarget = resolved.stream().anyMatch(CardEffect::isSelfTargeting);

        StackEntry entry = selfTarget
                ? new StackEntry(StackEntryType.TRIGGERED_ABILITY, match.permanent().getCard(), match.controllerId(),
                    match.permanent().getCard().getName() + "'s ability", resolved, null, match.permanent().getId())
                : new StackEntry(StackEntryType.TRIGGERED_ABILITY, match.permanent().getCard(), match.controllerId(),
                    match.permanent().getCard().getName() + "'s ability", resolved);
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
                .anyMatch(e -> e.canTargetPlayer() || e.canTargetPermanent());

        if (needsAnyTarget) {
            match.gameData().pendingSpellTargetTriggers.add(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    match.permanent().getCard(), match.controllerId(), new ArrayList<>(trigger.resolvedEffects())
            ));
            String logEntry = match.permanent().getCard().getName()
                    + "'s triggered ability triggers — choose a target.";
            gameBroadcastService.logAndBroadcast(match.gameData(), logEntry);
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

    @CollectsTrigger(value = DealDamageEqualToSpellManaValueToAnyTargetEffect.class, slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handleManaValueDamage(TriggerMatchContext match,
            DealDamageEqualToSpellManaValueToAnyTargetEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (!gameQueryService.matchesCardPredicate(sc.spellCard(), trigger.spellFilter(), null,
                match.gameData(), sc.castingPlayerId())) return false;

        int manaValue = sc.spellCard().getManaValue();
        List<CardEffect> resolvedEffects = List.of(new DealDamageToAnyTargetEffect(manaValue));
        match.gameData().pendingSpellTargetTriggers.add(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                match.permanent().getCard(), match.controllerId(), new ArrayList<>(resolvedEffects)
        ));
        String logEntry = match.permanent().getCard().getName()
                + "'s triggered ability triggers — choose a target for " + manaValue + " damage.";
        gameBroadcastService.logAndBroadcast(match.gameData(), logEntry);
        log.info("Game {} - {} spell-cast mana-value trigger queued ({} damage)",
                match.gameData().id, match.permanent().getCard().getName(), manaValue);
        return true;
    }

    @CollectsTrigger(value = GiveTargetPlayerPoisonCountersEffect.class, slot = EffectSlot.ON_CONTROLLER_CASTS_SPELL)
    private boolean handlePoisonOnSpellCast(TriggerMatchContext match,
            GiveTargetPlayerPoisonCountersEffect trigger, TriggerContext ctx) {
        TriggerContext.SpellCast sc = (TriggerContext.SpellCast) ctx;
        if (trigger.spellFilter() == null) return false;
        if (!gameQueryService.matchesCardPredicate(sc.spellCard(), trigger.spellFilter(), null,
                match.gameData(), sc.castingPlayerId())) return false;

        List<CardEffect> resolvedEffects = List.of(new GiveTargetPlayerPoisonCountersEffect(trigger.amount()));
        match.gameData().pendingSpellTargetTriggers.add(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                match.permanent().getCard(), match.controllerId(), new ArrayList<>(resolvedEffects), true
        ));
        String logEntry = match.permanent().getCard().getName()
                + "'s triggered ability triggers — choose target player for poison counter.";
        gameBroadcastService.logAndBroadcast(match.gameData(), logEntry);
        log.info("Game {} - {} spell-cast poison trigger queued",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    // ── ON_OPPONENT_CASTS_SPELL ────────────────────────────────────────

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
                && !gameQueryService.matchesCardPredicate(sc.spellCard(), trigger.spellFilter(), null,
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
        if (!gameQueryService.matchesCardPredicate(spellCard, trigger.spellFilter(), null,
                match.gameData(), castingPlayerId)) return false;

        List<CardEffect> resolved = new ArrayList<>(trigger.resolvedEffects());
        boolean selfTarget = resolved.stream().anyMatch(CardEffect::isSelfTargeting);
        boolean needsPlayerTarget = resolved.stream().anyMatch(CardEffect::canTargetPlayer);
        boolean needsPermanentTarget = resolved.stream().anyMatch(CardEffect::canTargetPermanent);
        boolean needsGraveyardTarget = resolved.stream().anyMatch(CardEffect::canTargetGraveyard);
        boolean needsTargeting = needsPlayerTarget || needsPermanentTarget;
        boolean playerTargetOnly = needsPlayerTarget && !needsPermanentTarget;

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
            match.gameData().pendingSpellGraveyardTargetTriggers.add(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    match.permanent().getCard(), match.controllerId(), resolved
            ));
            log.info("Game {} - {} spell-cast graveyard-target trigger queued",
                    match.gameData().id, match.permanent().getCard().getName());
        } else if (needsTargeting) {
            match.gameData().pendingSpellTargetTriggers.add(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    match.permanent().getCard(), match.controllerId(), resolved, playerTargetOnly, trigger.targetFilter()
            ));
            String logEntry = match.permanent().getCard().getName()
                    + "'s triggered ability triggers — choose a target.";
            gameBroadcastService.logAndBroadcast(match.gameData(), logEntry);
        } else {
            StackEntry entry = selfTarget
                    ? new StackEntry(StackEntryType.TRIGGERED_ABILITY, match.permanent().getCard(), match.controllerId(),
                        match.permanent().getCard().getName() + "'s ability", resolved, null, match.permanent().getId())
                    : new StackEntry(StackEntryType.TRIGGERED_ABILITY, match.permanent().getCard(), match.controllerId(),
                        match.permanent().getCard().getName() + "'s ability", resolved);
            match.gameData().stack.add(entry);
        }
        return true;
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
