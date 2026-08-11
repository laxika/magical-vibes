package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellOrAbilityAndDestroySourceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CounterSpellOrAbilityAndDestroySourceEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;
    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterSpellOrAbilityAndDestroySourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }

        StackEntry targetEntry = counterSupport.findCounterTarget(gameData, targetId, entry);
        if (targetEntry == null) {
            return;
        }

        UUID sourcePermanentId = isAbility(targetEntry) ? targetEntry.getSourcePermanentId() : null;
        counterSupport.counterSpell(gameData, entry, targetEntry);

        if (sourcePermanentId == null) {
            return;
        }
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (sourcePermanent != null) {
            destructionSupport.tryDestroyAndLog(gameData, sourcePermanent, entry.getCard().getName(), false);
        }
    }

    private boolean isAbility(StackEntry entry) {
        return entry.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                || entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY;
    }
}
