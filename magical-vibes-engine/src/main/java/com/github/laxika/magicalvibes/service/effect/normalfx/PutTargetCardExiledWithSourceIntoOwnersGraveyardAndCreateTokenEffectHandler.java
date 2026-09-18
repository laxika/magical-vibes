package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardExiledWithSourceIntoOwnersGraveyardAndCreateTokenEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Currency Converter's exiled-card graveyard and token-creation ability. */
@Component
@RequiredArgsConstructor
public class PutTargetCardExiledWithSourceIntoOwnersGraveyardAndCreateTokenEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final PermanentControlSupport permanentControlSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetCardExiledWithSourceIntoOwnersGraveyardAndCreateTokenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var putEffect = (PutTargetCardExiledWithSourceIntoOwnersGraveyardAndCreateTokenEffect) effect;
        UUID targetId = entry.getTargetId();
        ExiledCardEntry exiled = targetId == null ? null : gameData.findExiledCard(targetId);
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null && entry.getSourcePermanentSnapshot() != null) {
            sourcePermanentId = entry.getSourcePermanentSnapshot().getId();
        }
        if (exiled == null || sourcePermanentId == null
                || !sourcePermanentId.equals(exiled.sourcePermanentId()) || exiled.faceDown()) {
            return;
        }
        if (!gameData.removeFromExile(targetId)) {
            return;
        }

        graveyardService.addCardToGraveyard(gameData, exiled.ownerId(), exiled.card(), Zone.EXILE);
        gameLogService.append(gameData, GameLog.cardThen(exiled.card(),
                " is put into its owner's graveyard."));

        CreateTokenEffect token = exiled.card().hasType(CardType.LAND)
                ? putEffect.landToken() : putEffect.nonlandToken();
        entry.getCreatedPermanentIds().addAll(permanentControlSupport.applyCreateToken(
                gameData, entry.getControllerId(), token, 1, entry.getCard().getSetCode()));
    }
}
