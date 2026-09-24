package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class SeekLibraryEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SeekLibraryEffect seek = (SeekLibraryEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            logNoMatch(gameData, entry);
            return;
        }

        List<Card> matchingCards = library.stream()
                .filter(card -> card.getManaValue() <= seek.maxManaValue())
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, seek.filter(), null, gameData, controllerId))
                .filter(card -> !gameQueryService.isCardBlockedFromEnteringFromZone(
                        gameData, card, Zone.LIBRARY))
                .toList();
        if (matchingCards.isEmpty()) {
            logNoMatch(gameData, entry);
            return;
        }

        Card chosen = matchingCards.get(ThreadLocalRandom.current().nextInt(matchingCards.size()));
        library.remove(chosen);
        Permanent permanent = new Permanent(chosen, Zone.LIBRARY);
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent);
        if (seek.entersTapped()) {
            permanent.tap();
        }
        gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                .text("seeks and puts " + chosen.getName() + " onto the battlefield.").build());
    }

    private void logNoMatch(GameData gameData, StackEntry entry) {
        gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                .text("seeks but finds no matching card.").build());
    }
}
