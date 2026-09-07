package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastExiledCardWithNormalCostEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastInstantOrSorceryCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Queues one exclusive normal-cost cast offer for an instant or sorcery exiled with the source. */
@Slf4j
@Component
@RequiredArgsConstructor
public class MayCastInstantOrSorceryCardsExiledWithSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastInstantOrSorceryCardsExiledWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }

        List<Card> exiled = gameData.getCardsExiledByPermanent(sourcePermanentId).stream()
                .filter(card -> card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY))
                .toList();
        if (exiled.isEmpty()) {
            return;
        }

        UUID offerGroupId = UUID.randomUUID();
        for (Card card : exiled) {
            gameData.pendingMayAbilities.add(new PendingMayAbility(
                    card,
                    entry.getControllerId(),
                    List.of(new MayCastExiledCardWithNormalCostEffect(offerGroupId)),
                    "Cast " + card.getName() + "?",
                    card.getId(),
                    null,
                    sourcePermanentId));
        }
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " offers a normal-cost instant or sorcery cast from among the cards exiled with it."));
        log.info("Game {} - {} offers {} normal-cost cast(s) from source-linked exile",
                gameData.id, entry.getCard().getName(), exiled.size());
    }
}
