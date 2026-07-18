package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "5ED", collectorNumber = "384")
@CardRegistration(set = "4ED", collectorNumber = "331")
@CardRegistration(set = "10E", collectorNumber = "327")
@CardRegistration(set = "8ED", collectorNumber = "306")
@CardRegistration(set = "7ED", collectorNumber = "305")
@CardRegistration(set = "6ED", collectorNumber = "295")
public class JayemdaeTome extends Card {

    public JayemdaeTome() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new DrawCardEffect()),
                "{4}, {T}: Draw a card."
        ));
    }
}
