package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.EnchantedPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOM", collectorNumber = "53")
public class VolitionReins extends Card {

    public VolitionReins() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ConditionalEffect(
                new EnchantedPermanentMatches(new PermanentIsTappedPredicate(), "enchanted permanent is tapped"),
                new UntapPermanentsEffect(TapUntapScope.ENCHANTED)));
        addStaticEffect(new ControlEnchantedCreatureEffect());
    }
}
