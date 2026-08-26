package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateSetSelfBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealUntilCardPredicateSetSelfBasePowerToughnessEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilCardPredicateSetSelfBasePowerToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typedEffect = (RevealUntilCardPredicateSetSelfBasePowerToughnessEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + "'s library is empty — no cards are revealed."));
            return;
        }

        List<Card> revealedCards = new ArrayList<>();
        Card foundCard = null;
        while (!deck.isEmpty()) {
            Card card = deck.removeFirst();
            revealedCards.add(card);
            if (predicateEvaluationService.matchesCardPredicate(
                    card, typedEffect.predicate(), entry.getCard().getId(), gameData, controllerId)) {
                foundCard = card;
                break;
            }
        }

        String revealedNames = revealedCards.stream()
                .map(Card::getName)
                .collect(Collectors.joining(", "));
        gameLogService.append(gameData, GameLog.text(
                playerName + " reveals " + revealedNames + " from the top of their library."));

        Collections.shuffle(revealedCards);
        deck.addAll(revealedCards);

        if (foundCard == null) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " reveals their entire library — no matching card was found."));
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        CardPowerToughness cardPowerToughness = currentPowerToughness(gameData, foundCard, controllerId);
        int power = cardPowerToughness.power() * typedEffect.multiplier();
        int toughness = cardPowerToughness.toughness() * typedEffect.multiplier();
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), source.getId(), controllerId,
                new SetBasePowerToughnessEffect(power, toughness), source.getId(), null, null,
                EffectDuration.UNTIL_YOUR_NEXT_TURN, 0));

        gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                " has base power and toughness " + power + "/" + toughness
                        + " until your next turn."));
        log.info("Game {} - {} has base power and toughness {}/{} until next turn",
                gameData.id, source.getCard().getName(), power, toughness);
    }

    private CardPowerToughness currentPowerToughness(GameData gameData, Card card, UUID controllerId) {
        Permanent characteristicSource = new Permanent(card);
        AmountContext context = AmountContext.forStaticEffect(characteristicSource, controllerId);
        for (CardEffect staticEffect : card.getEffects(EffectSlot.STATIC)) {
            if (staticEffect instanceof SetPowerToughnessToAmountEffect setPowerToughness) {
                return new CardPowerToughness(
                        amountEvaluationService.evaluate(gameData, setPowerToughness.power(), context),
                        amountEvaluationService.evaluate(gameData, setPowerToughness.toughness(), context));
            }
        }
        return new CardPowerToughness(
                card.getPower() == null ? 0 : card.getPower(),
                card.getToughness() == null ? 0 : card.getToughness());
    }

    private record CardPowerToughness(int power, int toughness) {
    }
}
