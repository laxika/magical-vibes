package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentAndReturnTargetCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves a resolution-time sacrifice followed by a conditional graveyard return. */
@Slf4j
@Component
@RequiredArgsConstructor
public class SacrificePermanentAndReturnTargetCardsFromGraveyardEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final ReturnTargetCardsFromGraveyardToBattlefieldEffectHandler returnHandler;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificePermanentAndReturnTargetCardsFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SacrificePermanentAndReturnTargetCardsFromGraveyardEffect) effect;
        UUID controllerId = entry.getControllerId();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(controllerId);

        List<UUID> validIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(
                        permanent, e.sacrificeFilter(), filterContext)) {
                    validIds.add(permanent.getId());
                }
            }
        }

        if (validIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId) + " has no "
                            + e.permanentDescription() + " to sacrifice."));
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.SacrificePermanentAndReturnTargetCards(
                        controllerId, entry.getCard(), e));
        playerInputService.beginPermanentChoice(gameData, controllerId, validIds,
                entry.getCard().getName() + " — Choose " + e.permanentDescription() + " to sacrifice.");
    }

    public void resolveAfterChoice(GameData gameData, UUID permanentId,
                                   PermanentChoiceContext.SacrificePermanentAndReturnTargetCards context) {
        Permanent toSacrifice = gameQueryService.findPermanentById(gameData, permanentId);
        if (toSacrifice == null) {
            throw new IllegalStateException("Chosen permanent no longer exists");
        }

        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            throw new IllegalStateException("No pending resolution for sacrifice choice");
        }

        if (!permanentRemovalService.removePermanentToGraveyard(gameData, toSacrifice)) {
            return;
        }

        Card sacrificedCard = toSacrifice.getCard();
        triggerCollectionService.checkAllyPermanentSacrificedTriggers(
                gameData, context.controllerId(), sacrificedCard);
        gameLogService.append(gameData, GameLog.cardThen(sacrificedCard, " is sacrificed."));
        permanentRemovalService.removeOrphanedAuras(gameData);

        var e = context.effect();
        returnHandler.resolve(gameData, entry,
                new ReturnTargetCardsFromGraveyardToBattlefieldEffect(
                        e.returnFilter(), e.targetCount(), false, e.enterTapped()));
    }
}
