package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CipherEncodeEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLibraryAction;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayPutMilledLandsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.service.aura.AuraAttachmentService;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
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
    private final AmountEvaluationService amountEvaluationService;
    private final com.github.laxika.magicalvibes.service.CardRevealService cardRevealService;
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
        CardEffect declineBranch = e.elseEffect() instanceof MayEffect elseMay ? elseMay.wrapped() : e.elseEffect();
        if (e.wrapped() instanceof com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect
                && e.elseEffect() == null
                && gameData.playerHands.getOrDefault(entry.getControllerId(), List.of()).isEmpty()) {
            // Nothing can be discarded, so there is no choice to offer and "if you do" can't happen.
            return;
        }
        if (e.wrapped() instanceof com.github.laxika.magicalvibes.model.effect.MayRevealSubtypeFromHandEffect reveal
                && gameData.playerHands.getOrDefault(entry.getControllerId(), List.of()).stream()
                .noneMatch(card -> gameQueryService.cardHasSubtype(
                        card, reveal.subtype(), gameData, entry.getControllerId()))) {
            // No card of the subtype in hand to reveal, so no choice is offered and the ability does nothing.
            return;
        }
        if (e.wrapped() instanceof com.github.laxika.magicalvibes.model.effect.ReturnDelayedGraveyardCardUnderControlEffect delayedReturn
                && delayedReturn.pending().requireSourceOnBattlefield()
                && (delayedReturn.pending().sourcePermanentId() == null
                || gameQueryService.findPermanentById(gameData, delayedReturn.pending().sourcePermanentId()) == null)) {
            // "If it is still on the battlefield" (Shirei) fails, so the return can't happen and there is nothing to decide.
            return;
        }
        if (e.wrapped() instanceof LookAtTopCardsOfTargetLibraryEffect look
                && look.action() == TargetLibraryAction.MAY_PUT_TOP_ON_BOTTOM
                && entry.getTargetId() != null
                && gameData.playerDecks.getOrDefault(entry.getTargetId(), List.of()).isEmpty()) {
            // The targeted library is empty, so there is no top card to look at and no choice is offered.
            return;
        }
        if (e.wrapped() instanceof com.github.laxika.magicalvibes.model.effect.ExchangeControlOfSourceAndTriggeringSpellEffect) {
            // Both objects are already controlled by the same player, so the exchange does nothing and no choice is offered.
            Permanent exchangeSource = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            StackEntry exchangeSpell = entry.getTriggeringCardId() == null ? null
                    : gameQueryService.findStackEntryByCardId(gameData, entry.getTriggeringCardId());
            if (exchangeSource != null && exchangeSpell != null
                    && java.util.Objects.equals(
                    gameQueryService.findPermanentController(gameData, exchangeSource.getId()),
                    exchangeSpell.getControllerId())) {
                return;
            }
        }
        CardEffect leadingEffect = e.wrapped() instanceof SequenceEffect leadingSequence && !leadingSequence.steps().isEmpty()
                ? leadingSequence.steps().getFirst() : e.wrapped();
        boolean drawThenDiscard = e.wrapped() instanceof SequenceEffect drawSequence
                && drawSequence.steps().stream().anyMatch(com.github.laxika.magicalvibes.model.effect.DiscardEffect.class::isInstance);
        if (leadingEffect instanceof DrawCardEffect && !drawThenDiscard
                && drawService.isDrawPrevented(gameData, entry.getControllerId())) {
            // The controller can't draw (e.g. Narset's draw limit), so the "may have you draw" choice
            // can't be carried out and no choice is offered; the "if they do" branch never happens.
            // A "draw a card, then discard a card" loot is still offered: the discard is a separate
            // instruction that is followed even though the draw is impossible (CR 608.2c, CR 609.3).
            insertElseEffect(entry, e);
            return;
        }
        if (e.wrapped() instanceof MillControllerAndMayPutMilledLandsOntoBattlefieldEffect mill
                && e.elseEffect() == null
                && amountEvaluationService.evaluate(gameData, mill.count(),
                AmountContext.forStackEntry(entry, entry.getSourcePermanentSnapshot()))
                > gameData.playerDecks.getOrDefault(entry.getControllerId(), List.of()).size()) {
            // CR 701.17b — a player can't choose to mill more cards than their library holds, so no choice is offered.
            return;
        }
        if (e.wrapped() instanceof ReturnCardFromGraveyardEffect battlefieldReturn
                && battlefieldReturn.targetGraveyard()
                && battlefieldReturn.destination() == GraveyardChoiceDestination.BATTLEFIELD
                && declineBranch instanceof ReturnCardFromGraveyardEffect handReturn
                && handReturn.destination() == GraveyardChoiceDestination.HAND) {
            UUID graveyardTargetId = entry.getTargetId() != null ? entry.getTargetId()
                    : entry.getTargetCardIds().isEmpty() ? null : entry.getTargetCardIds().getFirst();
            Card card = gameQueryService.findCardInGraveyardById(gameData, graveyardTargetId);
            if (card != null && (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, card, Zone.GRAVEYARD)
                    || card.isAura() && !hasLegalAuraAttachment(gameData, card, entry.getControllerId()))) {
                insertElseEffect(entry, e);
                return;
            }
        }
        if (e.wrapped() instanceof com.github.laxika.magicalvibes.model.effect.BlightEffect
                && !controlsBlightableCreature(gameData, entry.getControllerId())) {
            insertElseEffect(entry, e);
            return;
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
            if (source == null && e.elseEffect() == null) {
                // Nothing replaces the counters and nothing observable depends on the choice, but the
                // controller still makes it (Kresh's trigger after Kresh left the battlefield).
            } else if (source == null || gameQueryService.cantHaveCounters(gameData, source)
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

        // An "up to N" graveyard return with no chosen cards has nothing to decide, so no choice is offered.
        if (e.wrapped() instanceof ReturnTargetCardsFromGraveyardToHandEffect
                && targetId == null && entry.getTargetCardIds().isEmpty()) {
            return;
        }

        // An "up to N" target choice with no creature chosen (CR 601.2c) leaves nothing to tap, so no choice is offered.
        if (e.wrapped() instanceof com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect tap
                && tap.scope() == com.github.laxika.magicalvibes.model.effect.TapUntapScope.TARGET
                && targetId == null && entry.getTargetIds().isEmpty()) {
            return;
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

        if (e.wrapped() instanceof com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect hand
                && hand.looksAtHandBeforeMayChoice() && targetId != null && gameData.playerIds.contains(targetId)
                && e.choicePlayer() == MayChoicePlayer.CONTROLLER) {
            cardRevealService.lookAtHand(gameData, entry.getControllerId(), targetId);
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
            case TARGET_SPELL_CONTROLLER -> {
                UUID onStack = findTargetSpellControllerId(gameData, targetId);
                yield onStack != null ? onStack : entry.getCounteredSpellControllerId();
            }
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
        UUID discarderId = optionalAction instanceof DiscardEffect discard
                ? switch (discard.recipient()) {
                    case CONTROLLER -> entry.getControllerId();
                    case TARGET_PLAYER -> !discard.random() && !entry.isNonTargeting() ? targetId : null;
                    default -> null;
                }
                : null;
        if (discarderId != null && gameData.playerHands.getOrDefault(discarderId, List.of()).isEmpty()) {
            // Nothing can be discarded, so no discard choice is offered and "if you don't" applies. A random
            // discard, or a discard by "that player" from a nontargeting combat damage trigger, is still asked (CR 603.5).
            gameData.resolvingMayEffectFromStack = false;
            insertElseEffect(entry, e);
            return;
        }
        if (optionalAction instanceof DrawCardEffect && !drawThenDiscard
                && drawService.isDrawPrevented(gameData, entry.getControllerId())) {
            // The ability's controller is the one who would draw, whoever makes the choice.
            gameData.resolvingMayEffectFromStack = false;
            insertElseEffect(entry, e);
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
            UUID sacrificingPlayerId = sacrifice.recipient()
                    == com.github.laxika.magicalvibes.model.effect.SacrificeRecipient.TARGET_PLAYER && targetId != null
                    ? targetId : choicePlayerId;
            // The choice is only withheld when the deciding player is the one who would sacrifice; a
            // controller choosing for a targeted opponent is asked whatever that opponent controls.
            cannotSacrifice = choicePlayerId.equals(sacrificingPlayerId)
                    && gameData.playerBattlefields.getOrDefault(sacrificingPlayerId, List.of()).stream()
                    .noneMatch(permanent -> !gameQueryService.cantBeChosenToSacrifice(gameData, permanent, sacrifice.filter())
                            && (sacrifice.filter() == null || predicateEvaluationService.matchesPermanentPredicate(
                            gameData, permanent, sacrifice.filter())));
        }
        if (cannotSacrifice) {
            gameData.resolvingMayEffectFromStack = false;
            insertElseEffect(entry, e);
            return;
        }

        boolean defendingPlayerChoice = e.choicePlayer() == MayChoicePlayer.DEFENDING_PLAYER;
        if (e.choicePlayer() == MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER) {
            // The resolved effect acts for the triggering permanent's controller as of resolution, which
            // can differ from the trigger-time controller if control changed while the ability waited.
            entry.setTriggeringPermanentControllerId(choicePlayerId);
            if (e.wrapped() instanceof DrawCardForTargetPlayerEffect draw
                    && !draw.targetsPlayer() && draw.targetGroup() < 0) {
                entry.setTargetId(choicePlayerId);
            }
        }

        // "Its controller may draw a card" draws for the triggering permanent's controller as of
        // resolution, which is the player making the choice.
        UUID pendingTargetId = e.choicePlayer() == MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER
                && e.wrapped() instanceof DrawCardForTargetPlayerEffect
                ? choicePlayerId : targetId;
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                defendingPlayerChoice ? entry.getControllerId() : choicePlayerId,
                List.of(e.wrapped()),
                entry.getCard().getName() + " - " + e.prompt(),
                pendingTargetId,
                null,
                entry.getSourcePermanentId(),
                null,
                0,
                0,
                attackedTargetForPendingMay(gameData, entry),
                e.choicePlayer() == MayChoicePlayer.ACTIVE_PLAYER ? entry.getActivePlayerId() : null,
                defendingPlayerChoice ? choicePlayerId : null,
                entry.getSourcePermanentSnapshot(),
                e.choicePlayer() == MayChoicePlayer.TRIGGERING_PERMANENT_CONTROLLER
                        ? choicePlayerId : entry.getTriggeringPermanentControllerId(),
                entry.getTriggeringCardId(),
                entry.getEventValue(),
                entry.getTriggeringPermanentId(),
                entry.getTriggeringPermanentPowerAtTrigger(),
                null,
                entry.getTriggeringPermanentToughnessAtTrigger(),
                entry.getTriggeringCardGraveyardEntryVersion()
        ));
    }

    /**
     * CR 508.5: the defending player stays the player the creature attacked even after an attacked
     * planeswalker or battle has left the battlefield, so fall back to the player recorded at trigger time.
     */
    private UUID attackedTargetForPendingMay(GameData gameData, StackEntry entry) {
        UUID attacked = entry.getAttackedTargetId();
        if (attacked != null && entry.getDefendingPlayerId() != null
                && !gameData.playerIds.contains(attacked)
                && gameQueryService.findPermanentById(gameData, attacked) == null) {
            return entry.getDefendingPlayerId();
        }
        return attacked;
    }

    private void insertElseEffect(StackEntry entry, MayEffect may) {
        if (may.elseEffect() != null) {
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, List.of(may.elseEffect()));
        }
    }

    private boolean controlsBlightableCreature(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return false;
        }
        return battlefield.stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .anyMatch(permanent -> !gameQueryService.cantHaveCounters(gameData, permanent)
                        && !gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, permanent)
                        && !gameQueryService.cantBeChosen(gameData, permanent));
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
