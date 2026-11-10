package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerIsMonarch;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMM", collectorNumber = "316")
@CardRegistration(set = "CMM", collectorNumber = "569")
@CardRegistration(set = "CMM", collectorNumber = "651")
@CardRegistration(set = "LCC", collectorNumber = "251")
public class RegalBehemoth extends Card {

    public RegalBehemoth() {
        // Trample is auto-loaded from Scryfall.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new BecomeMonarchEffect());

        // Whenever you tap a land for mana while you're the monarch, add an additional one mana of any color.
        addTrigger(Triggers.youTapLandForMana(),
                TriggerOptions.DEFAULT.withEventCondition(new ControllerIsMonarch()),
                AddOneOfEachManaTypeProducedByLandEffect.ofAnyColor());
    }
}
