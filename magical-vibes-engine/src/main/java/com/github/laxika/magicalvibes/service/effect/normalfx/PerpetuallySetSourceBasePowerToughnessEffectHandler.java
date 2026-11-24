package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallySetSourceBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Applies a perpetual base-power and base-toughness change to the card of each permanent the subject names. */
@Component
@RequiredArgsConstructor
public class PerpetuallySetSourceBasePowerToughnessEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallySetSourceBasePowerToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var set = (PerpetuallySetSourceBasePowerToughnessEffect) effect;
        for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect, set.subject())) {
            Card copy = permanent.getCard().createRuntimeCopy();
            copy.setPower(set.power());
            copy.setToughness(set.toughness());
            copy.freeze();
            permanent.exchangeCard(copy);
        }
    }
}
