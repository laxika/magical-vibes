package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachOpponentChoosesCreatureToTapAndGoadEffect;
import com.github.laxika.magicalvibes.model.effect.SpliceEffect;

@CardRegistration(set = "LTC", collectorNumber = "508")
@CardRegistration(set = "LTC", collectorNumber = "552")
public class FellBeastsShriek extends Card {

    public FellBeastsShriek() {
        addSpellEffect(new EachOpponentChoosesCreatureToTapAndGoadEffect());
        addStaticEffect(SpliceEffect.ontoInstantOrSorcery("{2}{U}{R}"));
    }
}
