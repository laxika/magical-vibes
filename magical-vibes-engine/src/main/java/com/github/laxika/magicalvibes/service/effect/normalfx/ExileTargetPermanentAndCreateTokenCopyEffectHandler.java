package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndCreateTokenCopyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileTargetPermanentAndCreateTokenCopyEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPermanentAndCreateTokenCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTargetPermanentAndCreateTokenCopyEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        Card exiledCard = target.getCard();
        permanentRemovalService.removePermanentToExile(gameData, target);
        gameLogService.append(gameData, GameLog.cardThen(exiledCard, " is exiled."));

        if (!e.skipTokenCopyIfAura() || !exiledCard.isAura()) {
            CreateTokenCopyOfTargetPermanentEffect tokenProfile =
                    new CreateTokenCopyOfTargetPermanentEffect(
                            e.additionalSubtypes(), e.additionalTypes(), e.powerOverride(), e.toughnessOverride(),
                            java.util.Map.of(), false, true, false, false,
                            false, false, e.colorOverride(), e.additionalKeywords());
            tokenCopySupport.createTokenCopies(gameData, entry, java.util.List.of(exiledCard), null, tokenProfile);
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
