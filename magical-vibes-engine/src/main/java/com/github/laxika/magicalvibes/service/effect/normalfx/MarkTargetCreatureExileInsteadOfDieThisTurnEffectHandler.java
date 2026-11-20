package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MarkTargetCreatureExileInsteadOfDieThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MarkTargetCreatureExileInsteadOfDieThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MarkTargetCreatureExileInsteadOfDieThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var mark = (MarkTargetCreatureExileInsteadOfDieThisTurnEffect) effect;
        if (!(mark.subject() instanceof PermanentSubject.Target)) {
            for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, mark, mark.subject())) {
                gameData.exileInsteadOfDying(entry, permanent.getId(), mark.trackWithSource());
            }
            return;
        }
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null || !gameQueryService.isCreature(gameData, target)) {
            return;
        }
        gameData.exileInsteadOfDying(entry, target.getId(), mark.trackWithSource());
    }
}
