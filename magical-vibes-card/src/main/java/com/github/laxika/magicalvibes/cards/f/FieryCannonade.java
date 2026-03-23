package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "XLN", collectorNumber = "143")
public class FieryCannonade extends Card {

    public FieryCannonade() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(2, false, false, new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.PIRATE))));
    }
}
