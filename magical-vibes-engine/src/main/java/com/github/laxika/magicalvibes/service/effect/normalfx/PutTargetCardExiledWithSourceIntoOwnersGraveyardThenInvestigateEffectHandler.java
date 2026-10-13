package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardExiledWithSourceIntoOwnersGraveyardThenInvestigateEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PutTargetCardExiledWithSourceIntoOwnersGraveyardThenInvestigateEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final PermanentControlSupport permanentControlSupport;
    private final PredicateEvaluationService predicateEvaluationService;
    @Lazy
    private final TriggerCollectionService triggerCollectionService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetCardExiledWithSourceIntoOwnersGraveyardThenInvestigateEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var putEffect = (PutTargetCardExiledWithSourceIntoOwnersGraveyardThenInvestigateEffect) effect;
        UUID targetId = entry.getTargetId();
        if (putEffect.chooseAtResolution()) {
            UUID sourceId = sourcePermanentId(entry);
            List<UUID> available = gameData.orderedPlayerIds.stream()
                    .flatMap(owner -> gameData.getPlayerExiledCards(owner).stream())
                    .map(card -> gameData.findExiledCard(card.getId()))
                    .filter(exiled -> exiled != null && !exiled.faceDown()
                            && sourceId != null && sourceId.equals(exiled.sourcePermanentId()))
                    .map(exiled -> exiled.card().getId())
                    .filter(id -> putEffect.filter() == null || matchesFilter(gameData, entry, putEffect, id))
                    .toList();
            if (available.isEmpty()) return;
            if (available.size() > 1) {
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.ExiledCardChoice(
                        entry.getControllerId(), available, entry.getCard().getName(), entry, effect));
                return;
            }
            targetId = available.getFirst();
        }
        putChosenCardIntoGraveyard(gameData, entry, putEffect, targetId);
    }

    public void putChosenCardIntoGraveyard(GameData gameData, StackEntry entry,
            PutTargetCardExiledWithSourceIntoOwnersGraveyardThenInvestigateEffect putEffect, UUID targetId) {
        ExiledCardEntry exiled = targetId == null ? null : gameData.findExiledCard(targetId);
        UUID sourcePermanentId = sourcePermanentId(entry);
        if (exiled == null || sourcePermanentId == null
                || !sourcePermanentId.equals(exiled.sourcePermanentId())
                || exiled.faceDown()
                || (putEffect.filter() != null && !matchesFilter(gameData, entry, putEffect, targetId))) {
            return;
        }
        if (!gameData.removeFromExile(targetId)) {
            return;
        }
        graveyardService.addCardToGraveyard(gameData, exiled.ownerId(), exiled.card(), Zone.EXILE);
        gameLogService.append(gameData, GameLog.cardThen(exiled.card(),
                " is put into its owner's graveyard."));

        permanentControlSupport.applyCreateToken(gameData, entry.getControllerId(),
                CreateTokenEffect.ofClueToken(1), entry.getCard().getSetCode());
        triggerCollectionService.checkInvestigateTriggers(gameData, entry.getControllerId());
    }

    private UUID sourcePermanentId(StackEntry entry) {
        if (entry.getSourcePermanentId() != null) {
            return entry.getSourcePermanentId();
        }
        return entry.getSourcePermanentSnapshot() == null ? null : entry.getSourcePermanentSnapshot().getId();
    }

    private boolean matchesFilter(GameData gameData, StackEntry entry,
            PutTargetCardExiledWithSourceIntoOwnersGraveyardThenInvestigateEffect putEffect, UUID cardId) {
        ExiledCardEntry exiled = gameData.findExiledCard(cardId);
        return exiled != null && predicateEvaluationService.matchesCardPredicate(
                exiled.card(), putEffect.filter(), entry.getCard().getId(), gameData, exiled.ownerId());
    }
}
