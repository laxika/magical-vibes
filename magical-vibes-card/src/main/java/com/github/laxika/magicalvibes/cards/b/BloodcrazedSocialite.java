package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentAndBoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "96")
@CardRegistration(set = "DBL", collectorNumber = "363")
public class BloodcrazedSocialite extends Card {

    public BloodcrazedSocialite() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofBloodToken(1));
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                new SacrificePermanentAndBoostSelfEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.BLOOD),
                                new PermanentIsTokenPredicate())),
                        2,
                        2,
                        "a Blood token"),
                "Sacrifice a Blood token?"));
    }
}
