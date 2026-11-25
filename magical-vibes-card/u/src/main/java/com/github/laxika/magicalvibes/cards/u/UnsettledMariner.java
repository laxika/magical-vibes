package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH1", collectorNumber = "216")
public class UnsettledMariner extends Card {

    public UnsettledMariner() {
        addTrigger(Triggers.youOrPermanentYouControlBecomesTargetOfOpponentSpellOrAbility(),
                UnlessPaysEffect.counterUnlessPays(1));
    }
}
