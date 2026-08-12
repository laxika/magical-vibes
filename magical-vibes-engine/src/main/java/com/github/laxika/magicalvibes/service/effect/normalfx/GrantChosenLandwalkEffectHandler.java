package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenLandwalkEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GrantChosenLandwalkEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantChosenLandwalkEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantChosenLandwalkEffect) effect;
        UUID recipientId = e.scope() == GrantScope.SELF
                ? entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId()
                : entry.getTargetId();
        Permanent recipient = gameQueryService.findPermanentById(gameData, recipientId);
        if (recipient == null) {
            return;
        }

        playerInputService.beginLandwalkTypeChoice(gameData, entry.getControllerId(), recipient.getId());
    }
}
