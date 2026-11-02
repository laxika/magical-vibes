package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTJ", collectorNumber = "239")
public class WylieDukeAtiinHero extends Card {

    public WylieDukeAtiinHero() {
        addTrigger(Triggers.permanentYouControlBecomesTapped(),
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsSourceCardPredicate(),
                        SequenceEffect.of(
                                new GainLifeEffect(1),
                                new DrawCardEffect(1))));
    }
}
