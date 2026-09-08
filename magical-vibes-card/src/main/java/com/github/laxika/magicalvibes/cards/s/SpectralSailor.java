package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "76")
@CardRegistration(set = "FDN", collectorNumber = "164")
public class SpectralSailor extends Card {

    public SpectralSailor() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(new DrawCardEffect()),
                "{3}{U}: Draw a card."
        ));
    }
}
