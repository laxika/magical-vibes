package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameAtResolutionEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeForSourceEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChooseCardNameAtResolutionEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCardNameAtResolutionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        begin(gameData, entry.getCard(), entry.getControllerId(), entry.getSourcePermanentId(),
                (ChooseCardNameAtResolutionEffect) effect, null);
    }

    /**
     * Starts the name choice. A non-null {@code followUpSubtypeChoice} is made right after the name, for an
     * "as this becomes attached" pair of choices that is not a triggered ability.
     */
    public void begin(GameData gameData, Card sourceCard, UUID controllerId, UUID sourcePermanentId,
                      ChooseCardNameAtResolutionEffect chooseEffect,
                      ChooseSubtypeForSourceEffect followUpSubtypeChoice) {
        playerInputService.beginCardNameAtResolutionChoice(gameData,
                new ChoiceContext.ChooseCardNameAtResolutionChoice(
                        sourceCard, controllerId, sourcePermanentId, chooseEffect, followUpSubtypeChoice));

        log.info("Game {} - Awaiting {} to choose a card name for {}",
                gameData.id, gameData.playerIdToName.get(controllerId), sourceCard.getName());
    }
}
