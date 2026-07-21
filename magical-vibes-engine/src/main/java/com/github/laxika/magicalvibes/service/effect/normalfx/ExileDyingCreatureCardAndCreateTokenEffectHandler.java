package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileDyingCreatureCardAndCreateTokenEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves the accepted "you may exile that card. If you do, create a token." branch of Unscythe,
 * Killer of Kings' damaged-creature-dies trigger. The dying creature's card id is bound onto the
 * effect at trigger time. The card is exiled only if it is still in a graveyard (a token, or a card
 * that left the graveyard in response, can't be exiled — and then no token is created).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileDyingCreatureCardAndCreateTokenEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final PermanentControlSupport permanentControlSupport;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileDyingCreatureCardAndCreateTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileDyingCreatureCardAndCreateTokenEffect) effect;
        UUID dyingCardId = e.dyingCardId();
        if (dyingCardId == null) {
            return;
        }

        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, dyingCardId);
        Card card = gameQueryService.findCardInGraveyardById(gameData, dyingCardId);
        if (ownerId == null || card == null) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(entry.getCard(),
                    "'s ability does nothing (that card is no longer in a graveyard)."));
            log.info("Game {} - {} can't exile dying card {} (not in a graveyard)",
                    gameData.id, entry.getCard().getName(), dyingCardId);
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, dyingCardId);
        exileService.exileCard(gameData, ownerId, card);
        permanentControlSupport.applyCreateToken(gameData, entry.getControllerId(), e.token(),
                entry.getCard().getSetCode());

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(entry.getCard(),
                "'s ability exiles ", card, " and creates a 2/2 black Zombie creature token."));
        log.info("Game {} - {} exiles {} and creates a Zombie token",
                gameData.id, entry.getCard().getName(), card.getName());
    }
}
