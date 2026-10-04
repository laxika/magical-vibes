package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "SCG", collectorNumber = "89")
@CardRegistration(set = "DDG", collectorNumber = "53")
@CardRegistration(set = "GN3", collectorNumber = "75")
@CardRegistration(set = "C17", collectorNumber = "136")
@CardRegistration(set = "SCD", collectorNumber = "137")
@CardRegistration(set = "ARC", collectorNumber = "36")
public class DragonspeakerShaman extends Card {

    public DragonspeakerShaman() {
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardSubtypePredicate(CardSubtype.DRAGON), 2, CostModificationScope.SELF));
    }
}
