package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.EnchantedPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;

@CardRegistration(set = "SOM", collectorNumber = "53")
public class VolitionReins extends Card {

    public VolitionReins() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                new EnchantedPermanentMatches(new PermanentIsTappedPredicate(), "enchanted permanent is tapped"),
                new UntapPermanentsEffect(TapUntapScope.ENCHANTED)));
        addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect());
    }
}
