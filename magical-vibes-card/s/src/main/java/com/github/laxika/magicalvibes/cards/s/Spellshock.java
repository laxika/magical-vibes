package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EXO", collectorNumber = "104")
@CardRegistration(set = "TPR", collectorNumber = "160")
public class Spellshock extends Card {

    public Spellshock() {
        addTrigger(Triggers.playerCastsSpell().anySpell(),
                DealDamageEffect.to(Subjects.triggeringPlayer(), 2));
    }
}
