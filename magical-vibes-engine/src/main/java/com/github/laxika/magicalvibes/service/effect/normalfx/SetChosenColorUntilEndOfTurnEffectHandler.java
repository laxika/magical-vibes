package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SetChosenColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SetChosenColorUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetChosenColorUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SetChosenColorUntilEndOfTurnEffect chosenColor = (SetChosenColorUntilEndOfTurnEffect) effect;
        UUID targetId = chosenColor.targeted() ? entry.getTargetId() : entry.getSourcePermanentId();
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null && (!chosenColor.canTargetSpell()
                || gameQueryService.findStackEntryByCardId(gameData, targetId) == null)) {
            return;
        }

        playerInputService.beginColorSetChoice(gameData, entry.getControllerId(), targetId,
                entry.getCard().getName());
    }
}
