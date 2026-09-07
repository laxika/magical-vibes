package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAnyGraveyardCardAndImprintOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSharesCardTypeWithImprintedCardPredicate;

@CardRegistration(set = "VOW", collectorNumber = "50")
public class CemeteryIlluminator extends Card {

    public CemeteryIlluminator() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ExileAnyGraveyardCardAndImprintOnSourceEffect());
        addEffect(EffectSlot.ON_ATTACK,
                new ExileAnyGraveyardCardAndImprintOnSourceEffect());
        addEffect(EffectSlot.STATIC, new LookAtTopCardOfOwnLibraryEffect());
        addEffect(EffectSlot.STATIC, new AllowCastFromTopOfLibraryEffect(
                new CardSharesCardTypeWithImprintedCardPredicate(true), true));
    }
}
