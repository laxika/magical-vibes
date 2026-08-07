package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopXCardsLandsToBattlefieldTappedRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
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
public class RevealTopXCardsLandsToBattlefieldTappedRestOnBottomRandomEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final ConditionEvaluationService conditionEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopXCardsLandsToBattlefieldTappedRestOnBottomRandomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typedEffect = (RevealTopXCardsLandsToBattlefieldTappedRestOnBottomRandomEffect) effect;
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        List<Card> deck = gameData.playerDecks.get(controllerId);

        int xValue = entry.getXValue();
        if (xValue <= 0 || deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " reveals no cards from the top of their library."));
            return;
        }

        int count = Math.min(xValue, deck.size());
        List<Card> revealedCards = new ArrayList<>(deck.subList(0, count));
        deck.subList(0, count).clear();

        gameLogService.append(gameData, GameLog.text(playerName + " reveals "
                + revealedCards.stream().map(Card::getName).collect(Collectors.joining(", "))
                + " from the top of their library."));

        // The lands enter tapped; the spell-mastery rider untaps exactly the lands put onto the
        // battlefield this way, so it is evaluated once and applied to those permanents only.
        boolean untap = conditionEvaluationService.isMet(
                gameData, typedEffect.untapCondition(), ConditionContext.forStackEntry(entry));

        List<Card> remaining = new ArrayList<>();
        for (Card card : revealedCards) {
            if (!card.hasType(CardType.LAND)) {
                remaining.add(card);
                continue;
            }
            Permanent permanent = new Permanent(card);
            permanent.tap();
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent);
            if (untap) {
                permanent.untap();
            }
            gameLogService.append(gameData, GameLog.entersBattlefieldUnder(card, playerName));
        }

        if (!remaining.isEmpty()) {
            Collections.shuffle(remaining);
            deck.addAll(remaining);
        }

        log.info("Game {} - {} revealed {} cards, put {} lands onto the battlefield (untapped={})",
                gameData.id, playerName, count, count - remaining.size(), untap);
    }
}
