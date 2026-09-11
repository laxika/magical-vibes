package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.condition.OpponentGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ZNR", collectorNumber = "236")
public class SoaringThoughtThief extends Card {

    public SoaringThoughtThief() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new OpponentGraveyardAtLeast(8),
                new StaticBoostEffect(1, 0, GrantScope.ALL_OWN_CREATURES,
                        new PermanentHasSubtypePredicate(CardSubtype.ROGUE))));
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new ConditionalEffect(
                new HasAttacker(new PermanentHasSubtypePredicate(CardSubtype.ROGUE)),
                new MillEffect(2, MillRecipient.EACH_OPPONENT)));
    }
}
