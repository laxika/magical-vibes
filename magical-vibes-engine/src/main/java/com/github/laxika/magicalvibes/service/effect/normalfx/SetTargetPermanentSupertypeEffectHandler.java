package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SetTargetPermanentSupertypeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetTargetPermanentSupertypeEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetTargetPermanentSupertypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SetTargetPermanentSupertypeEffect) effect;
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        if (e.gained()) {
            target.getPersistentRemovedSupertypes().remove(e.supertype());
            target.getPersistentGrantedSupertypes().add(e.supertype());
        } else {
            target.getPersistentGrantedSupertypes().remove(e.supertype());
            target.getPersistentRemovedSupertypes().add(e.supertype());
        }

        String supertypeName = e.supertype().getDisplayName().toLowerCase();
        String text = e.gained() ? " becomes " + supertypeName + "." : " is no longer " + supertypeName + ".";
        gameLogService.append(gameData, GameLog.builder().card(target.getCard()).text(text).build());

        log.info("Game {} - {}{}", gameData.id, target.getCard().getName(), text);
    }
}
