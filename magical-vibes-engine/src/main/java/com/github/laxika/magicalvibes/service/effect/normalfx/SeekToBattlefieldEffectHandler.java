package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class SeekToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameQueryService gameQueryService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SeekToBattlefieldEffect seekEffect = (SeekToBattlefieldEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        UUID sourceCardId = entry.getCard() == null ? null : entry.getCard().getId();
        List<Card> matchingCards = library.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, seekEffect.predicate(), sourceCardId, gameData, controllerId))
                .filter(card -> !gameQueryService.isCardBlockedFromEnteringFromZone(
                        gameData, card, Zone.LIBRARY))
                .toList();
        if (matchingCards.isEmpty()) {
            return;
        }

        Card selected = matchingCards.get(ThreadLocalRandom.current().nextInt(matchingCards.size()));
        library.remove(selected);

        Permanent permanent = new Permanent(selected, Zone.LIBRARY);
        permanent.tap();
        battlefieldEntryService.putPermanentOntoBattlefield(
                gameData, controllerId, permanent,
                battlefieldEntryService.snapshotEnterTappedTypes(gameData));
        battlefieldEntryService.processLandETBEffects(gameData, controllerId, selected);
        gameLogService.append(gameData, GameLog.entersBattlefieldTappedUnder(
                selected, gameData.playerIdToName.get(controllerId)));
        triggerCollectionService.checkSeekTriggers(gameData, controllerId, List.of(selected));
    }
}
