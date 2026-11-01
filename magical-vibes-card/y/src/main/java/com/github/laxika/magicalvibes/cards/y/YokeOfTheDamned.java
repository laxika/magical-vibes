package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CON", collectorNumber = "57")
@CardRegistration(set = "DDJ", collectorNumber = "68")
public class YokeOfTheDamned extends Card {

    public YokeOfTheDamned() {
        // Enchant creature
        target(TargetFilters.creature());
        // When a creature dies, destroy enchanted creature.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANY),
                new DestroyReferencedPermanentEffect(PermanentReference.ATTACHED));
    }
}
