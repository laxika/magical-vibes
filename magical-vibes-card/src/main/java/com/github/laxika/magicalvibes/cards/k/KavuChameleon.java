package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.SetChosenColorUntilEndOfTurnEffect;

import java.util.List;

@CardRegistration(set = "INV", collectorNumber = "191")
public class KavuChameleon extends Card {

    public KavuChameleon() {
        addEffect(EffectSlot.STATIC, new CantBeCounteredEffect());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(new SetChosenColorUntilEndOfTurnEffect(false, false)),
                "{G}: This creature becomes the color of your choice until end of turn."
        ));
    }
}
