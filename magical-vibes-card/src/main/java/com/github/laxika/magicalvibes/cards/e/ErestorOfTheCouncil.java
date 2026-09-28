package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ErestorOfTheCouncilEffect;

@CardRegistration(set = "LTC", collectorNumber = "53")
@CardRegistration(set = "LTC", collectorNumber = "136")
public class ErestorOfTheCouncil extends Card {

    public ErestorOfTheCouncil() {
        addEffect(EffectSlot.ON_PLAYERS_FINISH_VOTING, new ErestorOfTheCouncilEffect());
    }
}
