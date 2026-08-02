package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.Set;

@CardRegistration(set = "M15", collectorNumber = "128")
public class AltacBloodseeker extends Card {

    public AltacBloodseeker() {
        addEffect(EffectSlot.ON_OPPONENT_CREATURE_DIES, new BoostSelfEffect(2, 0));
        addEffect(EffectSlot.ON_OPPONENT_CREATURE_DIES,
                new GrantKeywordEffect(Set.of(Keyword.FIRST_STRIKE, Keyword.HASTE), GrantScope.SELF));
    }
}
