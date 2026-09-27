package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopiesOfTargetArtifactOrGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateTokenCopiesOfTargetArtifactOrGraveyardCardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopiesOfTargetArtifactOrGraveyardCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var copyEffect = (CreateTokenCopiesOfTargetArtifactOrGraveyardCardEffect) effect;
        Card targetCard;

        if (entry.getTargetZone() == Zone.GRAVEYARD) {
            UUID targetCardId = entry.getTargetCardIds().isEmpty()
                    ? entry.getTargetId() : entry.getTargetCardIds().getFirst();
            targetCard = targetCardId == null
                    ? null : gameQueryService.findCardInGraveyardById(gameData, targetCardId);
            if (targetCard == null || !targetCard.hasType(CardType.ARTIFACT)) {
                return;
            }
        } else {
            Permanent targetPermanent = entry.getTargetId() == null
                    ? null : gameQueryService.findPermanentById(gameData, entry.getTargetId());
            if (targetPermanent == null || !gameQueryService.isArtifact(gameData, targetPermanent)) {
                return;
            }
            targetCard = targetPermanent.getCard();
        }

        Permanent sourcePermanent = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        tokenCopySupport.createTokenCopies(
                gameData,
                entry,
                Collections.nCopies(copyEffect.amount(), targetCard),
                sourcePermanent,
                new CreateTokenCopyOfTargetPermanentEffect());
    }
}
