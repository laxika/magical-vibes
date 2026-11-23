package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtHandEffect;
import com.github.laxika.magicalvibes.service.CardRevealService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtHandEffectHandler implements NormalEffectHandlerBean {

    private final CardRevealService cardRevealService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var look = (LookAtHandEffect) effect;
        UUID playerId = subjectResolutionService.player(gameData, entry, effect, look.player());
        if (playerId == null) {
            return;
        }
        cardRevealService.lookAtHand(gameData, entry.getControllerId(), playerId);
        log.info("Game {} - {} looks at {}'s hand", gameData.id,
                gameData.playerIdToName.get(entry.getControllerId()), gameData.playerIdToName.get(playerId));
    }
}
