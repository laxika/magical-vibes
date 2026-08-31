package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "110")
public class RummagingWizard extends Card {

    public RummagingWizard() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(new SurveilEffect(1)),
                "{2}{U}: Surveil 1."
        ));
    }
}
