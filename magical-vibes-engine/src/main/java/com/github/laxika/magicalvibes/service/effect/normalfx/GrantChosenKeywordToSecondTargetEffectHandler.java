package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenKeywordToSecondTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GrantChosenKeywordToSecondTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantChosenKeywordToSecondTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantChosenKeywordToSecondTargetEffect) effect;
        if (entry.getTargetIds() == null || entry.getTargetIds().size() < 2) {
            return;
        }

        UUID secondTargetId = entry.getTargetIds().get(1);
        Permanent target = gameQueryService.findPermanentById(gameData, secondTargetId);
        if (target == null) {
            return;
        }

        playerInputService.beginKeywordChoice(gameData, entry.getControllerId(), target.getId(), e.options());
    }
}
