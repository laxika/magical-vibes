package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentChoosesPermanentToDestroyEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves non-targeting per-opponent permanent choices before destroying them simultaneously. */
@Component
@RequiredArgsConstructor
public class EachOpponentChoosesPermanentToDestroyEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentChoosesPermanentToDestroyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var destroyEffect = (EachOpponentChoosesPermanentToDestroyEffect) effect;
        beginNextOpponent(gameData, entry.getControllerId(), entry.getCard(),
                apnapOpponents(gameData, entry.getControllerId()), List.of(), destroyEffect.filter());
    }

    public void beginNextOpponent(GameData gameData, UUID controllerId, Card sourceCard,
            List<UUID> remainingOpponentIds, List<UUID> chosenPermanentIds, PermanentPredicate filter) {
        List<UUID> remaining = new ArrayList<>(remainingOpponentIds);
        List<UUID> chosen = new ArrayList<>(chosenPermanentIds);

        while (!remaining.isEmpty()) {
            UUID opponentId = remaining.removeFirst();
            List<Permanent> candidates = matchingPermanents(gameData, opponentId, filter);
            if (candidates.isEmpty()) {
                continue;
            }

            if (candidates.size() == 1) {
                chosen.add(candidates.getFirst().getId());
                continue;
            }

            PermanentChoiceContext.EachOpponentChoosesPermanentToDestroy context =
                    new PermanentChoiceContext.EachOpponentChoosesPermanentToDestroy(
                            controllerId, sourceCard, opponentId, List.copyOf(remaining),
                            List.copyOf(chosen), filter);
            gameData.interaction.setPermanentChoiceContext(context);
            playerInputService.beginPermanentChoice(gameData, controllerId,
                    candidates.stream().map(Permanent::getId).toList(), context,
                    sourceCard.getName() + " — Choose an artifact or land controlled by the opponent to destroy.");
            return;
        }

        destroyChosen(gameData, chosen, sourceCard.getName());
    }

    public void completeChoice(GameData gameData, UUID permanentId,
            PermanentChoiceContext.EachOpponentChoosesPermanentToDestroy context) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosen == null
                || !context.opponentId().equals(gameQueryService.findPermanentController(gameData, permanentId))
                || !predicateEvaluationService.matchesPermanentPredicate(gameData, chosen, context.filter())) {
            throw new IllegalStateException(
                    "Chosen permanent is no longer a matching permanent controlled by the opponent");
        }

        List<UUID> chosenPermanentIds = new ArrayList<>(context.chosenPermanentIds());
        chosenPermanentIds.add(permanentId);
        beginNextOpponent(gameData, context.controllerId(), context.sourceCard(),
                context.remainingOpponentIds(), chosenPermanentIds, context.filter());
    }

    private List<Permanent> matchingPermanents(GameData gameData, UUID playerId, PermanentPredicate filter) {
        return gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, filter))
                .toList();
    }

    private void destroyChosen(GameData gameData, List<UUID> chosenPermanentIds, String sourceName) {
        List<Permanent> chosen = chosenPermanentIds.stream()
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .filter(permanent -> permanent != null)
                .toList();
        if (!chosen.isEmpty()) {
            destructionSupport.destroyBatch(gameData, chosen, sourceName, false);
        }
    }

    private List<UUID> apnapOpponents(GameData gameData, UUID controllerId) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        List<UUID> rotated = new ArrayList<>();
        if (activeIndex > 0) {
            rotated.addAll(ordered.subList(activeIndex, ordered.size()));
            rotated.addAll(ordered.subList(0, activeIndex));
        } else {
            rotated.addAll(ordered);
        }
        return rotated.stream().filter(id -> !id.equals(controllerId)).toList();
    }
}
