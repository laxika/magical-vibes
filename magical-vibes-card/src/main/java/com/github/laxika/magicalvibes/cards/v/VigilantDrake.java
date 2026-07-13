package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "7ED", collectorNumber = "109")
public class VigilantDrake extends Card {

    public VigilantDrake() {
        addActivatedAbility(new ActivatedAbility(false, "{2}{U}", List.of(new UntapPermanentsEffect(TapUntapScope.SELF)), "{2}{U}: Untap this creature."));
    }
}
