package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;

@CardRegistration(set = "RNA", collectorNumber = "241")
public class SphinxOfTheGuildpact extends Card {

    public SphinxOfTheGuildpact() {
        removeKeyword(Keyword.HEXPROOF);
        addEffect(EffectSlot.STATIC, TargetingRestrictionEffect.hexproofFromMonocolored());
        addEffect(EffectSlot.STATIC, new GrantColorEffect(CardColor.WHITE, GrantScope.SELF));
        addEffect(EffectSlot.STATIC, new GrantColorEffect(CardColor.BLUE, GrantScope.SELF));
        addEffect(EffectSlot.STATIC, new GrantColorEffect(CardColor.BLACK, GrantScope.SELF));
        addEffect(EffectSlot.STATIC, new GrantColorEffect(CardColor.RED, GrantScope.SELF));
        addEffect(EffectSlot.STATIC, new GrantColorEffect(CardColor.GREEN, GrantScope.SELF));
    }
}
