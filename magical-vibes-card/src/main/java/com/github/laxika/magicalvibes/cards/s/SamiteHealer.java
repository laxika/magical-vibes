package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "5ED", collectorNumber = "58")
@CardRegistration(set = "4ED", collectorNumber = "47")
@CardRegistration(set = "10E", collectorNumber = "38")
@CardRegistration(set = "9ED", collectorNumber = "39")
@CardRegistration(set = "8ED", collectorNumber = "41")
@CardRegistration(set = "7ED", collectorNumber = "38")
@CardRegistration(set = "6ED", collectorNumber = "40")
public class SamiteHealer extends Card {

    public SamiteHealer() {
        addActivatedAbility(new ActivatedAbility(true, null, List.of(PreventDamageEffect.nextToAny(1)), "{T}: Prevent the next 1 damage that would be dealt to any target this turn."));
    }
}
