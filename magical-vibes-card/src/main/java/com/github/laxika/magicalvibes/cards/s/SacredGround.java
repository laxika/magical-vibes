package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringLandFromGraveyardToBattlefieldEffect;

@CardRegistration(set = "9ED", collectorNumber = "37")
@CardRegistration(set = "8ED", collectorNumber = "39")
public class SacredGround extends Card {

    public SacredGround() {
        // Whenever a spell or ability an opponent controls causes a land to be put into your
        // graveyard from the battlefield, return that card to the battlefield. The trigger collector
        // stamps the concrete land card id at collection time.
        addEffect(EffectSlot.ON_ALLY_LAND_PUT_INTO_GRAVEYARD_BY_OPPONENT,
                new ReturnTriggeringLandFromGraveyardToBattlefieldEffect(null));
    }
}
