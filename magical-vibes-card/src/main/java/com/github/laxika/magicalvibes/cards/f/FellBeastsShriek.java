package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentChoosesCreatureToTapAndGoadEffect;
import com.github.laxika.magicalvibes.model.effect.SpliceEffect;

@CardRegistration(set = "LTC", collectorNumber = "508")
@CardRegistration(set = "LTC", collectorNumber = "552")
public class FellBeastsShriek extends Card {

    public FellBeastsShriek() {
        addEffect(EffectSlot.SPELL, new EachOpponentChoosesCreatureToTapAndGoadEffect());
        addEffect(EffectSlot.STATIC, SpliceEffect.ontoInstantOrSorcery("{2}{U}{R}"));
    }
}
