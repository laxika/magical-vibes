package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeToChosenPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GrantSubtypeToChosenPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantSubtypeToChosenPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GrantSubtypeToChosenPermanentEffect grant = (GrantSubtypeToChosenPermanentEffect) effect;
        if (entry.getChosenPermanentId() == null) {
            return;
        }

        Permanent chosen = gameQueryService.findPermanentById(gameData, entry.getChosenPermanentId());
        if (chosen == null || chosen.getGrantedSubtypes().contains(grant.subtype())) {
            return;
        }

        chosen.getGrantedSubtypes().add(grant.subtype());
        gameLogService.append(gameData, GameLog.builder()
                .card(chosen.getCard())
                .text(" becomes a " + grant.subtype().getDisplayName()
                        + " in addition to its other types.")
                .build());
    }
}
