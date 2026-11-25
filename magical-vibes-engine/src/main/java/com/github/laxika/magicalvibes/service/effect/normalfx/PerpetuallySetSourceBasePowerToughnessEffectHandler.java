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

/** Records a perpetual base-power and base-toughness setter on the physical card of each permanent the subject names. */
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
            // A perpetual change is made to the physical card, not to its copiable values (CR 707.2): a copy of the
            // permanent copies the printed values, and the layered pass applies the stored setter in layer 7b.
            Card card = permanent.getOriginalCard() != null ? permanent.getOriginalCard() : permanent.getCard();
            gameData.perpetualCardBasePowerToughness.put(card.getId(), new GameData.PerpetualBasePowerToughness(
                    set.power(), set.toughness(), gameData.nextTimestamp()));
        }
    }
}
