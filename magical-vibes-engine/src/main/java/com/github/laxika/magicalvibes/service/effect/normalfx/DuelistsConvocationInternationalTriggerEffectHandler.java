package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DuelistsConvocationInternationalTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DuelistsConvocationInternationalTriggerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DuelistsConvocationInternationalTriggerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        if (source == null || source.getChosenNumberDigits().size() != 10) {
            return;
        }

        int manaValue = entry.getEventValue();
        List<Integer> digits = source.getChosenNumberDigits();
        int crossedPosition = -1;
        for (int i = 0; i < digits.size(); i++) {
            if (!source.getCrossedNumberDigitPositions().contains(i) && digits.get(i) == manaValue) {
                crossedPosition = i;
                break;
            }
        }
        if (crossedPosition < 0) {
            return;
        }

        source.getCrossedNumberDigitPositions().add(crossedPosition);
        int effectIndex = effectIndex(entry, effect);
        if (effectIndex < 0) {
            return;
        }

        List<CardEffect> followUps = new ArrayList<>();
        followUps.add(new DrawCardEffect(1));
        if (source.getCrossedNumberDigitPositions().size() == 10) {
            followUps.add(new WinGameEffect());
        }
        entry.insertEffectsToResolve(effectIndex + 1, followUps);
    }

    private int effectIndex(StackEntry entry, CardEffect effect) {
        for (int i = 0; i < entry.getEffectsToResolve().size(); i++) {
            if (entry.getEffectsToResolve().get(i) == effect) {
                return i;
            }
        }
        return -1;
    }
}
