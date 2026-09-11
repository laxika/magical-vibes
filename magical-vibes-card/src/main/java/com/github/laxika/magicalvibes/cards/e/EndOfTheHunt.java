package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerExilesGreatestManaValueCreatureOrPlaneswalkerEffect;

@CardRegistration(set = "SOS", collectorNumber = "81")
public class EndOfTheHunt extends Card {

    public EndOfTheHunt() {
        addEffect(EffectSlot.SPELL, new TargetPlayerExilesGreatestManaValueCreatureOrPlaneswalkerEffect());
    }
}
