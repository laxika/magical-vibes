package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfCommanderEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Resolves I Am Never Alone's commander-wide token-copy effect. */
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfCommanderEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfCommanderEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> commanders = gameData.playerCommanders.getOrDefault(entry.getControllerId(), List.of());
        if (commanders.isEmpty()) {
            return;
        }

        Card commander = commanders.getFirst();
        Permanent commanderPermanent = null;
        Card sourceCard = null;
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (permanent.getOriginalCard() != null
                        && commander.getId().equals(permanent.getOriginalCard().getId())) {
                    commanderPermanent = permanent;
                    sourceCard = permanent.getCard();
                    break;
                }
            }
            if (sourceCard != null) {
                break;
            }
        }
        if (sourceCard == null) {
            sourceCard = gameQueryService.findCardById(gameData, commander.getId());
        }
        if (sourceCard == null) {
            sourceCard = commander;
        }

        tokenCopySupport.createTokenCopies(
                gameData,
                entry,
                List.of(sourceCard),
                commanderPermanent,
                entry.getControllerId(),
                CreateTokenCopyOfTargetPermanentEffect.nonLegendary(
                        List.of(), Set.of(), null, null, Map.of()));
    }
}
