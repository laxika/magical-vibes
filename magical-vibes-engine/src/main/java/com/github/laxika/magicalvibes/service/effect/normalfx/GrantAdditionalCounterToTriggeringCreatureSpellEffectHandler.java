package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalCounterToTriggeringCreatureSpellEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class GrantAdditionalCounterToTriggeringCreatureSpellEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantAdditionalCounterToTriggeringCreatureSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GrantAdditionalCounterToTriggeringCreatureSpellEffect grant =
                (GrantAdditionalCounterToTriggeringCreatureSpellEffect) effect;
        if (entry.getTriggeringCardId() == null) {
            return;
        }

        StackEntry spellEntry = gameQueryService.findStackEntryByCardId(
                gameData, entry.getTriggeringCardId());
        if (spellEntry == null) {
            return;
        }

        gameData.spellEntryCounters
                .computeIfAbsent(entry.getTriggeringCardId(), ignored -> new ConcurrentHashMap<>())
                .merge(grant.counterType(), grant.count(), Integer::sum);
        gameLogService.append(gameData, GameLog.cardThen(spellEntry.getCard(),
                " enters with " + grant.count() + " additional " + grant.counterType()
                        + " counter(s)."));
    }
}
