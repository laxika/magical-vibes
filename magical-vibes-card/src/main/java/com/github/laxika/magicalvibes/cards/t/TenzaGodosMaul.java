package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

import java.util.Set;

/**
 * The legendary and red bonuses are independent conditional statics on top of the flat +1/+1, so a
 * legendary red creature gets +3/+3 and trample.
 */
@CardRegistration(set = "CHK", collectorNumber = "271")
public class TenzaGodosMaul extends Card {

    public TenzaGodosMaul() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                2, 2, GrantScope.EQUIPPED_CREATURE, new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.TRAMPLE, GrantScope.EQUIPPED_CREATURE, new PermanentColorInPredicate(Set.of(CardColor.RED))));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
