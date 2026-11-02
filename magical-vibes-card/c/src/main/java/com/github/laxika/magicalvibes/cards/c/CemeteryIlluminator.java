package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAnyGraveyardCardAndImprintOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSharesCardTypeWithImprintedCardPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "50")
@CardRegistration(set = "DBL", collectorNumber = "317")
public class CemeteryIlluminator extends Card {

    public CemeteryIlluminator() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileAnyGraveyardCardAndImprintOnSourceEffect());
        addEffect(EffectSlot.ON_ATTACK,
                new ExileAnyGraveyardCardAndImprintOnSourceEffect());
        addStaticEffect(new LookAtTopCardOfOwnLibraryEffect());
        addStaticEffect(new AllowCastFromTopOfLibraryEffect(
                new CardSharesCardTypeWithImprintedCardPredicate(true), true));
    }
}
