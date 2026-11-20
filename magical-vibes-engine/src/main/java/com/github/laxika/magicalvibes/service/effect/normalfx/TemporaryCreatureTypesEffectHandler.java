package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantAllCreatureTypesToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves a temporary grant of creature types without granting a keyword ability. */
@Component
@RequiredArgsConstructor
public class TemporaryCreatureTypesEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantAllCreatureTypesToOwnCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantAllCreatureTypesToOwnCreaturesEffect) effect;
        CardEffect applied = effect;
        PermanentSubject subject = Subjects.target();
        if (grant.subject().isSet()) {
            // The creatures the set names as the ability resolves ("creatures you control", "creatures target player
            // controls").
            subject = grant.subject();
            applied = new GrantAllCreatureTypesToOwnCreaturesEffect(Subjects.target(), grant.filter());
        }
        for (Permanent target : subjectResolutionService.permanents(gameData, entry, effect, subject)) {
            gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                    entry.getCard().getName(), null, entry.getControllerId(), applied,
                    target.getId(), null, null, Duration.UNTIL_END_OF_TURN, 0));
        }
    }
}
