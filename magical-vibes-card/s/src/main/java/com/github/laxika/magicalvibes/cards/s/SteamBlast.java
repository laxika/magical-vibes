package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "USG", collectorNumber = "219")
@CardRegistration(set = "BRB", collectorNumber = "81")
public class SteamBlast extends Card {

    public SteamBlast() {
        addSpellEffect(DealDamageEffect.toEachCreature(2).andTo(Subjects.eachPlayer()));
    }
}
