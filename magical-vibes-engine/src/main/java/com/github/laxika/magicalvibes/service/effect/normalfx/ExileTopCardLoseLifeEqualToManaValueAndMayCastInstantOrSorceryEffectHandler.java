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
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Hidetsugu and Kairi's death trigger: exile the top card of the controller's library, target opponent loses life
 * equal to its mana value, then the controller may cast it without paying its mana cost if it's an instant or
 * sorcery card.
 *
 * <p>Marked {@link AsksDeathReplacementsFirst}: exiling a card from a library can't change what a Lich's Duel
 * Mastery's sacrifice can be replaced with, so the question that sacrifice needs (CR 616.1) is asked before the
 * exile, while nothing has changed yet, and the effect is resolved again with the answer; the loss itself still
 * follows the exile (CR 608.2c).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardLoseLifeEqualToManaValueAndMayCastInstantOrSorceryEffectHandler
        implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

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

        Card topCard = library.getFirst();
        int manaValue = topCard.getManaValue();
        UUID targetPlayerId = entry.getTargetId();
        Map<UUID, Integer> losses = targetPlayerId != null && gameData.playerIds.contains(targetPlayerId)
                ? Map.of(targetPlayerId, manaValue) : Map.of();
        lifeSupport.askBeforeLifeLossesThatFollowAChange(gameData, losses);

        library.removeFirst();
        exileService.exileCard(gameData, controllerId, topCard);
        gameLogService.append(gameData, GameLog.builder()
                .text(gameData.playerIdToName.get(controllerId) + " exiles ")
                .card(topCard)
                .text(" from the top of their library (mana value " + manaValue + ").")
                .build());

        lifeSupport.applyLifeLosses(gameData, losses, entry.getCard().getName());

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
