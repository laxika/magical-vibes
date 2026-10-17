package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileDyingCreatureAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileDyingCreatureAndCreateTokenCopyEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final TokenCopySupport tokenCopySupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileDyingCreatureAndCreateTokenCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileDyingCreatureAndCreateTokenCopyEffect) effect;
        UUID dyingCardId = e.dyingCardId();
        if (dyingCardId == null) {
            return;
        }

        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, dyingCardId);
        Card dyingCard = gameQueryService.findCardInGraveyardById(gameData, dyingCardId);
        if (ownerId == null || dyingCard == null) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    "'s ability does nothing (that card is no longer in a graveyard)."));
            return;
        }

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, dyingCardId);
        exileService.exileCard(gameData, ownerId, dyingCard);
        // The token copies the creature as it last existed on the battlefield, so a creature that was
        // copying another one is copied as that copy, not as the card in the graveyard (CR 707.2).
        Permanent departed = gameData.departedPermanentSnapshots.get(dyingCardId);
        Card cardToCopy = departed != null && departed.getCard() != departed.getOriginalCard()
                ? departed.getCard() : dyingCard;
        tokenCopySupport.createTokenCopies(gameData, entry, List.of(cardToCopy), null,
                entry.getControllerId(), e.tokenCopyEffect());
    }
}
