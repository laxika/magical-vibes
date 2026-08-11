package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AttackedWithCreaturesOfSubtypeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ECL", collectorNumber = "214")
public class DeepwayNavigator extends Card {

    public DeepwayNavigator() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new UntapPermanentsEffect(
                TapUntapScope.OTHER_CONTROLLED_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.MERFOLK)));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new AttackedWithCreaturesOfSubtypeThisTurn(3, CardSubtype.MERFOLK),
                new StaticBoostEffect(1, 0, GrantScope.OWN_CREATURES,
                        new PermanentHasSubtypePredicate(CardSubtype.MERFOLK))));
    }
}
