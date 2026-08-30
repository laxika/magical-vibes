package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfChosenPermanentYouControlEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the non-targeted permanent-copy choice on Awaken the Maelstrom. */
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfChosenPermanentYouControlEffectHandler implements NormalEffectHandlerBean {

    private static final CreateTokenCopyOfTargetPermanentEffect TOKEN_PROFILE =
            new CreateTokenCopyOfTargetPermanentEffect();

    private final CreateTokenCopyOfTargetPermanentEffectHandler tokenCopyHandler;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfChosenPermanentYouControlEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var copyEffect = (CreateTokenCopyOfChosenPermanentYouControlEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<UUID> permanentIds = controlledPermanentIds(gameData, controllerId, entry.getSourcePermanentId(),
                copyEffect.filter());

        if (permanentIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    " resolves but its controller controls no permanent to copy."));
        } else if (permanentIds.size() == 1) {
            createCopy(gameData, controllerId, entry.getCard(), permanentIds.getFirst());
        } else {
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.ChosenPermanentCopyChoice(
                            controllerId, entry.getCard(), copyEffect.filter()));
            playerInputService.beginPermanentChoice(gameData, controllerId, permanentIds,
                    entry.getCard().getName() + " - Choose a permanent you control to copy.");
        }
    }

    private List<UUID> controlledPermanentIds(GameData gameData, UUID controllerId, UUID sourcePermanentId,
                                               PermanentPredicate filter) {
        FilterContext context = FilterContext.of(gameData)
                .withSourceControllerId(controllerId)
                .withSourcePermanentId(sourcePermanentId);
        return gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                .filter(permanent -> filter == null
                        || predicateEvaluationService.matchesPermanentPredicate(permanent, filter, context))
                .map(Permanent::getId)
                .toList();
    }

    public void completeChoice(GameData gameData, UUID permanentId,
                               PermanentChoiceContext.AwakenTheMaelstromPermanentCopyChoice context) {
        createCopy(gameData, context.controllerId(), context.sourceCard(), permanentId);
    }

    public void completeChoice(GameData gameData, UUID permanentId,
                               PermanentChoiceContext.ChosenPermanentCopyChoice context) {
        if (!controlledPermanentIds(gameData, context.controllerId(), null, context.filter()).contains(permanentId)) {
            return;
        }
        createCopy(gameData, context.controllerId(), context.sourceCard(), permanentId);
    }

    private void createCopy(GameData gameData, UUID controllerId, Card sourceCard, UUID permanentId) {
        StackEntry copyEntry = new StackEntry(sourceCard, controllerId);
        copyEntry.setTargetId(permanentId);
        tokenCopyHandler.resolve(gameData, copyEntry, TOKEN_PROFILE);
    }
}
