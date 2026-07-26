package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtHandEffect;
import com.github.laxika.magicalvibes.service.CardRevealService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtHandEffectHandler implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        UUID targetPlayerId = entry.getTargetId();
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        String casterName = gameData.playerIdToName.get(entry.getControllerId());

        cardRevealService.lookAtHand(gameData, entry.getControllerId(), targetPlayerId);

        log.info("Game {} - {} looks at {}'s hand", gameData.id, casterName, targetName);
    
    }
}
