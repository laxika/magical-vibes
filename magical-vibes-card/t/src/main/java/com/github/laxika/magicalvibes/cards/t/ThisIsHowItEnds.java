package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCreatureThenOwnerFacesVillainousChoiceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "WHO", collectorNumber = "70")
@CardRegistration(set = "WHO", collectorNumber = "373")
@CardRegistration(set = "WHO", collectorNumber = "675")
@CardRegistration(set = "WHO", collectorNumber = "964")
public class ThisIsHowItEnds extends Card {

    public ThisIsHowItEnds() {
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL,
                new ShuffleTargetCreatureThenOwnerFacesVillainousChoiceEffect());
    }
}
