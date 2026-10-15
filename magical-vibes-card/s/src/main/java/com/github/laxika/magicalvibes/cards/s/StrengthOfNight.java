package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "APC", collectorNumber = "86")
public class StrengthOfNight extends Card {

    public StrengthOfNight() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{B}"));
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(1, 1));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new Kicked(),
                new BoostAllOwnCreaturesEffect(2, 2, new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE))));
    }
}
