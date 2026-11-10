package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.BushidoEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BOK", collectorNumber = "13")
public class KentaroTheSmilingCat extends Card {

    public KentaroTheSmilingCat() {
        addTrigger(Triggers.blocks(), TriggerOptions.ONE_OR_MORE, new BushidoEffect(1));
        addTrigger(Triggers.becomesBlocked(), new BushidoEffect(1));

        addStaticEffect(AlternativeCostForSpellsEffect.genericEqualToManaValue(
                new CardSubtypePredicate(CardSubtype.SAMURAI)));
    }
}
