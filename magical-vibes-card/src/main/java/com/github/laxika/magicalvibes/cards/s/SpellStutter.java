package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "WOE", collectorNumber = "69")
public class SpellStutter extends Card {

    public SpellStutter() {
        // Counter target spell unless its controller pays {2} plus {1} for each Faerie you control.
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(new Sum(
                new Fixed(2),
                new PermanentCount(
                        new PermanentHasSubtypePredicate(CardSubtype.FAERIE), CountScope.CONTROLLER))));
    }
}
