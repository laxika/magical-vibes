package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfControlsPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "DOM", collectorNumber = "75")
public class WizardsRetort extends Card {

    public WizardsRetort() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostIfControlsPermanentEffect(
                new PermanentHasSubtypePredicate(CardSubtype.WIZARD), 1));
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
