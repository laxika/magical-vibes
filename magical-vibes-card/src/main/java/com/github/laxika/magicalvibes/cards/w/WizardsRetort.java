package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfControlsSubtypeEffect;

@CardRegistration(set = "DOM", collectorNumber = "75")
public class WizardsRetort extends Card {

    public WizardsRetort() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostIfControlsSubtypeEffect(CardSubtype.WIZARD, 1));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
