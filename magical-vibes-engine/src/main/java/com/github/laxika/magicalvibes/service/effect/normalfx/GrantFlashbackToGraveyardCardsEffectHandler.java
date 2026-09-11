package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToGraveyardCardsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantFlashbackToGraveyardCardsEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantFlashbackToGraveyardCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantFlashbackToGraveyardCardsEffect) effect;
        int count = 0;
        if (e.allGraveyards()) {
            for (List<Card> graveyard : gameData.playerGraveyards.values()) {
                count += grantToGraveyard(gameData, graveyard, e);
            }
        } else {
            count = grantToGraveyard(gameData, gameData.playerGraveyards.get(entry.getControllerId()), e);
        }

        gameLogService.append(gameData, GameLog.builder().card(entry.getCard()).text(" grants flashback to " + count + " card(s) in graveyard until end of turn.").build());
        log.info("Game {} - {} grants flashback to {} graveyard card(s)", gameData.id, entry.getCard().getName(), count);
    }

    private int grantToGraveyard(GameData gameData, List<Card> graveyard,
                                 GrantFlashbackToGraveyardCardsEffect effect) {
        if (graveyard == null) return 0;
        int count = 0;
        for (Card card : graveyard) {
            if (!predicateEvaluationService.matchesCardPredicate(card, effect.filter(), null)
                    || card.getCastingOption(FlashbackCast.class).isPresent()) {
                continue;
            }
            gameData.cardsGrantedFlashbackUntilEndOfTurn.add(card.getId());
            count++;
        }
        return count;
    }
}
