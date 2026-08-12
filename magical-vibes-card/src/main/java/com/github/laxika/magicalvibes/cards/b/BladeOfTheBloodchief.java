package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.condition.EnchantedPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ZEN", collectorNumber = "196")
public class BladeOfTheBloodchief extends Card {

    public BladeOfTheBloodchief() {
        PermanentHasSubtypePredicate vampire = new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE);

        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new ConditionalReplacementEffect(
                new EnchantedPermanentMatches(vampire, "equipped creature is a Vampire"),
                new PutCounterOnReferencedPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                new PutCounterOnReferencedPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)));

        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
