package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.ControllerSpellsCantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.Card;

import java.util.Set;

@CardRegistration(set = "10E", collectorNumber = "265")
@CardRegistration(set = "8ED", collectorNumber = "252")
@CardRegistration(set = "PLS", collectorNumber = "80")
@CardRegistration(set = "DPA", collectorNumber = "65")
public class GaeasHerald extends Card {

    public GaeasHerald() {
        addStaticEffect(new ControllerSpellsCantBeCounteredEffect(Subjects.eachPlayer(), Set.of(CardType.CREATURE), null));
    }
}
