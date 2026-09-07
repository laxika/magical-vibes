package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentAndBoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "VOW", collectorNumber = "96")
public class BloodcrazedSocialite extends Card {

    public BloodcrazedSocialite() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofBloodToken(1));
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new SacrificePermanentAndBoostSelfEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.BLOOD),
                        2,
                        2,
                        "a Blood token"),
                "Sacrifice a Blood token?"));
    }
}
