package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterTriggeringSpellAndCreateTokensEffect;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Dovescape's counter-and-create-tokens trigger. */
@Component
@RequiredArgsConstructor
public class CounterTriggeringSpellAndCreateTokensEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;
    private final CreateTokenEffectHandler createTokenEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterTriggeringSpellAndCreateTokensEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID triggeringSpellId = entry.getTriggeringCardId();
        if (triggeringSpellId != null) {
            StackEntry targetEntry = counterSupport.findCounterTarget(gameData, triggeringSpellId, entry);
            if (targetEntry != null) {
                counterSupport.counterSpell(gameData, entry, targetEntry);
            }
        }

        UUID casterId = entry.getTargetId();
        if (casterId == null || !gameData.playerIds.contains(casterId)) {
            return;
        }
        CounterTriggeringSpellAndCreateTokensEffect counterAndCreate =
                (CounterTriggeringSpellAndCreateTokensEffect) effect;
        createTokenEffectHandler.resolveForController(gameData, entry, counterAndCreate.token(), casterId);
    }
}
