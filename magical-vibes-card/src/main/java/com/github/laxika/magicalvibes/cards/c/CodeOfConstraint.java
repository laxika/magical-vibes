package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerMainPhase;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RNA", collectorNumber = "35")
public class CodeOfConstraint extends Card {

    public CodeOfConstraint() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(-4, 0))
                .addEffect(EffectSlot.SPELL, new DrawCardEffect(1))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(new ControllerMainPhase(),
                        new TapPermanentsEffect(TapUntapScope.TARGET)))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(new ControllerMainPhase(),
                        new SkipNextUntapEffect(TapUntapScope.TARGET)));
    }
}
