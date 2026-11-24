package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.SetTargetPermanentSupertypeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class SetTargetPermanentSupertypeEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetTargetPermanentSupertypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SetTargetPermanentSupertypeEffect) effect;
        for (Permanent target : subjectResolutionService.permanents(gameData, entry, effect, e.subject())) {
            gameData.addEffectOnPermanent(entry, e, target.getId(), Duration.PERMANENT);

            String supertypeName = e.supertype().getDisplayName().toLowerCase();
            String text = e.gained() ? " becomes " + supertypeName + "." : " is no longer " + supertypeName + ".";
            gameLogService.append(gameData, GameLog.builder().card(target.getCard()).text(text).build());

            log.info("Game {} - {}{}", gameData.id, target.getCard().getName(), text);
        }
    }
}
