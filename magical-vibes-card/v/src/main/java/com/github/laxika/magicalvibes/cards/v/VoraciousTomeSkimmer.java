package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ECL", collectorNumber = "250")
public class VoraciousTomeSkimmer extends Card {

    public VoraciousTomeSkimmer() {
        addTrigger(Triggers.youCastSpell(),
                new SpellCastTriggerEffect(null, List.of(
                        new MayPayManaEffect("{0}", 1, new DrawCardEffect(1), "Pay 1 life?")), true));
    }
}
