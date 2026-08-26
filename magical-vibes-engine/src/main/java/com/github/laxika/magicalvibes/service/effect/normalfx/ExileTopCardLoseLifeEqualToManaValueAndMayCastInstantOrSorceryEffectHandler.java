package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardLoseLifeEqualToManaValueAndMayCastInstantOrSorceryEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardLoseLifeEqualToManaValueAndMayCastInstantOrSorceryEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final LifeSupport lifeSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardLoseLifeEqualToManaValueAndMayCastInstantOrSorceryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        Card topCard = library.removeFirst();
        exileService.exileCard(gameData, controllerId, topCard);

        int manaValue = topCard.getManaValue();
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId != null && manaValue > 0) {
            lifeSupport.applyLifeLoss(gameData, targetPlayerId, manaValue, entry.getCard().getName());
        }

        gameLogService.append(gameData, GameLog.builder()
                .text(gameData.playerIdToName.get(controllerId) + " exiles ")
                .card(topCard)
                .text(" from the top of their library (mana value " + manaValue + ").")
                .build());

        if (topCard.hasType(CardType.INSTANT) || topCard.hasType(CardType.SORCERY)) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(),
                    controllerId,
                    List.of(new MayPlayExiledCardWithoutPayingManaCostEffect()),
                    "Cast " + topCard.getName() + " without paying its mana cost?",
                    topCard.getId()
            ));
            log.info("Game {} - {} may cast {} without paying its mana cost",
                    gameData.id, gameData.playerIdToName.get(controllerId), topCard.getName());
        }
    }
}
