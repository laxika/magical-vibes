package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatedPermanentsAttackingEffect;
import com.github.laxika.magicalvibes.model.effect.MakeChosenPermanentAttackingEffect;
import java.util.ArrayList;
import java.util.List;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Lets the controller choose a defender for each created attacking permanent.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MakeCreatedPermanentsAttackingEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MakeCreatedPermanentsAttackingEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<CardEffect> choices = new ArrayList<>();
        for (var createdId : entry.getCreatedPermanentIds()) {
            Permanent created = gameQueryService.findPermanentById(gameData, createdId);
            if (created != null && gameQueryService.isCreature(gameData, created)) {
                choices.add(new MakeChosenPermanentAttackingEffect(createdId));
            }
        }
        int effectIndex = entry.getEffectsToResolve().indexOf(effect);
        if (effectIndex < 0) {
            throw new IllegalStateException("Current effect is not present in its stack entry");
        }
        entry.insertEffectsToResolve(effectIndex + 1, choices);
        log.info("Game {} - {} permanent(s) made attacking by {}",
                gameData.id, entry.getCreatedPermanentIds().size(), entry.getCard().getName());
    }
}
