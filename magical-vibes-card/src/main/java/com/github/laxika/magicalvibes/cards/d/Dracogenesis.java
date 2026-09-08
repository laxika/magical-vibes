package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "TDM", collectorNumber = "105")
public class Dracogenesis extends Card {

    public Dracogenesis() {
        addEffect(EffectSlot.STATIC, new AlternativeCostForSpellsEffect(
                "{0}", new CardSubtypePredicate(CardSubtype.DRAGON)));
    }
}
