package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayCastArtifactFromHandOrGraveyardByPayingLifeEqualToManaValueEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "40K", collectorNumber = "28")
public class AnrakyrTheTraveller extends Card {

    public AnrakyrTheTraveller() {
        addTrigger(Triggers.attacks(),
                new MayCastArtifactFromHandOrGraveyardByPayingLifeEqualToManaValueEffect());
    }
}
