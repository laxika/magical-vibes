package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantRandomColorToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves an effect that adds one currently missing color to its source permanently. */
@Component
@RequiredArgsConstructor
public class GrantRandomColorToSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantRandomColorToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourceId = entry.getSourcePermanentId();
        if (sourceId == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        if (source == null) {
            return;
        }

        Set<CardColor> existingColors = gameQueryService.getEffectiveColors(gameData, source);
        List<CardColor> missingColors = Arrays.stream(CardColor.values())
                .filter(color -> !existingColors.contains(color))
                .toList();
        if (missingColors.isEmpty()) {
            return;
        }

        CardColor chosenColor = missingColors.get(ThreadLocalRandom.current().nextInt(missingColors.size()));
        GrantColorEffect grant = new GrantColorEffect(chosenColor, GrantScope.SELF);
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), sourceId, entry.getControllerId(),
                grant, sourceId, null, null, EffectDuration.PERMANENT, 0));
    }
}
