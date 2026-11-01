package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentToughness;
import com.github.laxika.magicalvibes.model.effect.DiscoverEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCC", collectorNumber = "4")
@CardRegistration(set = "LCC", collectorNumber = "20")
@CardRegistration(set = "LCC", collectorNumber = "30")
@CardRegistration(set = "LCC", collectorNumber = "124")
public class PantlazaSunFavored extends Card {

    public PantlazaSunFavored() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.DINOSAUR),
                        OncePerTurnTriggerEffect.markOnAcceptance(
                                new MayEffect(
                                        new DiscoverEffect(new TriggeringPermanentToughness()),
                                        "Discover?"))));
    }
}
