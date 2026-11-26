package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "SHM", collectorNumber = "205")
@CardRegistration(set = "V14", collectorNumber = "6")
@CardRegistration(set = "CMD", collectorNumber = "199")
public class Firespout extends Card {

    public Firespout() {
        // 3 damage to each creature without flying if {R} was spent to cast this spell.

        // 3 damage to each creature with flying if {G} was spent to cast this spell.
        addSimultaneousSpellEffects(new ConditionalEffect(
                        new ColorSpentToCast(ManaColor.RED),
                        DealDamageEffect.toEachCreature(3,
                                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING)))),
                new ConditionalEffect(
                        new ColorSpentToCast(ManaColor.GREEN),
                        DealDamageEffect.toEachCreature(3, new PermanentHasKeywordPredicate(Keyword.FLYING))));
    }
}
