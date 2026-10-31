package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;

@CardRegistration(set = "FDN", collectorNumber = "79")
public class Boltwave extends Card {

    public Boltwave() {
        addSpellEffect(new DealDamageToPlayersEffect(3, DamageRecipient.EACH_OPPONENT));
    }
}
