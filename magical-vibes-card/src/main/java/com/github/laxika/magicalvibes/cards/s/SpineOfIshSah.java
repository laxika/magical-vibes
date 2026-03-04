package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

@CardRegistration(set = "MBS", collectorNumber = "136")
public class SpineOfIshSah extends Card {

    public SpineOfIshSah() {
        // When Spine of Ish Sah enters the battlefield, destroy target permanent.
        setTargetFilter(new PermanentPredicateTargetFilter(
                new PermanentTruePredicate(),
                "Target can be any permanent"
        ));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DestroyTargetPermanentEffect());

        // When Spine of Ish Sah is put into a graveyard from the battlefield,
        // return Spine of Ish Sah to its owner's hand.
        addEffect(EffectSlot.ON_DEATH, new ReturnCardFromGraveyardEffect(
                GraveyardChoiceDestination.HAND,
                new CardIsSelfPredicate(),
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                false, true, false, null, false
        ));
    }
}
