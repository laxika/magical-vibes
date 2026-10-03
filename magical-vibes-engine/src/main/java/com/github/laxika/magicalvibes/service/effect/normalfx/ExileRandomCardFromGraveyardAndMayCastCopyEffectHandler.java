package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileRandomCardFromGraveyardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastCopyWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves a random graveyard exile followed by an optional free cast of its copy. */
@Component
@RequiredArgsConstructor
public class ExileRandomCardFromGraveyardAndMayCastCopyEffectHandler
        implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final CopySupport copySupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileRandomCardFromGraveyardAndMayCastCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var randomEffect = (ExileRandomCardFromGraveyardAndMayCastCopyEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> candidates = gameData.playerGraveyards.getOrDefault(controllerId, List.of()).stream()
                .filter(card -> randomEffect.filter() == null
                        || predicateEvaluationService.matchesCardPredicate(
                        card, randomEffect.filter(), entry.getCard().getId(), gameData,
                        controllerId, entry.getSourcePermanentId(),
                        entry.getTriggeringPermanentPowerAtTrigger()))
                .toList();
        if (candidates.isEmpty()) {
            return;
        }

        Card exiled = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, exiled.getId());
        exileService.exileCard(gameData, controllerId, exiled);
        gameLogService.append(gameData, GameLog.isExiled(exiled));

        Card copy = copySupport.createCopyCard(exiled);
        exileService.exileCard(gameData, controllerId, copy);
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                copy,
                controllerId,
                List.of(new MayCastCopyWithoutPayingManaCostEffect()),
                "Cast the copy of " + copy.getName() + " without paying its mana cost?",
                copy.getId(),
                null,
                entry.getSourcePermanentId()));
    }
}
