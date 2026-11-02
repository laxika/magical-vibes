package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GRN", collectorNumber = "48")
@CardRegistration(set = "RVR", collectorNumber = "52")
@CardRegistration(set = "MKC", collectorNumber = "113")
@CardRegistration(set = "WOC", collectorNumber = "100")
@CardRegistration(set = "ZNC", collectorNumber = "32")
public class NightveilSprite extends Card {

    public NightveilSprite() {
        addTrigger(Triggers.attacks(), new SurveilEffect(1));
    }
}
