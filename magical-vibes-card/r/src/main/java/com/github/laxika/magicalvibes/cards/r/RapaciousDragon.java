package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M20", collectorNumber = "153")
@CardRegistration(set = "FDN", collectorNumber = "544")
@CardRegistration(set = "GN3", collectorNumber = "85")
@CardRegistration(set = "2XM", collectorNumber = "140")
@CardRegistration(set = "CMM", collectorNumber = "248")
@CardRegistration(set = "TDC", collectorNumber = "229")
@CardRegistration(set = "SCD", collectorNumber = "154")
@CardRegistration(set = "FDC", collectorNumber = "171")
public class RapaciousDragon extends Card {

    public RapaciousDragon() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.ofTreasureToken(2));
    }
}
