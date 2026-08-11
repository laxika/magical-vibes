package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "INV", collectorNumber = "138")
public class BreathOfDarigaaz extends Card {

    public BreathOfDarigaaz() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{2}"));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(new Kicked(),
                new MassDamageEffect(1, false, true,
                        new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))),
                new MassDamageEffect(4, false, true,
                        new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING)))
        ));
    }
}
