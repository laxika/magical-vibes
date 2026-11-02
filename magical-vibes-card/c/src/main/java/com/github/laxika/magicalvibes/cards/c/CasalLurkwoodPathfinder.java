package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "1241")
@CardRegistration(set = "SLX", collectorNumber = "29")
public class CasalLurkwoodPathfinder extends Card {

    public CasalLurkwoodPathfinder() {
        setBackFaceCard(new CasalPathbreakerOwlbear());

        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new SearchLibraryEffect(new CardSubtypePredicate(CardSubtype.FOREST),
                        LibrarySearchDestination.BATTLEFIELD_TAPPED));
        addEffect(EffectSlot.ON_ATTACK,
                new MayPayManaEffect("{1}{G}", new TransformSelfEffect(),
                        "Pay {1}{G} to transform Casal?"));
    }

    @Override
    public String getBackFaceClassName() {
        return "CasalPathbreakerOwlbear";
    }
}
