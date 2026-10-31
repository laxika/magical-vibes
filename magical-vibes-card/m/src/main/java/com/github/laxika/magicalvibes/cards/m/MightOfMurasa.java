package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ZNR", collectorNumber = "194")
public class MightOfMurasa extends Card {

    public MightOfMurasa() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{2}{G}"));

        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(3, 3))
                .addSpellEffect(new ConditionalEffect(
                        new Kicked(), new BoostTargetCreatureEffect(2, 2)));
    }
}
