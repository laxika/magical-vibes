package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "WHO", collectorNumber = "9")
@CardRegistration(set = "WHO", collectorNumber = "332")
@CardRegistration(set = "WHO", collectorNumber = "614")
@CardRegistration(set = "WHO", collectorNumber = "923")
public class ClaraOswald extends Card {

    public ClaraOswald() {
        addEffect(EffectSlot.STATIC,
                new AdditionalTriggeredAbilityEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.DOCTOR)));
    }
}
