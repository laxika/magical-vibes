package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "ZEN", collectorNumber = "3")
public class BoldDefense extends Card {

    public BoldDefense() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{3}{W}"));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(new Kicked(),
                new BoostAllOwnCreaturesEffect(1, 1),
                SequenceEffect.of(
                        new BoostAllOwnCreaturesEffect(2, 2),
                        new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.OWN_CREATURES)
                )));
    }
}
