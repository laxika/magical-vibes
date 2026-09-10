package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ManifestDreadEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;

@CardRegistration(set = "DSK", collectorNumber = "216")
public class GrowingDread extends Card {

    public GrowingDread() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ManifestDreadEffect.forController());
        addEffect(EffectSlot.ON_SELF_OR_ALLY_PERMANENT_TURNS_FACE_UP,
                new PutCounterOnReferencedPermanentEffect(
                        PermanentReference.TRIGGERING, CounterType.PLUS_ONE_PLUS_ONE));
    }
}
