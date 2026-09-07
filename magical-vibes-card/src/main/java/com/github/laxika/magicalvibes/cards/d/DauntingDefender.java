package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventFixedDamagePerSourceToCreaturesYouControlEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ONS", collectorNumber = "21")
public class DauntingDefender extends Card {

    public DauntingDefender() {
        addEffect(EffectSlot.STATIC, new PreventFixedDamagePerSourceToCreaturesYouControlEffect(
                new PermanentHasSubtypePredicate(CardSubtype.CLERIC), 1));
    }
}
