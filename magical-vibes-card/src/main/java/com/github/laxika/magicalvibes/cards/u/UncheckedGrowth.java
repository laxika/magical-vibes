package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "BOK", collectorNumber = "148")
public class UncheckedGrowth extends Card {

    public UncheckedGrowth() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(4, 4));
        addEffect(EffectSlot.SPELL, GrantKeywordEffect.toTargetIf(
                Keyword.TRAMPLE, new PermanentHasSubtypePredicate(CardSubtype.SPIRIT)));
    }
}
