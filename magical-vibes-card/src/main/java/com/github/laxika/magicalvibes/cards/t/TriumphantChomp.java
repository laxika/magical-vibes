package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LCI", collectorNumber = "170")
public class TriumphantChomp extends Card {

    public TriumphantChomp() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL,
                new DealDamageToTargetCreatureEffect(new Max(
                        new Fixed(2),
                        new GreatestPowerAmongControlled(
                                new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR)))));
    }
}
