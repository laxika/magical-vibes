package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourceToughness;
import com.github.laxika.magicalvibes.model.effect.DiscoverEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCC", collectorNumber = "60")
@CardRegistration(set = "LCC", collectorNumber = "92")
public class DinosaurEgg extends Card {

    public DinosaurEgg() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new MayEffect(
                new DiscoverEffect(new SourceToughness()),
                "Discover?"));
    }
}
