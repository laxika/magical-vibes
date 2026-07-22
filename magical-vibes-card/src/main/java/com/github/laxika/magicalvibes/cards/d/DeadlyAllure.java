package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleThisTurnEffect;

@CardRegistration(set = "DKA", collectorNumber = "58")
@CardRegistration(set = "INR", collectorNumber = "103")
public class DeadlyAllure extends Card {

    public DeadlyAllure() {
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.TARGET));
        addEffect(EffectSlot.SPELL, new MustBeBlockedIfAbleThisTurnEffect());
        addCastingOption(new FlashbackCast("{G}"));
    }
}
