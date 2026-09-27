package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayCastArtifactFromHandOrGraveyardByPayingLifeEqualToManaValueEffect;

@CardRegistration(set = "40K", collectorNumber = "28")
public class AnrakyrTheTraveller extends Card {

    public AnrakyrTheTraveller() {
        addEffect(EffectSlot.ON_ATTACK,
                new MayCastArtifactFromHandOrGraveyardByPayingLifeEqualToManaValueEffect());
    }
}
