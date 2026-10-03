package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromHandWithManaValueTimeCountersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Alaundo the Seer's hand exile and time-counter progression. */
@Component
@RequiredArgsConstructor
public class ExileCardFromHandWithManaValueTimeCountersEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final RemoveTimeCounterFromExiledCardEffectHandler removeTimeCounterHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileCardFromHandWithManaValueTimeCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileCardFromHandWithManaValueTimeCountersEffect alaundoEffect =
                (ExileCardFromHandWithManaValueTimeCountersEffect) effect;
        UUID controllerId = entry.getControllerId();

        if (alaundoEffect.chosenCard() == null) {
            List<com.github.laxika.magicalvibes.model.Card> hand = gameData.playerHands.get(controllerId);
            if (hand != null && !hand.isEmpty()) {
                playerInputService.beginExileFromHandChoice(gameData, controllerId, alaundoEffect);
            }
            return;
        }

        UUID chosenCardId = alaundoEffect.chosenCard().getId();
        ExiledCardEntry chosenEntry = gameData.findExiledCard(chosenCardId);
        if (chosenEntry == null || !controllerId.equals(chosenEntry.ownerId())) {
            return;
        }

        int manaValue = chosenEntry.card().getManaValue();
        gameData.exiledCardsWithAlaundoCastAbility.add(chosenCardId);
        if (chosenEntry.card().getHandActivatedAbilities().stream()
                .noneMatch(com.github.laxika.magicalvibes.model.ActivatedAbility::isSuspendsSourceFromHand)) {
            gameData.exiledCardsWithNonSuspendTimeCounters.add(chosenCardId);
        }
        if (manaValue > 0) {
            gameData.exiledCardTimeCounters.put(chosenCardId, manaValue);
            gameLogService.append(gameData,
                    GameLog.cardThen(chosenEntry.card(), " gets " + manaValue + " time counters."));
        }

        for (ExiledCardEntry otherEntry : List.copyOf(gameData.exiledCards)) {
            if (!controllerId.equals(otherEntry.ownerId())
                    || chosenCardId.equals(otherEntry.card().getId())) {
                continue;
            }
            Integer counters = gameData.exiledCardTimeCounters.get(otherEntry.card().getId());
            if (counters != null && counters > 0) {
                removeTimeCounterHandler.removeTimeCounter(gameData, otherEntry.card().getId());
            }
        }
    }
}
