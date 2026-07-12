package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "SHM", collectorNumber = "97")
public class JawsOfStone extends Card {

    public JawsOfStone() {
        // Jaws of Stone deals X damage divided as you choose among any number of targets,
        // where X is the number of Mountains you control as you cast this spell.
        addEffect(EffectSlot.SPELL, DealDividedDamageEffect.chosenAmongAnyTargets(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN), CountScope.CONTROLLER)));
    }
}
