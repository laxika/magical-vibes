package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveScreamCounterFromExiledCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveScreamCounterFromExiledCardEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveScreamCounterFromExiledCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RemoveScreamCounterFromExiledCardEffect screamEffect =
                (RemoveScreamCounterFromExiledCardEffect) effect;
        UUID cardId = screamEffect.cardId();
        ExiledCardEntry exiledEntry = gameData.findExiledCard(cardId);
        Integer counters = gameData.exiledCardScreamCounters.get(cardId);
        if (exiledEntry == null || counters == null || counters <= 0) {
            gameData.exiledCardScreamCounters.remove(cardId);
            return;
        }

        Card card = exiledEntry.card();
        if (counters > 1) {
            int remaining = counters - 1;
            gameData.exiledCardScreamCounters.put(cardId, remaining);
            gameLogService.append(gameData, GameLog.builder().card(card)
                    .text(" has a scream counter removed (" + remaining + " remaining).").build());
            return;
        }

        if (!gameData.removeFromExile(cardId)) {
            gameData.exiledCardScreamCounters.remove(cardId);
            return;
        }

        gameData.exiledCardScreamCounters.remove(cardId);
        graveyardService.addCardToGraveyard(gameData, exiledEntry.ownerId(), card, Zone.EXILE);
        gameLogService.append(gameData, GameLog.cardThen(card,
                " has its last scream counter removed and is put into its owner's graveyard."));
        log.info("Game {} - {}'s last scream counter removed", gameData.id, card.getName());

        CardEffect followUp = screamEffect.whenLastCounterRemoved();
        if (followUp != null) {
            int effectIndex = entry.getEffectsToResolve().indexOf(effect);
            if (effectIndex >= 0) {
                entry.insertEffectsToResolve(effectIndex + 1, List.of(followUp));
            }
        }
    }
}
