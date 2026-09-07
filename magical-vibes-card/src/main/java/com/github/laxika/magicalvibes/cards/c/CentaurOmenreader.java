package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "FUT", collectorNumber = "143")
public class CentaurOmenreader extends Card {

    public CentaurOmenreader() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new SourceIsTapped(),
                new ReduceCastCostForMatchingSpellsEffect(
                        new CardTypePredicate(CardType.CREATURE), 2, CostModificationScope.SELF)));
    }
}
