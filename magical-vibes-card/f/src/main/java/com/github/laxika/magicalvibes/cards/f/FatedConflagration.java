package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "BNG", collectorNumber = "94")
public class FatedConflagration extends Card {

    public FatedConflagration() {
        addSpellEffect(new DealDamageToTargetCreatureOrPlaneswalkerEffect(5));
        addSpellEffect(new ConditionalEffect(new ControllerTurn(), new ScryEffect(2)));
    }
}
