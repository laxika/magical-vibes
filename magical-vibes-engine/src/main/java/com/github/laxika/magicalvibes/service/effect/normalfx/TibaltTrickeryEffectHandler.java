package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastExiledCardThenBottomRestEffect;
import com.github.laxika.magicalvibes.model.effect.TibaltTrickeryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves Tibalt's Trickery's random mill and top-library exile rider. The found card is offered
 * through the shared free-cast flow, which also returns the other tracked exiled cards to the
 * bottom of their owner's library in a random order.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TibaltTrickeryEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final ExileBottomRandomSupport exileBottomRandomSupport;
    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TibaltTrickeryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        StackEntry targetSpell = findTargetSpell(gameData, entry.getTargetId());
        if (targetSpell == null) {
            return;
        }

        UUID targetControllerId = targetSpell.getControllerId();
        UUID sourceId = entry.getCard().getId();
        int millCount = ThreadLocalRandom.current().nextInt(1, 4);
        graveyardService.resolveMillPlayer(gameData, targetControllerId, millCount);

        List<Card> library = gameData.playerDecks.get(targetControllerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        String targetSpellName = targetSpell.getCard().getName();
        Card found = null;
        int exiledCount = 0;
        while (!library.isEmpty()) {
            Card top = library.removeFirst();
            exileService.exileCard(gameData, targetControllerId, top, sourceId);
            exiledCount++;
            if (!top.hasType(CardType.LAND) && !Objects.equals(targetSpellName, top.getName())) {
                found = top;
                break;
            }
        }

        String playerName = gameData.playerIdToName.get(targetControllerId);
        if (found == null) {
            gameLogService.append(gameData, GameLog.text(playerName + " exiles " + exiledCount
                    + " card(s) from the top of their library with Tibalt's Trickery; no different-name"
                    + " nonland card was found."));
            exileBottomRandomSupport.bottomCardsExiledWithSource(gameData, sourceId, null);
            return;
        }

        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " exiles cards until ").card(found)
                .text(" with Tibalt's Trickery and may cast it without paying its mana cost.")
                .build());
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                targetControllerId,
                List.of(new MayCastExiledCardThenBottomRestEffect(sourceId)),
                "Cast " + found.getName() + " without paying its mana cost?",
                found.getId()
        ));
    }

    private StackEntry findTargetSpell(GameData gameData, UUID targetCardId) {
        if (targetCardId == null) {
            return null;
        }
        for (StackEntry stackEntry : gameData.stack) {
            if (stackEntry.getCard().getId().equals(targetCardId)) {
                return stackEntry;
            }
        }
        return null;
    }
}
