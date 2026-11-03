package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EXO", collectorNumber = "104")
@CardRegistration(set = "TPR", collectorNumber = "160")
public class Spellshock extends Card {

    public Spellshock() {
        addTrigger(Triggers.playerCastsSpell().anySpell(),
                new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PLAYER));
    }
}
