package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.PendingTokenCreationReplacement;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfChosenCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfTargetPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;
    private final com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService permanentCopierService;
    private final AmountEvaluationService amountEvaluationService;
    private final EsixTokenReplacementSupport esixTokenReplacementSupport;
    private final ObjectProvider<CreateTokenCopyOfChosenCreatureEffectHandler> chosenCreatureCopyHandler;
    private final ObjectProvider<CreateTokenCopyOfEquippedCreatureEffectHandler> equippedCreatureCopyHandler;
    private final ObjectProvider<CreateTokenCopyOfEnchantedPermanentEffectHandler> enchantedPermanentCopyHandler;

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

        List<UUID> targetIds = entry.targetsForBoundEffectGroup(copyEffect);
        if (targetIds == null) {
            targetIds = !entry.getTargetIds().isEmpty()
                    ? entry.getTargetIds()
                    : !entry.getTargetCardIds().isEmpty()
                    ? entry.getTargetCardIds()
                     : entry.getTargetId() == null ? List.of() : List.of(entry.getTargetId());
        } else if (targetIds.isEmpty() && entry.getDeclaredTargetIds().isEmpty()
                && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        Permanent sourcePermanent = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int copyCount = amountEvaluationService.evaluate(
                gameData, copyEffect.amount(), AmountContext.forStackEntry(entry, sourcePermanent));
        if (copyCount <= 0) {
            return;
        }

        if (resolveEsixReplacement(gameData, entry, copyEffect, targetIds, copyCount)) {
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
                Card lastKnownCard = entry.lastKnownPermanentCard(targetId);
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
            } else if (pending.copyEnchantedPermanent()) {
                enchantedPermanentCopyHandler.getObject().resolve(gameData, entry,
                        new CreateTokenCopyOfEnchantedPermanentEffect(
                                pending.amount(), pending.replacementPermanentId()));
            } else {
                equippedCreatureCopyHandler.getObject().resolve(gameData, entry,
                        new CreateTokenCopyOfEquippedCreatureEffect(
                                pending.amount(), false, false, pending.replacementPermanentId()));
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

    private Card copySourceFor(Permanent targetPermanent) {
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
        Card lastKnownCard = entry.lastKnownPermanentCard(targetId);
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
}
