package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DestroyDamageSourcePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.Set;

@CardRegistration(set = "DKA", collectorNumber = "70")
public class MikaeusTheUnhallowed extends Card {

    public MikaeusTheUnhallowed() {
        addEffect(EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU,
                new DestroyDamageSourcePermanentEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.HUMAN)));

        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                1, 1, Set.of(Keyword.UNDYING), GrantScope.OWN_CREATURES,
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.HUMAN))));
    }
}
