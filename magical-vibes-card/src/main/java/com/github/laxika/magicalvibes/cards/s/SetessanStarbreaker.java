package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "BNG", collectorNumber = "139")
public class SetessanStarbreaker extends Card {

    public SetessanStarbreaker() {
        target(new PermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.AURA),
                "Target must be an Aura"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new DestroyTargetPermanentEffect(), "Destroy target Aura?"));
    }
}
