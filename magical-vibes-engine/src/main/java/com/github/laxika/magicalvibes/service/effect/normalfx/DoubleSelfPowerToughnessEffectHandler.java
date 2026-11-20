package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleSelfPowerToughnessEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.MaroGoneNutsSupport;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class DoubleSelfPowerToughnessEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DoubleSelfPowerToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DoubleSelfPowerToughnessEffect) effect;
        Permanent self = subjectResolutionService.permanent(gameData, entry, effect, e.subject());
        // A target that stopped being a creature is no longer a legal "target creature"; the source form never
        // checked.
        if (self == null || e.subject().declaresTarget() && !gameQueryService.isCreature(gameData, self)) {
            return;
        }

        int currentPower = gameQueryService.getEffectivePower(gameData, self);
        int currentToughness = e.powerOnly()
                ? 0 : gameQueryService.getEffectiveToughness(gameData, self);

        gameData.addPowerToughnessBoost(entry, self.getId(),

                MaroGoneNutsSupport.doublingIncrease(gameData, effect, currentPower),

                MaroGoneNutsSupport.doublingIncrease(gameData, effect, currentToughness));

        
        gameLogService.append(gameData, GameLog.builder().card(self.getCard()).text("'s power and toughness are doubled (+" + currentPower + "/+" + currentToughness + ").").build());

        log.info("Game {} - {} power/toughness doubled (+{}/+{})", gameData.id, self.getCard().getName(), currentPower, currentToughness);
    }
}
