package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "DRK", collectorNumber = "18")
public class TivadarsCrusade extends Card {

    public TivadarsCrusade() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(
                new PermanentHasSubtypePredicate(CardSubtype.GOBLIN)));
    }
}
