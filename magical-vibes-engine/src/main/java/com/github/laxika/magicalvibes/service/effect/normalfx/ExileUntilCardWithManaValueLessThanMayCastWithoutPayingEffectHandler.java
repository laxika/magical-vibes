package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUntilCardWithManaValueLessThanMayCastWithoutPayingEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Resolves a Jodah-style library exile and free-cast offer. */
@Component
@RequiredArgsConstructor
public class ExileUntilCardWithManaValueLessThanMayCastWithoutPayingEffectHandler
        implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileUntilCardWithManaValueLessThanMayCastWithoutPayingEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileEffect = (ExileUntilCardWithManaValueLessThanMayCastWithoutPayingEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        int manaValue = amountEvaluationService.evaluate(gameData, exileEffect.manaValue(),
                AmountContext.forStackEntry(entry, null));

        if (deck == null || deck.isEmpty()) {
            return;
        }

        List<Card> revealed = new ArrayList<>();
        Card hit = null;
        while (!deck.isEmpty()) {
            Card card = deck.removeFirst();
            if (predicateEvaluationService.matchesCardPredicate(
                    card, exileEffect.predicate(), entry.getCard().getId(), gameData, controllerId)
                    && card.getManaValue() < manaValue) {
                hit = card;
                break;
            }
            revealed.add(card);
        }

        if (hit == null) {
            for (Card card : revealed) {
                exileService.exileCard(gameData, controllerId, card);
            }
            gameLogService.append(gameData, GameLog.text(playerName
                    + " exiles their library without finding a matching card for "
                    + entry.getCard().getName() + "."));
            return;
        }

        if (!revealed.isEmpty()) {
            Collections.shuffle(revealed);
            deck.addAll(revealed);
        }
        exileService.exileCard(gameData, controllerId, hit);
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                controllerId,
                List.of(new MayPlayExiledCardWithoutPayingManaCostEffect()),
                "You may cast " + hit.getName() + " without paying its mana cost.",
                hit.getId()));
        gameLogService.append(gameData, GameLog.text(playerName + " exiles " + hit.getName()
                + " for " + entry.getCard().getName() + " and may cast it without paying its mana cost."));
    }
}
