package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "35")
@CardRegistration(set = "MM2", collectorNumber = "49")
@CardRegistration(set = "HA2", collectorNumber = "6")
@CardRegistration(set = "PIP", collectorNumber = "177")
@CardRegistration(set = "PIP", collectorNumber = "460")
@CardRegistration(set = "PIP", collectorNumber = "705")
@CardRegistration(set = "PIP", collectorNumber = "988")
@CardRegistration(set = "FIC", collectorNumber = "265")
public class InexorableTide extends Card {

    public InexorableTide() {
        addTrigger(Triggers.youCastSpell(),
                new SpellCastTriggerEffect(null, List.of(new ProliferateEffect())));
    }
}
