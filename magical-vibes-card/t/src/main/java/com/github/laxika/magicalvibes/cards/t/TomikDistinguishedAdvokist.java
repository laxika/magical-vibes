package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantTargetLandsEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerRestriction;
import com.github.laxika.magicalvibes.model.effect.PlayersCantCastSpellsFromZonesEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "WAR", collectorNumber = "34")
@CardRegistration(set = "RVR", collectorNumber = "31")
public class TomikDistinguishedAdvokist extends Card {

    public TomikDistinguishedAdvokist() {
        addStaticEffect(new OpponentsCantTargetLandsEffect());
        addStaticEffect(new PlayersCantCastSpellsFromZonesEffect(Subjects.eachOpponent(),
                PlayerRestriction.playingLands().from(Zone.GRAVEYARD)));
    }
}
