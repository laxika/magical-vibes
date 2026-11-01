package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.SourceManaValueMinusOne;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZNR", collectorNumber = "233")
public class OrahSkyclaveHierophant extends Card {

    public OrahSkyclaveHierophant() {
        ReturnCardFromGraveyardEffect returnCleric = ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardSubtypePredicate(CardSubtype.CLERIC))
                .targetGraveyard(true)
                .dynamicMaxManaValue(new SourceManaValueMinusOne())
                .build();

        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER_YOU_CONTROL), new TriggeringCardConditionalEffect(
                new CardSubtypePredicate(CardSubtype.CLERIC), returnCleric));
        addTrigger(Triggers.dies(ObjectRelation.THIS), returnCleric);
    }
}
