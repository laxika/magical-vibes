package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTargetFaceDownPermanentAndMayTurnFaceUpEffect;
import com.github.laxika.magicalvibes.model.effect.TurnTargetFaceUpEffect;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RevealTargetFaceDownPermanentAndMayTurnFaceUpEffectHandler implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final GameQueryService gameQueryService;
    private final MayEffectHandler mayEffectHandler;
    private final ObjectProvider<GameService> gameServiceProvider;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTargetFaceDownPermanentAndMayTurnFaceUpEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (gameData.resolvedMayAccepted != null) {
            boolean accepted = gameData.resolvedMayAccepted;
            gameData.resolvedMayAccepted = null;
            if (accepted) {
                Permanent target = target(gameData, entry);
                if (target != null) {
                    gameServiceProvider.getObject()
                            .turnPermanentFaceUpIfCreatureCardWithoutPayingManaCost(gameData, target);
                }
            }
            return;
        }

        Permanent target = target(gameData, entry);
        if (target == null || !target.isFaceDown()) {
            return;
        }

        cardRevealService.revealToAllPlayers(gameData, entry.getControllerId(),
                GameEventFact.RevealZone.PERMANENT, List.of(target.getOriginalCard()));
        if (target.getOriginalCard().hasType(CardType.CREATURE)) {
            mayEffectHandler.resolve(gameData, entry,
                    new MayEffect(new TurnTargetFaceUpEffect(), "Turn it face up?"));
        }
    }

    private Permanent target(GameData gameData, StackEntry entry) {
        UUID targetId = entry.getTargetId();
        return targetId == null ? null : gameQueryService.findPermanentById(gameData, targetId);
    }
}
