package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ROE", collectorNumber = "12")
@CardRegistration(set = "V11", collectorNumber = "14")
@CardRegistration(set = "MM2", collectorNumber = "6")
@CardRegistration(set = "UMA", collectorNumber = "7")
@CardRegistration(set = "2X2", collectorNumber = "3")
@CardRegistration(set = "AA3", collectorNumber = "2")
public class UlamogTheInfiniteGyre extends Card {

    public UlamogTheInfiniteGyre() {
        target(TargetFilters.permanent()).addEffect(EffectSlot.ON_SELF_CAST,
                new DestroyTargetPermanentEffect());

        addEffect(EffectSlot.ON_ATTACK, new SacrificePermanentsEffect(
                4, new PermanentTruePredicate(), SacrificeRecipient.DEFENDING_PLAYER));

        addTrigger(Triggers.putIntoGraveyardFromAnywhere(ObjectRelation.THIS),
                new ShuffleGraveyardIntoLibraryEffect(false));
    }
}
