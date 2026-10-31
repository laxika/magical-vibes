package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerOrControllerHasMoreLifeThanController;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "DST", collectorNumber = "66")
public class PulseOfTheForge extends Card {

    public PulseOfTheForge() {
        addSpellEffect(new DealDamageToTargetPlayerOrPlaneswalkerEffect(4));
        addSpellEffect(new ConditionalEffect(
                new TargetPlayerOrControllerHasMoreLifeThanController(), ReturnToHandEffect.selfSpell()));
    }
}
