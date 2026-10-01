package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayCardExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlaySourceExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Offers one source-tracked exiled card for a free play, including lands. */
@Slf4j
@Component
@RequiredArgsConstructor
public class MayPlayCardExiledWithSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayPlayCardExiledWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }

        List<Card> exiledCards = gameData.getCardsExiledByPermanent(sourcePermanentId);
        if (exiledCards.isEmpty()) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), " has no cards exiled with it to play."));
            return;
        }

        UUID controllerId = entry.getControllerId();
        for (int i = exiledCards.size() - 1; i >= 0; i--) {
            Card card = exiledCards.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    card,
                    controllerId,
                    List.of(new MayPlaySourceExiledCardWithoutPayingManaCostEffect()),
                    "Play " + card.getName() + " without paying its mana cost?",
                    card.getId(),
                    null,
                    sourcePermanentId
            ));
        }

        log.info("Game {} - {} offers a free play of {} card(s) exiled with {}",
                gameData.id, gameData.playerIdToName.get(controllerId), exiledCards.size(),
                entry.getCard().getName());
    }
}
