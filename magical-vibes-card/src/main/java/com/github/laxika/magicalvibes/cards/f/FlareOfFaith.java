package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MID", collectorNumber = "19")
public class FlareOfFaith extends Card {

    public FlareOfFaith() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(2, 2));
        addEffect(EffectSlot.SPELL, GrantKeywordEffect.toTargetIf(
                Keyword.INDESTRUCTIBLE, new PermanentHasSubtypePredicate(CardSubtype.HUMAN)));
    }
}
