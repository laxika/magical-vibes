package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SkipKind;
import com.github.laxika.magicalvibes.model.effect.SkipNextEffect;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "35")
public class Avizoa extends Card {

    public Avizoa() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{0}",
                List.of(new BoostSelfEffect(2, 2), new SkipNextEffect(SkipKind.UNTAP_STEP)),
                "{0}: This creature gets +2/+2 until end of turn. You skip your next untap step. Activate only once each turn.",
                1));
    }
}
