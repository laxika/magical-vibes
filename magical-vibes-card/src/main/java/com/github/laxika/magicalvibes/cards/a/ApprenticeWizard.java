package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "4ED", collectorNumber = "61")
public class ApprenticeWizard extends Card {

    public ApprenticeWizard() {
        // {U}, {T}: Add {C}{C}{C}.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(new AwardManaEffect(ManaColor.COLORLESS, 3)),
                "{U}, {T}: Add {C}{C}{C}."
        ));
    }
}
