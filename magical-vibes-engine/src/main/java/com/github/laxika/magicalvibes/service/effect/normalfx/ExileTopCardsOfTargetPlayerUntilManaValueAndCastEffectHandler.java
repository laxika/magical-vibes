package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfTargetPlayerUntilManaValueAndCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Resolves Synthesis Pod's exact-mana-value library exile and free-cast offer. */
@Component
@RequiredArgsConstructor
public class ExileTopCardsOfTargetPlayerUntilManaValueAndCastEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsOfTargetPlayerUntilManaValueAndCastEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        Card exiledSpell = entry.getExiledCostCardSnapshot();
        List<Card> library = targetPlayerId == null ? null : gameData.playerDecks.get(targetPlayerId);
        if (library == null || library.isEmpty() || exiledSpell == null) {
            return;
        }

        int requiredManaValue = exiledSpell.getManaValue() + 1;
        List<Card> revealed = new ArrayList<>();
        Card hit = null;
        while (!library.isEmpty()) {
            Card card = library.removeFirst();
            if (card.getManaValue() == requiredManaValue) {
                hit = card;
                break;
            }
            revealed.add(card);
        }

        library.addAll(revealed);
        Collections.shuffle(library);
        if (hit == null) {
            return;
        }

        exileService.exileCard(gameData, targetPlayerId, hit);
        UUID controllerId = entry.getControllerId();
        String controllerName = gameData.playerIdToName.get(controllerId);
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String prompt = "You may cast " + hit.getName() + " without paying its mana cost.";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                controllerId,
                List.of(new MayPlayExiledCardWithoutPayingManaCostEffect()),
                prompt,
                hit.getId()));
        gameLogService.append(gameData, GameLog.text(controllerName + " exiles " + hit.getName()
                + " from " + targetName + "'s library for " + entry.getCard().getName()
                + " and may cast it without paying its mana cost."));
    }
}
