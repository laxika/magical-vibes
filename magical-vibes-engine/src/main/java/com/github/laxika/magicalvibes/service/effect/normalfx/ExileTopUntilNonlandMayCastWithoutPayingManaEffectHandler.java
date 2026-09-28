package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandMayCastWithoutPayingManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a library dig that leaves all exiled cards in exile and offers the first nonland free. */
@Component
@RequiredArgsConstructor
public class ExileTopUntilNonlandMayCastWithoutPayingManaEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopUntilNonlandMayCastWithoutPayingManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (library == null || library.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + "'s library is empty."));
            return;
        }

        Card hit = null;
        int exiledCount = 0;
        while (!library.isEmpty()) {
            Card top = library.removeFirst();
            exileService.exileCard(gameData, controllerId, top);
            exiledCount++;
            if (!top.hasType(CardType.LAND)) {
                hit = top;
                break;
            }
        }

        if (hit == null) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " exiles " + exiledCount + " card(s) until a nonland card is found, but finds none."));
            return;
        }

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                controllerId,
                List.of(new MayPlayExiledCardWithoutPayingManaCostEffect()),
                "You may cast " + hit.getName() + " without paying its mana cost.",
                hit.getId()));
        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " exiles cards until ").card(hit)
                .text(" and may cast it without paying its mana cost (" + entry.getCard().getName() + ").")
                .build());
    }
}
