package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves a random filtered library card entering the controller's battlefield. */
@Component
@RequiredArgsConstructor
public class SeekCardToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameQueryService gameQueryService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekCardToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SeekCardToBattlefieldEffect seek = (SeekCardToBattlefieldEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        List<Card> candidates = library.stream()
                .filter(card -> !card.isToken())
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, seek.filter(), entry.getCard().getId(), gameData, controllerId))
                .filter(card -> !gameQueryService.isCardBlockedFromEnteringFromZone(
                        gameData, card, Zone.LIBRARY))
                .toList();
        if (candidates.isEmpty()) {
            return;
        }

        Card chosen = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        if (!library.remove(chosen)) {
            return;
        }

        Permanent permanent = new Permanent(chosen, Zone.LIBRARY);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent);
        if (seek.enterTapped()) {
            permanent.tap();
        }
        if (chosen.hasType(com.github.laxika.magicalvibes.model.CardType.CREATURE)) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId,
                    chosen, null, false);
        }
        gameLogService.append(gameData,
                GameLog.entersBattlefieldUnder(chosen, gameData.playerIdToName.get(controllerId)));
    }
}
