package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToCreaturesEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WTH", collectorNumber = "18")
public class InnerSanctum extends Card {

    public InnerSanctum() {
        // "Cumulative upkeep—Pay 2 life."
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), CumulativeUpkeepEffect.life(2));
        // "Prevent all damage that would be dealt to creatures you control."
        addStaticEffect(PreventDamageToCreaturesEffect.youControl(false));
    }
}
