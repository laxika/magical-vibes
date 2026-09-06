package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LEG", collectorNumber = "44")
public class AcidRain extends Card {

    public AcidRain() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(
                new PermanentHasSubtypePredicate(CardSubtype.FOREST)));
    }
}
