package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SpellCastDamageToCasterEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "165")
@CardRegistration(set = "LCI", collectorNumber = "323")
@CardRegistration(set = "PIO", collectorNumber = "156")
public class ScytheclawRaptor extends Card {

    public ScytheclawRaptor() {
        addTrigger(Triggers.playerCastsSpell(),
                SpellCastDamageToCasterEffect.whenCasterIsNotActiveTurn(4));
    }
}
