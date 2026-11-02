package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "220")
public class JunkbladeBruiser extends Card {

    public JunkbladeBruiser() {
        addTrigger(Triggers.youCastSpell(),
                SpellCastTriggerEffect.wheneverYouExpend(4, List.of(new BoostSelfEffect(2, 1))));
    }
}
