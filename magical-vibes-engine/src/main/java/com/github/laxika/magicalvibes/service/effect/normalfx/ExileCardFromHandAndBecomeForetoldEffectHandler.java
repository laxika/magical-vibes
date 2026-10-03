package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromHandAndBecomeForetoldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a hand-card exile that makes the chosen card foretold at a reduced mana cost. */
@Component
@RequiredArgsConstructor
public class ExileCardFromHandAndBecomeForetoldEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileCardFromHandAndBecomeForetoldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileCardFromHandAndBecomeForetoldEffect) effect;
        UUID controllerId = entry.getControllerId();

        if (e.chosenCard() == null) {
            List<Card> hand = gameData.playerHands.get(controllerId);
            if (hand == null || hand.isEmpty()) {
                gameLogService.append(gameData, GameLog.text(
                        gameData.playerIdToName.get(controllerId) + " has no cards to exile from hand."));
                return;
            }

            playerInputService.beginExileFromHandChoice(
                    gameData, controllerId, null, null, 1, List.of(), 0,
                    true, false, null, false, null, 0, false, e);
            return;
        }

        Card card = e.chosenCard();
        ManaCost foretellCost = card.getParsedManaCost() == null
                ? null : card.getParsedManaCost().reducedBy(new ManaCost("{2}"));
        if (!gameData.markExiledCardAsForetold(card.getId(), controllerId, foretellCost)) {
            return;
        }

        gameLogService.append(gameData, GameLog.cardThen(card,
                " becomes foretold with its mana cost reduced by {2}."));
    }
}
