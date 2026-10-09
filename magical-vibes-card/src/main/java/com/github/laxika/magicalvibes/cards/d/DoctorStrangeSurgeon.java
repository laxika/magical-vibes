package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControllerLifeAtLeast;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleLifeGainEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "MSC", collectorNumber = "591")
public class DoctorStrangeSurgeon extends Card {

    public DoctorStrangeSurgeon() {
        addEffect(EffectSlot.STATIC, new DoubleLifeGainEffect());
        addEffect(EffectSlot.EACH_BEGINNING_OF_COMBAT_TRIGGERED,
                new ConditionalEffect(
                        new ControllerLifeAtLeast(10, true),
                        SequenceEffect.of(
                                new BoostAllOwnCreaturesEffect(2, 2),
                                new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.ALL_OWN_CREATURES))));
    }
}
