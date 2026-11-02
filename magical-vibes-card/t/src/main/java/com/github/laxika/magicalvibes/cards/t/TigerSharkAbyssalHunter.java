package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "747")
public class TigerSharkAbyssalHunter extends Card {

    public TigerSharkAbyssalHunter() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawDiscardAndConniveEffect());
        addEffect(EffectSlot.ON_ATTACK, new DrawDiscardAndConniveEffect());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{U/B}",
                List.of(new MakeCreatureUnblockableEffect(true)),
                "{4}{U/B}: Tiger Shark can't be blocked this turn."
        ));
    }
}
