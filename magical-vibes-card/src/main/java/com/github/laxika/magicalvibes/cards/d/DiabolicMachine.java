package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "5ED", collectorNumber = "363")
@CardRegistration(set = "4ED", collectorNumber = "314")
public class DiabolicMachine extends Card {

    public DiabolicMachine() {
        addActivatedAbility(new ActivatedAbility(false, "{3}", List.of(new RegenerateEffect()), "{3}: Regenerate Diabolic Machine."));
    }
}
