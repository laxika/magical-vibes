package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.AllowPlayAndCastCardsExiledWithCroakCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringPermanentCardFromLibraryWithCroakCounterEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "238")
@CardRegistration(set = "DBL", collectorNumber = "505")
public class GrolnokTheOmnivore extends Card {

    public GrolnokTheOmnivore() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.FROG),
                        new MillEffect(3, MillRecipient.CONTROLLER)));
        addTrigger(Triggers.putIntoGraveyardFrom(Zone.LIBRARY, Triggers.PERMANENT_CARD, ObjectRelation.YOU_OWN),
                new ExileTriggeringPermanentCardFromLibraryWithCroakCounterEffect());
        addStaticEffect(new AllowPlayAndCastCardsExiledWithCroakCountersEffect());
    }
}
