package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;


@CardRegistration(set = "ONS", collectorNumber = "138")
@CardRegistration(set = "PHUK", collectorNumber = "18")
@CardRegistration(set = "A25", collectorNumber = "86")
@CardRegistration(set = "C13", collectorNumber = "73")
public class DirgeOfDread extends Card {

    public DirgeOfDread() {
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.FEAR, GrantScope.ALL_CREATURES));

        addCycling("{1}{B}");
        addEffect(EffectSlot.ON_SELF_CYCLED, new MayEffect(
                new GrantKeywordEffect(Keyword.FEAR, GrantScope.TARGET),
                "Have target creature gain fear until end of turn?"));
    }
}
