package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "RTR", collectorNumber = "21")
public class SelesnyaSentry extends Card {

    public SelesnyaSentry() {
        addActivatedAbility(new ActivatedAbility(false, "{5}{G}", List.of(new RegenerateEffect()), "{5}{G}: Regenerate Selesnya Sentry."));
    }
}
