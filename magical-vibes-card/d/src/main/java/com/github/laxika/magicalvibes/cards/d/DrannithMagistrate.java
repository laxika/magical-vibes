package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.PlayerRestriction;
import com.github.laxika.magicalvibes.model.effect.PlayersCantCastSpellsFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "IKO", collectorNumber = "11")
@CardRegistration(set = "TLE", collectorNumber = "2")
public class DrannithMagistrate extends Card {

    public DrannithMagistrate() {
        // Your opponents can't cast spells from anywhere other than their hands.
        addStaticEffect(new PlayersCantCastSpellsFromZonesEffect(Subjects.eachOpponent(),
                PlayerRestriction.castingSpells().fromAnywhereBut(Zone.HAND).ofTheirOwn()));
    }
}
