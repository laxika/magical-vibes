package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEachCreatureTokenOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfEachCreatureTokenOnBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private static final CreateTokenCopyOfTargetPermanentEffect PLAIN_TOKEN_COPY =
            new CreateTokenCopyOfTargetPermanentEffect();

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfEachCreatureTokenOnBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Map<UUID, List<Card>> sourceCardsByController = new LinkedHashMap<>();
        for (Map.Entry<UUID, List<Permanent>> battlefieldEntry : gameData.playerBattlefields.entrySet()) {
            List<Card> sourceCards = battlefieldEntry.getValue().stream()
                    .filter(permanent -> permanent.getCard().isToken())
                    .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                    .map(Permanent::getCard)
                    .toList();
            if (!sourceCards.isEmpty()) {
                sourceCardsByController.put(battlefieldEntry.getKey(), sourceCards);
            }
        }

        for (Map.Entry<UUID, List<Card>> sourceEntry : sourceCardsByController.entrySet()) {
            tokenCopySupport.createTokenCopies(
                    gameData, entry, sourceEntry.getValue(), null, sourceEntry.getKey(), PLAIN_TOKEN_COPY);
        }
    }
}
