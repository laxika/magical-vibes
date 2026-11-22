package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasChosenSpellColorPredicate;
import com.github.laxika.magicalvibes.model.effect.ClearChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorAtResolutionEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "INV", collectorNumber = "87")
@CardRegistration(set = "2X2", collectorNumber = "67")
@CardRegistration(set = "C13", collectorNumber = "66")
@CardRegistration(set = "CMA", collectorNumber = "43")
public class WashOut extends Card {

    public WashOut() {
        addSpellEffect(new ChooseColorAtResolutionEffect());
        addSpellEffect(ReturnToHandEffect.allPermanentsMatching(new PermanentHasChosenSpellColorPredicate()));
        addSpellEffect(new ClearChosenColorEffect());
    }
}
