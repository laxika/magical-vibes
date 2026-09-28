package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutRandomCardFromLibraryIntoGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves a random filtered library card moving to the controller's graveyard. */
@Component
@RequiredArgsConstructor
public class PutRandomCardFromLibraryIntoGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GraveyardService graveyardService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutRandomCardFromLibraryIntoGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        PutRandomCardFromLibraryIntoGraveyardEffect randomEffect =
                (PutRandomCardFromLibraryIntoGraveyardEffect) effect;
        List<Card> candidates = library.stream()
                .filter(card -> !card.isToken())
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, randomEffect.predicate(), entry.getCard().getId(), gameData, controllerId))
                .toList();
        if (candidates.isEmpty()) {
            return;
        }

        Card chosen = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        if (!library.remove(chosen)) {
            return;
        }

        if (graveyardService.addCardToGraveyard(gameData, controllerId, chosen, Zone.LIBRARY)) {
            gameLogService.append(gameData, GameLog.cardThen(chosen,
                    " is put into " + gameData.playerIdToName.get(controllerId)
                            + "'s graveyard from their library."));
        }
    }
}
