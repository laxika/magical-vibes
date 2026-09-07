package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TargetSorceryDamageRedirectShield;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectAllDamageFromTargetSorceryToControllerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedirectAllDamageFromTargetSorceryToControllerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectAllDamageFromTargetSorceryToControllerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        StackEntry targetSorcery = gameQueryService.findStackEntryByCardId(gameData, entry.getTargetId());
        if (targetSorcery == null
                || targetSorcery.getEntryType() != StackEntryType.SORCERY_SPELL
                || targetSorcery.getCard() == null
                || targetSorcery.getControllerId() == null) {
            return;
        }

        gameData.targetSorceryDamageRedirectShields.add(new TargetSorceryDamageRedirectShield(
                targetSorcery.getCard().getId(), targetSorcery.getControllerId()));
        gameLogService.append(gameData, GameLog.cardThen(targetSorcery.getCard(),
                "'s damage is dealt to its controller instead this turn."));
    }
}
