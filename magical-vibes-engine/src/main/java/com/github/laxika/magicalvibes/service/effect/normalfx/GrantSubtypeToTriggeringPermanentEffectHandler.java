package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GrantSubtypeToTriggeringPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantSubtypeToTriggeringPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantSubtypeToTriggeringPermanentEffect) effect;
        if (entry.getTriggeringPermanentId() == null) {
            return;
        }
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        if (target == null) {
            return;
        }
        if (!target.getGrantedSubtypes().contains(e.subtype())) {
            target.getGrantedSubtypes().add(e.subtype());
        }
        gameLogService.append(gameData, GameLog.builder().card(target.getCard())
                .text(" becomes a " + e.subtype().getDisplayName() + " in addition to its other types.").build());
    }
}
