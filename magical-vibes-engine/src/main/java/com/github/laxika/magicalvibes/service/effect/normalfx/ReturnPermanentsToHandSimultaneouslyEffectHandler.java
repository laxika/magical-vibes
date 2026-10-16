package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentsToHandSimultaneouslyEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Chooses permanents one at a time and returns them all to their owners' hands together. */
@Component
@RequiredArgsConstructor
public class ReturnPermanentsToHandSimultaneouslyEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnPermanentsToHandSimultaneouslyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnPermanentsToHandSimultaneouslyEffect) effect;
        continueChoosing(gameData, new PermanentChoiceContext.BounceBatch(
                entry.getControllerId(), entry.getCard(), e.filter(), e.noun(), e.count(), List.of()));
    }

    /** Records one chosen permanent and either asks for the next or returns the whole batch. */
    public void completeChoice(GameData gameData, UUID permanentId, PermanentChoiceContext.BounceBatch context) {
        List<UUID> chosen = new ArrayList<>(context.chosenIds());
        chosen.add(permanentId);
        continueChoosing(gameData, new PermanentChoiceContext.BounceBatch(
                context.controllerId(), context.sourceCard(), context.filter(), context.noun(),
                context.count(), chosen));
    }

    private void continueChoosing(GameData gameData, PermanentChoiceContext.BounceBatch context) {
        List<UUID> candidates = context.chosenIds().size() >= context.count()
                ? List.of() : candidates(gameData, context);
        if (!candidates.isEmpty()) {
            gameData.interaction.setPermanentChoiceContext(context);
            playerInputService.beginPermanentChoice(gameData, context.controllerId(), candidates,
                    "Choose a " + context.noun() + " to return to its owner's hand.");
            return;
        }
        List<Card> returned = new ArrayList<>();
        for (UUID id : context.chosenIds()) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, id);
            if (permanent != null && permanentRemovalService.removePermanentToHand(gameData, permanent)) {
                returned.add(permanent.getCard());
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
        if (!returned.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(context.sourceCard(),
                    " returns " + returned.size() + " " + context.noun()
                            + (returned.size() == 1 ? "" : "s") + " to their owners' hands."));
        }
    }

    private List<UUID> candidates(GameData gameData, PermanentChoiceContext.BounceBatch context) {
        PermanentPredicate filter = context.filter();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(context.sourceCard() != null ? context.sourceCard().getId() : null)
                .withSourceControllerId(context.controllerId());
        List<UUID> result = new ArrayList<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(context.controllerId(), List.of())) {
            if (!context.chosenIds().contains(permanent.getId())
                    && predicateEvaluationService.matchesPermanentPredicate(permanent, filter, filterContext)) {
                result.add(permanent.getId());
            }
        }
        return result;
    }
}
