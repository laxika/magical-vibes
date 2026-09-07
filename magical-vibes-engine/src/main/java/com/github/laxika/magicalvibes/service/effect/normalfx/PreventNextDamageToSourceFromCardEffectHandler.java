package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TargetSourceDamagePreventionShield;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageToSourceFromCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PreventNextDamageToSourceFromCardEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventNextDamageToSourceFromCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getSourcePermanentId();
        UUID sourceCardId = ((PreventNextDamageToSourceFromCardEffect) effect).damageSourceCardId();
        if (targetId == null || sourceCardId == null) {
            return;
        }
        gameData.targetSourceDamagePreventionShields.add(
                new TargetSourceDamagePreventionShield(targetId, sourceCardId, 1));
        Card sourceCard = gameData.stack.stream()
                .filter(stackEntry -> stackEntry.getCard().getId().equals(sourceCardId))
                .map(StackEntry::getCard)
                .findFirst().orElse(entry.getCard());
        gameLogService.append(gameData, GameLog.builder()
                .text("The next 1 damage that would be dealt to ")
                .card(entry.getCard())
                .text(" by ")
                .card(sourceCard)
                .text(" this turn is prevented.")
                .build());
    }
}
