package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.PendingTokenCreationReplacement;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfChosenCreatureEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Creates token copies of the permanents the effect's subject names. A target is read as the entry recorded it; every
 * other subject through {@link SubjectResolutionService}: "that creature" or "one of them" ({@code triggering()}),
 * each of them ({@code triggeringAll()}), the source's host ({@code attached()}), or one named permanent. A named
 * permanent that has left the battlefield is copied as it last existed (CR 608.2h, 111.12).
 */
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;
    private final com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService permanentCopierService;
    private final AmountEvaluationService amountEvaluationService;
    private final EsixTokenReplacementSupport esixTokenReplacementSupport;
    private final ObjectProvider<CreateTokenCopyOfChosenCreatureEffectHandler> chosenCreatureCopyHandler;
    private final SubjectResolutionService subjectResolutionService;
    private final com.github.laxika.magicalvibes.service.input.PlayerInputService playerInputService;

    static Card buildTokenCopyCard(Card sourceCard, CreateTokenCopyOfTargetPermanentEffect effect) {
        return TokenCopySupport.buildTokenCopyCard(sourceCard, effect);
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var copyEffect = (CreateTokenCopyOfTargetPermanentEffect) effect;
        PermanentSubject subject = copyEffect.subject();

        boolean oneOfSeveral = subject instanceof PermanentSubject.Triggering
                || subject instanceof PermanentSubject.Target && entry.getTargetIds().isEmpty();
        if (oneOfSeveral) {
            List<UUID> group = triggeringPermanentIds(gameData, entry, effect);
            if (group.size() > 1) {
                List<UUID> candidates = group.stream()
                        .filter(id -> gameQueryService.findPermanentById(gameData, id) != null)
                        .toList();
                if (candidates.size() > 1) {
                    gameData.interaction.setPermanentChoiceContext(
                            new com.github.laxika.magicalvibes.model.PermanentChoiceContext
                                    .CreateTokenCopyOfOneOfTriggeringPermanents());
                    playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), candidates,
                            "Choose one of the creatures that entered to create a token copy of.");
                    return;
                }
            }
        }

        List<UUID> targetIds = switch (subject) {
            case PermanentSubject.Target ignored -> targetIds(entry, copyEffect);
            case PermanentSubject.Triggering ignored -> triggeringPermanentToCopy(gameData, entry, effect);
            default -> subjectResolutionService.permanentIds(gameData, entry, effect, subject);
        };
        resolveCopies(gameData, entry, copyEffect, targetIds, true);
    }

    /**
     * Creates {@code copyEffect}'s tokens for each of {@code targetIds}; {@code offerReplacement} offers a "first time
     * you would create tokens" replacement (Esix, Mirrormind Crown, Moonlit Meditation) first.
     */
    private void resolveCopies(GameData gameData, StackEntry entry, CreateTokenCopyOfTargetPermanentEffect copyEffect,
                               List<UUID> targetIds, boolean offerReplacement) {
        Permanent sourcePermanent = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int copyCount = amountEvaluationService.evaluate(
                gameData, copyEffect.amount(), AmountContext.forStackEntry(entry, sourcePermanent));
        if (copyCount <= 0) {
            return;
        }

        if (offerReplacement && resolveEsixReplacement(gameData, entry, copyEffect, targetIds, copyCount)) {
            return;
        }

        if (targetIds.size() > 1 && !copyEffect.chooseAttackTarget() && !copyEffect.createForTargetController()
                && !copyEffect.tappedAndAttacking()) {
            // One instruction ("for each of X target permanents, create X tokens"): every token enters
            // as one event, so token replacement effects only see permanents already on the battlefield
            // (a Doubling Season token created by this very spell doesn't double its siblings).
            List<Card> copySources = new java.util.ArrayList<>();
            for (UUID targetId : targetIds) {
                Permanent targetPermanent = gameQueryService.findPermanentById(gameData, targetId);
                if (targetPermanent != null) {
                    copySources.addAll(Collections.nCopies(copyCount, copySourceFor(targetPermanent)));
                    continue;
                }
                Card lastKnownCard = lastKnownCopySource(gameData, entry, copyEffect, targetId);
                if (lastKnownCard != null) {
                    copySources.addAll(Collections.nCopies(copyCount, lastKnownCard));
                }
            }
            tokenCopySupport.createTokenCopies(gameData, entry, copySources, sourcePermanent,
                    entry.getControllerId(), copyEffect, null);
            return;
        }
        for (UUID targetId : targetIds) {
            resolveForTarget(gameData, entry, copyEffect, targetId, copyCount);
        }
    }

    private static List<UUID> targetIds(StackEntry entry, CreateTokenCopyOfTargetPermanentEffect copyEffect) {
        List<UUID> targetIds = entry.targetsForBoundEffectGroup(copyEffect);
        if (targetIds == null) {
            return !entry.getTargetIds().isEmpty()
                    ? entry.getTargetIds()
                    : !entry.getTargetCardIds().isEmpty()
                    ? entry.getTargetCardIds()
                    : entry.getTargetId() == null ? List.of() : List.of(entry.getTargetId());
        }
        if (targetIds.isEmpty() && entry.getDeclaredTargetIds().isEmpty() && entry.getTargetId() != null) {
            return List.of(entry.getTargetId());
        }
        return targetIds;
    }

    /**
     * The permanents the trigger event was about, recorded on the entry: the group of a "one or more" event, else its
     * one permanent.
     */
    private List<UUID> triggeringPermanentIds(GameData gameData, StackEntry entry, CardEffect effect) {
        return subjectResolutionService.permanentIds(gameData, entry, effect, Subjects.triggeringAll());
    }

    /**
     * "That creature", or "one of them" when only one of the event's permanents is still on the battlefield: that one;
     * with none left, the first, copied as it last existed on the battlefield (CR 608.2h).
     */
    private List<UUID> triggeringPermanentToCopy(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> ids = triggeringPermanentIds(gameData, entry, effect);
        return ids.stream()
                .filter(id -> gameQueryService.findPermanentById(gameData, id) != null)
                .findFirst()
                .or(() -> ids.stream().findFirst())
                .map(List::of)
                .orElse(List.of());
    }

    /** Copies the creature chosen from the group that triggered the paused ability. */
    public void completeTriggeringPermanentChoice(GameData gameData, UUID chosenPermanentId) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            throw new IllegalStateException("No effect is waiting for a creature choice");
        }
        CreateTokenCopyOfTargetPermanentEffect copyEffect = entry.getEffectsToResolve().stream()
                .filter(CreateTokenCopyOfTargetPermanentEffect.class::isInstance)
                .map(CreateTokenCopyOfTargetPermanentEffect.class::cast)
                .findFirst().orElseThrow();
        if (!triggeringPermanentIds(gameData, entry, copyEffect).contains(chosenPermanentId)
                || gameQueryService.findPermanentById(gameData, chosenPermanentId) == null) {
            throw new IllegalStateException("Choose a creature that entered");
        }
        Permanent sourcePermanent = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int copyCount = amountEvaluationService.evaluate(
                gameData, copyEffect.amount(), AmountContext.forStackEntry(entry, sourcePermanent));
        resolveForTarget(gameData, entry, copyEffect, chosenPermanentId, copyCount);
    }

    /**
     * Mirrormind Crown, Moonlit Meditation: creates {@code amount} token copies of the creature or permanent
     * {@code attachmentId} is attached to, instead of the tokens the replaced effect would have created; nothing when
     * that Aura or Equipment is no longer attached to a permanent. Offers no replacement of its own.
     */
    public void createCopiesOfHost(GameData gameData, StackEntry entry, UUID attachmentId, int amount) {
        createCopiesOfHost(gameData, entry, attachmentId, amount, entry.getControllerId());
    }

    /**
     * {@link #createCopiesOfHost(GameData, StackEntry, UUID, int)} for the tokens {@code tokenControllerId} would have
     * created: that player creates the copies ("its controller creates a token" with that player's Mirrormind Crown).
     */
    public void createCopiesOfHost(GameData gameData, StackEntry entry, UUID attachmentId, int amount,
                                   UUID tokenControllerId) {
        Permanent attachment = gameQueryService.findPermanentById(gameData, attachmentId);
        UUID hostId = attachment == null ? null : attachment.getAttachedTo();
        if (hostId == null || gameData.playerIds.contains(hostId)) {
            return;
        }
        CreateTokenCopyOfTargetPermanentEffect copyEffect =
                new CreateTokenCopyOfTargetPermanentEffect(new Fixed(amount)).ofNamed(hostId);
        if (tokenControllerId.equals(entry.getControllerId())) {
            resolveCopies(gameData, entry, copyEffect,
                    subjectResolutionService.permanentIds(gameData, entry, copyEffect, copyEffect.subject()), false);
            return;
        }
        StackEntry copyEntry = new StackEntry(entry.getCard(), tokenControllerId);
        resolveCopies(gameData, copyEntry, copyEffect,
                subjectResolutionService.permanentIds(gameData, copyEntry, copyEffect, copyEffect.subject()), false);
        entry.getCreatedPermanentIds().addAll(copyEntry.getCreatedPermanentIds());
    }

    /**
     * Esix, Fractal Bloom: the first token creation of the controller's turn may instead copy a chosen
     * creature. The offer parks the creation in {@code pendingTokenCreationReplacement}; this effect is
     * re-run once the optional replacement is answered and continues normally when it was declined.
     *
     * @return {@code true} when the effect has been fully handled by the replacement or is waiting for it
     */
    private boolean resolveEsixReplacement(GameData gameData, StackEntry entry,
                                           CreateTokenCopyOfTargetPermanentEffect copyEffect,
                                           List<UUID> targetIds, int copyCount) {
        PendingTokenCreationReplacement pending = gameData.pendingTokenCreationReplacement;
        if (pending != null) {
            gameData.pendingTokenCreationReplacement = null;
            Boolean accepted = gameData.resolvedMayAccepted;
            gameData.resolvedMayAccepted = null;
            if (!Boolean.TRUE.equals(accepted)) {
                return false;
            }
            if (pending.copyChosenCreature()) {
                chosenCreatureCopyHandler.getObject().resolve(gameData, entry,
                        new CreateTokenCopyOfChosenCreatureEffect(pending.amount(), pending.replacementPermanentId()));
            } else {
                createCopiesOfHost(gameData, entry, pending.replacementPermanentId(), pending.amount());
            }
            return true;
        }
        if (copyEffect.chooseAttackTarget() || copyEffect.createForTargetController()
                || copyEffect.tappedAndAttacking()) {
            return false;
        }
        long existingTargets = targetIds.stream()
                .filter(targetId -> gameQueryService.findPermanentById(gameData, targetId) != null)
                .count();
        if (existingTargets == 0) {
            return false;
        }
        int replacedAmount = (int) (copyCount * existingTargets);
        return esixTokenReplacementSupport.offerMirrormindCrown(gameData, entry.getControllerId(), replacedAmount)
                || esixTokenReplacementSupport.offerMoonlitMeditation(gameData, entry.getControllerId(), replacedAmount)
                || esixTokenReplacementSupport.offer(gameData, entry.getControllerId(), replacedAmount, 0, 0);
    }

    /**
     * The card a token copy of {@code targetPermanent} copies: its copiable values, or the original card of a
     * transformed daybound permanent.
     */
    Card copySourceFor(Permanent targetPermanent) {
        return targetPermanent.isTransformed()
                && targetPermanent.getOriginalCard().getBackFaceCard() != null
                && targetPermanent.getOriginalCard().hasKeyword(com.github.laxika.magicalvibes.model.Keyword.DAYBOUND)
                ? targetPermanent.getOriginalCard() : permanentCopierService.copiableCard(targetPermanent);
    }

    void resolveForTarget(GameData gameData, StackEntry entry,
                          CreateTokenCopyOfTargetPermanentEffect effect, UUID targetId) {
        resolveForTarget(gameData, entry, effect, targetId, 1);
    }

    void resolveForTarget(GameData gameData, StackEntry entry,
                          CreateTokenCopyOfTargetPermanentEffect effect, UUID targetId, int copyCount) {
        Permanent targetPermanent = gameQueryService.findPermanentById(gameData, targetId);
        if (targetPermanent == null) {
            resolveForDepartedTarget(gameData, entry, effect, targetId, copyCount);
            return;
        }
        Permanent sourcePermanent = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        UUID tokenControllerId = entry.getControllerId();
        if (effect.createForTargetController()) {
            UUID targetControllerId = gameQueryService.findPermanentController(gameData, targetId);
            if (targetControllerId != null) {
                tokenControllerId = targetControllerId;
            }
        }
        if (effect.chooseAttackTarget()) {
            tokenCopySupport.createTokenCopiesChoosingAttackTargets(gameData, entry,
                    Collections.nCopies(copyCount, copySourceFor(targetPermanent)), sourcePermanent,
                    tokenControllerId, effect);
            return;
        }
        UUID attackTargetId = effect.tappedAndAttacking()
                ? entry.getAttackedTargetId() != null
                ? entry.getAttackedTargetId()
                : targetPermanent.getAttackTarget()
                : null;
        List<UUID> attackTargetIds = attackTargetId == null ? null : List.of(attackTargetId);
        Card copySource = copySourceFor(targetPermanent);
        tokenCopySupport.createTokenCopies(gameData, entry, Collections.nCopies(copyCount, copySource),
                sourcePermanent, tokenControllerId, effect, attackTargetIds);
    }
    /**
     * Copies a target that left the battlefield before this resolution, using the card it had as it last
     * existed on the battlefield (CR 608.2h). Choices that depend on a live target are skipped.
     */
    private void resolveForDepartedTarget(GameData gameData, StackEntry entry,
                                          CreateTokenCopyOfTargetPermanentEffect effect, UUID targetId, int copyCount) {
        Card lastKnownCard = lastKnownCopySource(gameData, entry, effect, targetId);
        if (lastKnownCard == null || effect.chooseAttackTarget()) {
            return;
        }
        UUID tokenControllerId = entry.getControllerId();
        if (effect.createForTargetController()) {
            tokenControllerId = entry.getRemovedPermanentControllers().get(targetId);
            if (tokenControllerId == null) {
                return;
            }
        }
        Permanent sourcePermanent = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        UUID attackTargetId = effect.tappedAndAttacking() ? entry.getAttackedTargetId() : null;
        List<UUID> attackTargetIds = attackTargetId == null ? null : List.of(attackTargetId);
        tokenCopySupport.createTokenCopies(gameData, entry, Collections.nCopies(copyCount, lastKnownCard),
                sourcePermanent, tokenControllerId, effect, attackTargetIds);
    }

    /**
     * The card a token copy of {@code permanentId}, which has left the battlefield, copies: the one the entry recorded
     * as it last existed, else, for a permanent the effect doesn't target, the last-known snapshot the game kept of it
     * (CR 608.2h).
     */
    private Card lastKnownCopySource(GameData gameData, StackEntry entry, CreateTokenCopyOfTargetPermanentEffect effect,
                                     UUID permanentId) {
        Card lastKnownCard = PermanentCopierService.lastKnownCardToCopy(gameData, entry, permanentId,
                entry.lastKnownPermanentCard(permanentId));
        if (lastKnownCard != null || effect.subject() instanceof PermanentSubject.Target) {
            return lastKnownCard;
        }
        return gameData.departedPermanentSnapshots.values().stream()
                .filter(departed -> permanentId.equals(departed.getId()))
                .findFirst()
                .map(permanentCopierService::copiableCard)
                .orElse(null);
    }
}
