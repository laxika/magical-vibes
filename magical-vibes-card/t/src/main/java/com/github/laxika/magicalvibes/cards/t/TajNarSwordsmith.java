package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect.Resource;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect;

@CardRegistration(set = "MRD", collectorNumber = "27")
@CardRegistration(set = "MM2", collectorNumber = "36")
@CardRegistration(set = "C17", collectorNumber = "77")
public class TajNarSwordsmith extends Card {

    public TajNarSwordsmith() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(SequenceEffect.of(
                        EachPlayerPaysAnyManaEffect.byYou(Resource.X_MANA),
                        new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.EQUIPMENT),
                                LibrarySearchDestination.BATTLEFIELD,
                                new ManaValueBound(AmountThisWay.manaPaid(), false, 0))),
                        "Pay {X} to search your library for an Equipment card with mana value X or less?"));
    }
}
