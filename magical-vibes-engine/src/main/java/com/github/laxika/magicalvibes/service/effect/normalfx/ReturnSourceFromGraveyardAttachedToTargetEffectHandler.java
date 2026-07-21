package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceFromGraveyardAttachedToTargetEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnSourceFromGraveyardAttachedToTargetEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnSourceFromGraveyardAttachedToTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID auraCardId = entry.getCard().getId();
        UUID auraOwnerId = entry.getControllerId();
        UUID targetId = entry.getTargetId();

        Permanent target = targetId != null ? gameQueryService.findPermanentById(gameData, targetId) : null;
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(entry.getCard(),
                    "'s ability fizzles (no legal creature to attach to)."));
            log.info("Game {} - {} fizzles, attach target missing or not a creature",
                    gameData.id, entry.getCard().getName());
            return;
        }

        Card auraCard = gameQueryService.findCardInGraveyardById(gameData, auraCardId);
        if (auraCard == null) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(entry.getCard(),
                    "'s ability fizzles (card not in graveyard)."));
            log.info("Game {} - {} not found in graveyard, ability fizzles",
                    gameData.id, entry.getCard().getName());
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, auraCardId);

        Permanent auraPerm = new Permanent(auraCard);
        auraPerm.setAttachedTo(target.getId());
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, auraOwnerId, auraPerm);

        String ownerName = gameData.playerIdToName.get(auraOwnerId);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                .card(auraCard)
                .text(" returns to the battlefield attached to ")
                .card(target.getCard())
                .text(" under " + ownerName + "'s control.")
                .build());
        log.info("Game {} - {} returns attached to {}", gameData.id, auraCard.getName(), target.getCard().getName());
    }
}
