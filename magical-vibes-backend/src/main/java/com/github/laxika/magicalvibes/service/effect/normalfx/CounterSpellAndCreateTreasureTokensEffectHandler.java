package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndCreateTreasureTokensEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CounterSpellAndCreateTreasureTokensEffectHandler implements NormalEffectHandlerBean {

    private final CounterSupport counterSupport;
    private final GameQueryService gameQueryService;
    private final PermanentControlSupport permanentControlSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CounterSpellAndCreateTreasureTokensEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry targetEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                targetEntry = se;
                break;
            }
        }

        if (targetEntry == null) {
            log.info("Game {} - Counter target no longer on stack", gameData.id);
            return;
        }

        int manaValue = targetEntry.getCard().getManaValue() + targetEntry.getXValue();

        boolean countered = false;
        if (gameQueryService.isUncounterable(gameData, targetEntry.getCard())) {
            log.info("Game {} - {} cannot be countered", gameData.id, targetEntry.getCard().getName());
        } else if (gameQueryService.isProtectedFromCounterBySpellColor(gameData, targetEntry.getControllerId(), entry)) {
            log.info("Game {} - {} cannot be countered by {} spells",
                    gameData.id, targetEntry.getCard().getName(),
                    entry.getCard().getColor().name().toLowerCase());
        } else {
            counterSupport.counterSpell(gameData, entry, targetEntry);
            countered = true;
        }

        if (!countered) {
            log.info("Game {} - {} could not counter {}, but still creating Treasure tokens",
                    gameData.id, entry.getCard().getName(), targetEntry.getCard().getName());
        }

        if (manaValue > 0) {
            CreateTokenEffect treasures = CreateTokenEffect.ofTreasureToken(manaValue);
            permanentControlSupport.applyCreateToken(
                    gameData, entry.getControllerId(), treasures, entry.getCard().getSetCode());
        }
    }
}
