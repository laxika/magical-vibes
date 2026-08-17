package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToDamageSourceControllerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "SOI", collectorNumber = "157")
public class FlamebladeAngel extends Card {

    public FlamebladeAngel() {
        addEffect(EffectSlot.ON_OPPONENT_SOURCE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT,
                new MayEffect(
                        new DealDamageToDamageSourceControllerEffect(1),
                        "Have Flameblade Angel deal 1 damage to that source's controller?"));
    }
}
