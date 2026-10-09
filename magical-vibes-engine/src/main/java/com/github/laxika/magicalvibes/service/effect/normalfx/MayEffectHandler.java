package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CipherEncodeEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MayEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final EffectHandlerRegistry effectHandlerRegistry;
    private final DrawService drawService;
    private final PredicateEvaluationService predicateEvaluationService;
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private AuraAttachmentService auraAttachmentService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MayEffect) effect;
        if (e.wrapped() instanceof ReturnCardFromGraveyardEffect battlefieldReturn
                && battlefieldReturn.targetGraveyard()
                && battlefieldReturn.destination() == GraveyardChoiceDestination.BATTLEFIELD
                && e.elseEffect() instanceof ReturnCardFromGraveyardEffect handReturn
                && handReturn.destination() == GraveyardChoiceDestination.HAND) {
            Card card = gameQueryService.findCardInGraveyardById(gameData, entry.getTargetId());
            if (card != null && (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)
                    || card.isAura() && !hasLegalAuraAttachment(gameData, card, entry.getControllerId()))) {
                insertElseEffect(entry, e);
                return;
            }
        }
        if (e.wrapped() instanceof com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect tap
                && tap.scope() == com.github.laxika.magicalvibes.model.effect.TapUntapScope.TRIGGERING) {
            var permanent = gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
            if (permanent == null || permanent.isTapped()) {
                insertElseEffect(entry, e);
                return;
            }
        }
        CardEffect firstEffect = e.wrapped() instanceof SequenceEffect sequence && !sequence.steps().isEmpty()
                ? sequence.steps().getFirst() : e.wrapped();
        if (firstEffect instanceof com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect counters
                && counters.predicate() == null) {
            var target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
            if (target == null || gameQueryService.cantHaveCounters(gameData, target)
                    || counters.counterType() == com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE
                    && gameQueryService.cantHavePlusOnePlusOneCounters(gameData, target)
                    || counters.counterType() == com.github.laxika.magicalvibes.model.CounterType.MINUS_ONE_MINUS_ONE
                    && gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, target)) {
                insertElseEffect(entry, e);
                return;
            }
        }
        if (e.wrapped() instanceof PutCountersOnSourceEffect counters) {
            var source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            if (source == null || gameQueryService.cantHaveCounters(gameData, source)
                    || counters.powerModifier() > 0
                    && gameQueryService.cantHavePlusOnePlusOneCounters(gameData, source)
                    || counters.powerModifier() < 0
                    && gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, source)) {
                insertElseEffect(entry, e);
                return;
            }
        }

        // CR 702.99a — cipher is "If this spell is represented by a card, you may exile this card
        // encoded on a creature you control". A copy cast off the encoded card is not represented by
        // a card, so the copy's cipher ability does nothing and must not prompt.
        if (entry.isCopy() && e.wrapped() instanceof CipherEncodeEffect) {
            return;
        }

        // On a multi-target card each "you may" is bound to its own target group, so the pending
        // ability must carry that group's target rather than the entry's lone one. A bound group
        // with no legal target left does nothing (CR 608.2b) — don't even prompt.
        UUID targetId = entry.getTargetId();
        List<UUID> groupTargets = entry.targetsForBoundEffectGroup(e);
        if (groupTargets != null && !entry.getTargetIds().isEmpty()) {
            if (groupTargets.isEmpty()) {
                return;
            }
            targetId = groupTargets.getFirst();
        }

        if (e.wrapped() instanceof com.github.laxika.magicalvibes.model.effect.AttachTargetEquipmentToTriggeringPermanentEffect
                && entry.getDeclaredTargetIds().isEmpty() && targetId == null) {
            return;
        }

        // Optional hand-ability targets can be omitted (e.g. Decree of Silence cycling).
        // Skip that optional effect while continuing the ability's remaining effects.
        if (targetId == null && entry.isCyclingAbility()
                && e.wrapped() instanceof CounterSpellEffect) {
            return;
        }

        // CR 603.5 — "you may" choice happens at resolution time.
        // Set flag so the resolution loop re-runs this effect after the player responds.
        gameData.resolvingMayEffectFromStack = true;
        UUID choicePlayerId = e.choicePlayerId() != null ? e.choicePlayerId() : switch (e.choicePlayer()) {
            case CONTROLLER -> entry.getControllerId();
            // Triggered abilities snapshot the active player on their stack entry. Activated
            // abilities do not need that trigger snapshot, so use the current active player for
            // effects such as Obeka's "the player whose turn it is may ...".
            case ACTIVE_PLAYER -> entry.getActivePlayerId() != null
                    ? entry.getActivePlayerId() : gameData.activePlayerId;
            case DEFENDING_PLAYER -> findDefendingPlayerId(gameData, entry.getAttackedTargetId());
            case TARGET_PLAYER -> targetId != null && gameData.playerIds.contains(targetId) ? targetId : null;
            case TARGET_PERMANENT_CONTROLLER -> targetPermanentController(gameData, entry, targetId);
            case TARGET_SPELL_CONTROLLER -> findTargetSpellControllerId(gameData, targetId);
            case TRIGGERING_PERMANENT_CONTROLLER -> findTriggeringPermanentControllerId(gameData, entry, targetId);
            case TARGET_PLAYER_OR_PERMANENT_CONTROLLER -> targetId == null
                    ? null
                    : gameData.playerIds.contains(targetId)
                    ? targetId
                    : gameQueryService.findPermanentController(gameData, targetId);
            case TRIGGERING_SPELL_CONTROLLER -> targetId;
            case LAST_MANA_PAYMENT_PLAYER -> entry.getLastManaPaymentPlayerId();
            case SOURCE_OWNER -> sourceOwnerId(entry);
        };
        if (choicePlayerId == null) {
            gameData.resolvingMayEffectFromStack = false;
            return;
        }
        CardEffect optionalAction = e.wrapped() instanceof SequenceEffect sequence
                && !sequence.steps().isEmpty() ? sequence.steps().getFirst() : e.wrapped();
        if (optionalAction instanceof DrawCardEffect
                && drawService.isDrawPrevented(gameData, choicePlayerId)) {
            gameData.resolvingMayEffectFromStack = false;
            return;
        }
        boolean cannotSacrifice = e.wrapped() instanceof SacrificeEnchantedCreatureEffect
                && !canSacrificeEnchantedPermanent(gameData, entry, choicePlayerId);
        if (optionalAction instanceof com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect) {
            var source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            cannotSacrifice = source == null
                    || !choicePlayerId.equals(gameQueryService.findPermanentController(gameData, source.getId()))
                    || gameQueryService.cantBeSacrificed(gameData, source);
        }
        if (e.wrapped() instanceof com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect sacrifice
                && sacrifice.count() instanceof com.github.laxika.magicalvibes.model.amount.Fixed count
                && count.value() > 0
                && (sacrifice.recipient() == com.github.laxika.magicalvibes.model.effect.SacrificeRecipient.TARGET_PLAYER
                || sacrifice.recipient() == com.github.laxika.magicalvibes.model.effect.SacrificeRecipient.CONTROLLER)) {
            cannotSacrifice = gameData.playerBattlefields.getOrDefault(choicePlayerId, List.of()).stream()
                    .noneMatch(permanent -> !gameQueryService.cantBeSacrificed(gameData, permanent)
                            && (sacrifice.filter() == null || predicateEvaluationService.matchesPermanentPredicate(
                            gameData, permanent, sacrifice.filter())));
        }
        if (cannotSacrifice) {
            gameData.resolvingMayEffectFromStack = false;
            insertElseEffect(entry, e);
            return;
        }

        boolean defendingPlayerChoice = e.choicePlayer() == MayChoicePlayer.DEFENDING_PLAYER;

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                defendingPlayerChoice ? entry.getControllerId() : choicePlayerId,
                List.of(e.wrapped()),
                entry.getCard().getName() + " - " + e.prompt(),
                targetId,
                null,
                entry.getSourcePermanentId(),
                null,
                0,
                0,
                entry.getAttackedTargetId(),
                e.choicePlayer() == MayChoicePlayer.ACTIVE_PLAYER ? entry.getActivePlayerId() : null,
                defendingPlayerChoice ? choicePlayerId : null,
                entry.getSourcePermanentSnapshot(),
                entry.getTriggeringPermanentControllerId(),
                entry.getTriggeringCardId(),
                entry.getEventValue(),
                entry.getTriggeringPermanentId(),
                entry.getTriggeringPermanentPowerAtTrigger(),
                null,
                entry.getTriggeringPermanentToughnessAtTrigger()
        ));
    }

    private void insertElseEffect(StackEntry entry, MayEffect may) {
        if (may.elseEffect() != null) {
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, List.of(may.elseEffect()));
        }
    }

    private boolean canSacrificeEnchantedPermanent(GameData gameData, StackEntry entry, UUID choicePlayerId) {
        UUID enchantedId = entry.getSourcePermanentSnapshot() != null
                ? entry.getSourcePermanentSnapshot().getAttachedTo()
                : null;
        if (enchantedId == null && entry.getSourcePermanentId() != null) {
            var aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            enchantedId = aura == null ? null : aura.getAttachedTo();
        }
        return enchantedId != null
                && choicePlayerId.equals(gameQueryService.findPermanentController(gameData, enchantedId))
                && !gameQueryService.cantBeSacrificed(gameData,
                gameQueryService.findPermanentById(gameData, enchantedId));
    }

    private UUID targetPermanentController(GameData gameData, StackEntry entry, UUID targetId) {
        if (targetId == null) return null;
        UUID current = gameQueryService.findPermanentController(gameData, targetId);
        return current == null ? entry.getRemovedPermanentControllers().get(targetId) : current;
    }

    private UUID findTriggeringPermanentControllerId(GameData gameData, StackEntry entry, UUID fallback) {
        if (entry.getTriggeringPermanentId() != null) {
            UUID liveControllerId = gameQueryService.findPermanentController(gameData, entry.getTriggeringPermanentId());
            if (liveControllerId != null) {
                return liveControllerId;
            }
        }
        return entry.getTriggeringPermanentControllerId() != null
                ? entry.getTriggeringPermanentControllerId() : fallback;
    }

    private UUID sourceOwnerId(StackEntry entry) {
        return entry.getCard() != null && entry.getCard().getOwnerId() != null
                ? entry.getCard().getOwnerId() : entry.getControllerId();
    }

    private UUID findTargetSpellControllerId(GameData gameData, UUID targetCardId) {
        if (targetCardId == null) {
            return null;
        }
        for (StackEntry stackEntry : gameData.stack) {
            if (stackEntry.getTargetableId().equals(targetCardId)) {
                return stackEntry.getControllerId();
            }
        }
        return null;
    }

    private boolean hasLegalAuraAttachment(GameData gameData, Card aura, UUID controllerId) {
        for (var battlefield : gameData.playerBattlefields.values()) {
            for (var permanent : battlefield) {
                if (auraAttachmentService.canEnchant(gameData, aura, controllerId, permanent)) return true;
            }
        }
        return aura.isEnchantPlayer() && gameData.orderedPlayerIds.stream()
                .anyMatch(playerId -> auraAttachmentService.canEnchantPlayer(gameData, aura, controllerId, playerId));
    }

    private UUID findDefendingPlayerId(GameData gameData, UUID attackedTargetId) {
        if (attackedTargetId == null) {
            return null;
        }
        return gameData.playerIds.contains(attackedTargetId)
                ? attackedTargetId
                : gameQueryService.findPermanentController(gameData, attackedTargetId);
    }
}
