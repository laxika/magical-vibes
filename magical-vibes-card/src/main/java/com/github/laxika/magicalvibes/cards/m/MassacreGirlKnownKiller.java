package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtMostPredicate;

@CardRegistration(set = "MKM", collectorNumber = "94")
public class MassacreGirlKnownKiller extends Card {

    public MassacreGirlKnownKiller() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.WITHER, GrantScope.ALL_OWN_CREATURES));
        addEffect(EffectSlot.ON_OPPONENT_CREATURE_DIES, new TriggeringPermanentConditionalEffect(
                new PermanentToughnessAtMostPredicate(0), new DrawCardEffect(1)));
    }
}
