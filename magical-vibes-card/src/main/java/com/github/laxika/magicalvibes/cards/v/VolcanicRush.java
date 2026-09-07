package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "DTK", collectorNumber = "166")
public class VolcanicRush extends Card {

    public VolcanicRush() {
        PermanentIsAttackingPredicate attacking = new PermanentIsAttackingPredicate();
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(2, 0, attacking));
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.ALL_CREATURES, attacking));
    }
}
