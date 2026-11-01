package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "194")
@CardRegistration(set = "MKM", collectorNumber = "358")
public class CuriousCadaver extends Card {

    public CuriousCadaver() {
        addTrigger(new TriggerCondition(Triggers.sacrificed(null, ObjectRelation.YOU_CONTROL),
                AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.CLUE),
                        new ReturnSourceCardFromGraveyardToOwnerHandEffect()
                ));
    }
}
