package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilLandToBattlefieldRestToGraveyardEffect;

@CardRegistration(set = "EXO", collectorNumber = "105")
public class AvengingDruid extends Card {

    public AvengingDruid() {
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER,
                new MayEffect(new RevealUntilLandToBattlefieldRestToGraveyardEffect(),
                        "Reveal cards until you reveal a land card?"));
    }
}
