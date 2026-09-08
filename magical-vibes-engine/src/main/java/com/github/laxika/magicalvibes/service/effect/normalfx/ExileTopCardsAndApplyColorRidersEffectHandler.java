package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsAndApplyColorRidersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves Culmination of Studies' top-library exile and inserts its counted riders into the
 * current resolution so the normal token, draw, and damage handlers process them.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardsAndApplyColorRidersEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardsAndApplyColorRidersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTopCardsAndApplyColorRidersEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        int count = Math.max(0, amountEvaluationService.evaluate(gameData, e.count(),
                AmountContext.forStackEntry(entry, null)));
        int exiledCount = Math.min(count, library.size());
        int landCount = 0;
        int blueCount = 0;
        int redCount = 0;
        for (int i = 0; i < exiledCount; i++) {
            Card card = library.removeFirst();
            exileService.exileCard(gameData, controllerId, card);
            if (card.hasType(CardType.LAND)) {
                landCount++;
            }
            if (card.getColors().contains(CardColor.BLUE)) {
                blueCount++;
            }
            if (card.getColors().contains(CardColor.RED)) {
                redCount++;
            }
        }

        if (exiledCount == 0) {
            return;
        }

        String controllerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.text(controllerName + " exiles the top "
                + exiledCount + " card" + (exiledCount == 1 ? "" : "s") + " of their library."));
        log.info("Game {} - {} exiles {} cards for color riders", gameData.id, controllerName,
                exiledCount);

        List<CardEffect> riders = new ArrayList<>();
        if (landCount > 0) {
            riders.add(CreateTokenEffect.ofTreasureToken(landCount));
        }
        if (blueCount > 0) {
            riders.add(new DrawCardEffect(blueCount));
        }
        if (redCount > 0) {
            for (int i = 0; i < redCount; i++) {
                riders.add(new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT));
            }
        }

        if (!riders.isEmpty()) {
            int effectIndex = findEffectIndex(entry, effect);
            entry.insertEffectsToResolve(effectIndex + 1, riders);
        }
    }

    private int findEffectIndex(StackEntry entry, CardEffect effect) {
        List<CardEffect> effects = entry.getEffectsToResolve();
        for (int i = 0; i < effects.size(); i++) {
            if (effects.get(i) == effect) {
                return i;
            }
        }
        throw new IllegalStateException("Exile color-rider effect is not in its stack entry");
    }
}
