package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "DSK", collectorNumber = "90")
public class DashingBloodsucker extends Card {

    public DashingBloodsucker() {
        SequenceEffect eerie = SequenceEffect.of(
                new BoostSelfEffect(2, 0),
                new GrantKeywordEffect(Keyword.LIFELINK, GrantScope.SELF));
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD, eerie);
        addEffect(EffectSlot.ON_ALLY_ROOM_FULLY_UNLOCKED, eerie);
    }
}
