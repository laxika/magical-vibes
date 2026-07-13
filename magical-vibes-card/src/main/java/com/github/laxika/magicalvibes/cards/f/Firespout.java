package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "SHM", collectorNumber = "205")
public class Firespout extends Card {

    public Firespout() {
        // 3 damage to each creature without flying if {R} was spent to cast this spell.
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ColorSpentToCast(ManaColor.RED),
                new MassDamageEffect(new Fixed(3), false, false,
                        new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING)))));

        // 3 damage to each creature with flying if {G} was spent to cast this spell.
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ColorSpentToCast(ManaColor.GREEN),
                new MassDamageEffect(new Fixed(3), false, false,
                        new PermanentHasKeywordPredicate(Keyword.FLYING))));
    }
}
