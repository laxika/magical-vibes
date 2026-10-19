package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EventStat;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BRO", collectorNumber = "149")
public class RazeToTheGround extends Card {

    public RazeToTheGround() {
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());
        target(TargetFilters.artifact())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentThenEffect(
                        EventStat.NONE,
                        new DrawCardEffect(),
                        ThenEffectRecipient.CONTROLLER,
                        new PermanentMaxManaValuePredicate(1)));
    }
}
