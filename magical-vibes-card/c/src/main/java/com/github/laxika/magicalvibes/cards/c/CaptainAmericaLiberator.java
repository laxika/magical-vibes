package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "587")
public class CaptainAmericaLiberator extends Card {

    public CaptainAmericaLiberator() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new SearchLibraryEffect(
                        new CardSubtypePredicate(CardSubtype.EQUIPMENT),
                        LibrarySearchDestination.BATTLEFIELD,
                        new ManaValueBound(new Fixed(3), false, 0)),
                "Search your library for an Equipment card with mana value 3 or less?"));
        addTrigger(Triggers.attacks(),
                CreateTokenEffect.whiteSoldier(new AttachmentsOnSource(false, true)));
    }
}
