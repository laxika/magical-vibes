package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "ISD", collectorNumber = "217")
public class ButchersCleaver extends Card {

    public ButchersCleaver() {
        addEffect(EffectSlot.STATIC, new BoostAttachedCreatureEffect(3, 0));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.EQUIPPED_CREATURE, new PermanentHasSubtypePredicate(CardSubtype.HUMAN)));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
