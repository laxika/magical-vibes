package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "POR", collectorNumber = "90")
@CardRegistration(set = "6ED", collectorNumber = "124")
@CardRegistration(set = "HML", collectorNumber = "46a")
@CardRegistration(set = "HML", collectorNumber = "46b")
@CardRegistration(set = "ME2", collectorNumber = "87")
public class DrySpell extends Card {

    public DrySpell() {
        // Dry Spell deals 1 damage to each creature and each player.
        addSpellEffect(DealDamageEffect.toEachCreature(1).andTo(Subjects.eachPlayer()));
    }
}
