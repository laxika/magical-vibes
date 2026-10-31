package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "M12", collectorNumber = "113")
public class TasteOfBlood extends Card {

    public TasteOfBlood() {
        // "Taste of Blood deals 1 damage to target player or planeswalker." (target auto-derived)
        addSpellEffect(new DealDamageToTargetPlayerOrPlaneswalkerEffect(1));
        // "and you gain 1 life."
        addSpellEffect(new GainLifeEffect(1));
    }
}
