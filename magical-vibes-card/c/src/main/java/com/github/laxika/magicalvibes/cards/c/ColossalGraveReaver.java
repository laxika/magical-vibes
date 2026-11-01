package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDC", collectorNumber = "50")
@CardRegistration(set = "TDC", collectorNumber = "90")
public class ColossalGraveReaver extends Card {

    public ColossalGraveReaver() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MillEffect(3, MillRecipient.CONTROLLER));
        addEffect(EffectSlot.ON_ATTACK,
                new MillEffect(3, MillRecipient.CONTROLLER));
        addTrigger(new TriggerCondition(Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.NONTOKEN,
                ObjectRelation.YOU_OWN)),
                TriggerOptions.ONE_OR_MORE,
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                        .filter(new CardTypePredicate(CardType.CREATURE))
                        .mandatory(true)
                        .eventCardIdsOnly(true)
                        .build());
    }
}
