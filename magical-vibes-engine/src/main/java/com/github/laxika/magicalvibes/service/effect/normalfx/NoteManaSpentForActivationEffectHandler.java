package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.NoteManaSpentForActivationEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.EnumMap;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Copies the mana spent to activate the ability (recorded at activation time by
 * {@code AbilityActivationService}) onto the source permanent as its noted mana.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NoteManaSpentForActivationEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return NoteManaSpentForActivationEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourceId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        if (source == null) {
            log.info("Game {} - Source permanent gone, noting mana spent is skipped", gameData.id);
            return;
        }

        UUID sourceCardId = source.getCard().getId();
        var spent = entry.getActivationManaSpent();
        EnumMap<ManaColor, Integer> noted = new EnumMap<>(ManaColor.class);
        if (spent != null) {
            noted.putAll(spent);
        }
        gameData.notedMana.put(sourceCardId, noted);
        log.info("Game {} - {} notes mana spent {}", gameData.id, source.getCard().getName(), spent);
    }
}
