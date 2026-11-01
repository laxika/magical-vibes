package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForSubtypeToBattlefieldAttachedToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "88")
public class BitterheartWitch extends Card {

    public BitterheartWitch() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new MayEffect(
                new SearchLibraryForSubtypeToBattlefieldAttachedToTargetPlayerEffect(CardSubtype.CURSE),
                "Search your library for a Curse card?"
        ));
    }
}
