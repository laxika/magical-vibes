package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForNextMatchingSpellEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "YWOE", collectorNumber = "9")
public class DraconicDebut extends Card {

    public DraconicDebut() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(new XValue()));
        addEffect(EffectSlot.SPELL, new ReduceCastCostForNextMatchingSpellEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardSubtypePredicate(CardSubtype.DRAGON))),
                new XValue(), false, false));
    }
}
