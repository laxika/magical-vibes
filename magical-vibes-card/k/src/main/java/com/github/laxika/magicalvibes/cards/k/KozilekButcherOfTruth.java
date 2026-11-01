package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.ShuffleGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ROE", collectorNumber = "6")
@CardRegistration(set = "MM2", collectorNumber = "5")
@CardRegistration(set = "UMA", collectorNumber = "6")
@CardRegistration(set = "2X2", collectorNumber = "2")
public class KozilekButcherOfTruth extends Card {

    public KozilekButcherOfTruth() {
        addEffect(EffectSlot.ON_SELF_CAST, new DrawCardEffect(4));

        addEffect(EffectSlot.ON_ATTACK, new SacrificePermanentsEffect(
                4, new PermanentTruePredicate(), SacrificeRecipient.DEFENDING_PLAYER));

        addTrigger(Triggers.putIntoGraveyardFromAnywhere(ObjectRelation.THIS),
                new ShuffleGraveyardIntoLibraryEffect(false));
    }
}
