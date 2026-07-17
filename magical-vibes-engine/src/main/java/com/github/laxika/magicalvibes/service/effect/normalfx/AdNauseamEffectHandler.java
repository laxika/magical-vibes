package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AdNauseamEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Ad Nauseam. Performs the first (mandatory) reveal-and-lose-life iteration, then — while the
 * library still has cards — begins the repeat accept/decline prompt. Each accepted repeat runs
 * another iteration and re-prompts; declining (or an empty library) ends the resolution. See
 * {@link com.github.laxika.magicalvibes.service.interaction.AdNauseamRepeatChoiceInteractionHandler}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdNauseamEffectHandler implements NormalEffectHandlerBean {

    private final AdNauseamSupport adNauseamSupport;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AdNauseamEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String sourceName = entry.getCard().getName();
        List<Card> deck = gameData.playerDecks.get(controllerId);

        if (deck == null || deck.isEmpty()) {
            String playerName = gameData.playerIdToName.get(controllerId);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        // The first iteration is mandatory; subsequent iterations are the controller's choice.
        adNauseamSupport.revealTopCardAndLoseLife(gameData, controllerId, sourceName);
        adNauseamSupport.beginRepeatPromptIfPossible(gameData, controllerId, sourceName);
    }
}
