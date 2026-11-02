package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "77")
public class TanufelRimespeaker extends Card {

    public TanufelRimespeaker() {
        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                new CardMinManaValuePredicate(4),
                List.of(new DrawCardEffect())
        ));
    }
}
