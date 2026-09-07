package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandOfEachOpponentWithSourceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the source-tracked library exile on Krang & Shredder. */
@Component
@RequiredArgsConstructor
public class ExileTopUntilNonlandOfEachOpponentWithSourceEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final ExileService exileService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopUntilNonlandOfEachOpponentWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        Permanent sourcePermanent = sourcePermanentId == null
                ? null : gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (sourcePermanent == null) {
            sourcePermanentId = findSourcePermanentId(gameData, entry);
        }
        if (sourcePermanentId == null) {
            return;
        }

        for (UUID opponentId : gameData.orderedPlayerIds) {
            if (opponentId.equals(entry.getControllerId())) {
                continue;
            }
            var library = gameData.playerDecks.get(opponentId);
            while (library != null && !library.isEmpty()) {
                Card card = library.removeFirst();
                exileService.exileCard(gameData, opponentId, card, sourcePermanentId);
                if (!card.hasType(CardType.LAND)) {
                    break;
                }
            }
        }
    }

    private UUID findSourcePermanentId(GameData gameData, StackEntry entry) {
        var battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) {
            return null;
        }
        for (Permanent permanent : battlefield) {
            if (permanent.getCard().getId().equals(entry.getCard().getId())) {
                return permanent.getId();
            }
        }
        return null;
    }
}
