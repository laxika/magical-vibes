package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.condition.SourceUntapped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "WAR", collectorNumber = "171")
public class ParadiseDruid extends Card {

    public ParadiseDruid() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new SourceUntapped(),
                new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF)));
        addActivatedAbility(ManaAbilities.tapForAnyColor());
    }
}
