package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "40K", collectorNumber = "13")
public class GreyKnightParagon extends Card {

    public GreyKnightParagon() {
        target(TargetFilters.attackingCreature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalReplacementEffect(
                        new TargetPermanentMatches(new PermanentHasSubtypePredicate(CardSubtype.DEMON)),
                        new DestroyTargetPermanentEffect(),
                        new ExileTargetPermanentEffect()));
    }
}
