package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureGainToughnessLosePowerToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Sapling of Colfenor's attack trigger: reveals the top card of its controller's library; a creature card makes them
 * gain life equal to its toughness, lose life equal to its power, and then go to their hand.
 *
 * <p>Marked {@link AsksDeathReplacementsFirst}: the card stays in the library until after the life changes, so the
 * Lich's Duel Mastery sacrifice that replaces the loss is asked about (CR 616.1) before anything changes.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardCreatureGainToughnessLosePowerToHandEffectHandler implements NormalEffectHandlerBean,
        AsksDeathReplacementsFirst {

    private final LifeSupport lifeSupport;
    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardCreatureGainToughnessLosePowerToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        // Reveal the top card. It only leaves the library if it's a creature card, after the life changes.
        Card topCard = deck.getFirst();
        if (!topCard.hasType(CardType.CREATURE)) {
            gameLogService.append(gameData, GameLog.builder().text(playerName + " reveals ").card(topCard).text(" from the top of their library (" + sourceName + ").").build());
            return;
        }

        int toughness = topCard.getToughness() != null ? topCard.getToughness() : 0;
        int power = topCard.getPower() != null ? topCard.getPower() : 0;
        for (CardEffect staticEffect : topCard.getStaticEffects()) {
            if (staticEffect instanceof SetPowerToughnessToAmountEffect characteristicDefining) {
                AmountContext context = AmountContext.forStaticEffect(new Permanent(topCard), controllerId);
                power = Math.max(0, amountEvaluationService.evaluate(
                        gameData, characteristicDefining.power(), context));
                toughness = Math.max(0, amountEvaluationService.evaluate(
                        gameData, characteristicDefining.toughness(), context));
                break;
            }
        }

        // Nothing has changed yet: a Lich's Duel Mastery sacrifice the gain (when a replacement turns it into a loss)
        // or the loss will cause is asked about (CR 616.1) before the gain, which closes the window the loss would
        // ask in.
        lifeSupport.askBeforeLifeGainThenLoss(gameData, controllerId, toughness, power);
        gameLogService.append(gameData, GameLog.builder().text(playerName + " reveals ").card(topCard).text(" from the top of their library (" + sourceName + ").").build());

        // Gain life equal to toughness, then lose life equal to power, then put it into hand (CR 608.2c).
        if (toughness > 0) {
            lifeSupport.applyGainLife(gameData, controllerId, toughness, sourceName,
                    entry.getCard(), entry.getEntryType());
        }
        if (power > 0) {
            lifeSupport.applyLifeLoss(gameData, controllerId, power, sourceName);
        }

        deck.remove(topCard);
        gameData.addCardToHand(controllerId, topCard);
        gameLogService.append(gameData, GameLog.builder().text(playerName + " puts ").card(topCard).text(" into their hand (" + sourceName + ").").build());

        log.info("Game {} - {} reveals creature {} (P/T {}/{}) via {}",
                gameData.id, playerName, topCard.getName(), power, toughness, sourceName);
    }
}
