package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link CreateTokenCopyOfCardEffect}: the copies of every card its subject names (or of its fixed or
 * triggering card) are created as one ordered token creation event (CR 616.1). A card the subject no longer finds where
 * it was put (CR 400.7) is not copied, so "exile it. Create a token that's a copy of it" copies nothing when nothing was
 * exiled. A card in exile face down has no characteristics to copy (CR 406.3a).
 */
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfCardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final TokenCopySupport tokenCopySupport;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var copyEffect = (CreateTokenCopyOfCardEffect) effect;
        CreateTokenCopyOfTargetPermanentEffect options = copyEffect.tokenCopyEffect();
        Permanent sourcePermanent = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        int copyCount = amountEvaluationService.evaluate(
                gameData, options.amount(), AmountContext.forStackEntry(entry, sourcePermanent));
        if (copyCount <= 0) {
            return;
        }
        if (copyEffect.cards() != null) {
            copyNamedCards(gameData, entry, copyEffect, copyCount);
            return;
        }
        Card sourceCard = copyEffect.sourceCard();
        if (sourceCard == null && entry.getTriggeringCardId() != null) {
            sourceCard = gameQueryService.findCardById(gameData, entry.getTriggeringCardId());
        }
        if (sourceCard == null) {
            return;
        }
        UUID tokenControllerId = tokenController(gameData, entry, options);
        tokenCopySupport.createTokenCopiesWithCopyException(gameData, entry,
                Collections.nCopies(copyCount, sourceCard),
                sourcePermanent, tokenControllerId, options, creatureTypesInstead(copyEffect));
    }

    /**
     * The copies of the cards the subject names, for the effect's controller. "Tapped and attacking" copies attack a
     * player or planeswalker their controller chooses as they enter when the options say so (CR 508.4), else what the
     * source attacks (as the ability resolves, else as the entry recorded the attack).
     */
    private void copyNamedCards(GameData gameData, StackEntry entry, CreateTokenCopyOfCardEffect copyEffect,
                                int copyCount) {
        List<Card> named = subjectResolutionService.cards(gameData, entry, copyEffect, copyEffect.cards());
        if (named.isEmpty()) {
            return;
        }
        List<Card> sourceCards = new ArrayList<>();
        for (Card card : named) {
            sourceCards.addAll(Collections.nCopies(copyCount, copiedValues(gameData, card)));
        }
        CreateTokenCopyOfTargetPermanentEffect options = copyEffect.tokenCopyEffect();
        if (options.tappedAndAttacking() && options.chooseAttackTarget()) {
            tokenCopySupport.createTokenCopiesChoosingAttackTargets(gameData, entry, sourceCards, null,
                    entry.getControllerId(), options);
            return;
        }
        List<UUID> attackTargetIds = null;
        if (options.tappedAndAttacking() && !options.chooseAttackTarget()) {
            Permanent source = entry.getSourcePermanentId() == null
                    ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            UUID attackTargetId = source == null ? entry.getAttackedTargetId() : source.getAttackTarget();
            attackTargetIds = attackTargetId == null ? null : List.of(attackTargetId);
        }
        tokenCopySupport.createTokenCopies(gameData, entry, sourceCards, null,
                tokenController(gameData, entry, options), options, attackTargetIds, creatureTypesInstead(copyEffect));
    }

    /**
     * What a copy of {@code card} copies: nothing of a card in exile face down, which has no characteristics
     * (CR 406.3a), else the card.
     */
    private static Card copiedValues(GameData gameData, Card card) {
        return gameData.isExiledFaceDown(card.getId()) ? Card.namedRuntimePlaceholder("") : card;
    }

    /** "That player creates": the target player or the target permanent's controller; else the effect's controller. */
    private UUID tokenController(GameData gameData, StackEntry entry, CreateTokenCopyOfTargetPermanentEffect options) {
        if (options.createForTargetController() && entry.getTargetId() != null) {
            Permanent targetPermanent = gameQueryService.findPermanentById(gameData, entry.getTargetId());
            UUID targetControllerId = targetPermanent == null
                    ? gameData.playerIds.contains(entry.getTargetId()) ? entry.getTargetId() : null
                    : gameQueryService.findPermanentController(gameData, entry.getTargetId());
            if (targetControllerId != null) {
                return targetControllerId;
            }
        }
        return entry.getControllerId();
    }

    private Consumer<Card> creatureTypesInstead(CreateTokenCopyOfCardEffect copyEffect) {
        return copyEffect.creatureTypesInstead().isEmpty() ? null
                : tokenCard -> tokenCopySupport.replaceCreatureTypes(tokenCard, copyEffect.creatureTypesInstead());
    }
}
