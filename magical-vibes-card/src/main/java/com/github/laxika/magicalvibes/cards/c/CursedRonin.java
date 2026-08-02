package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BushidoEffect;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "107")
public class CursedRonin extends Card {

    public CursedRonin() {
        addEffect(EffectSlot.ON_BLOCK, new BushidoEffect(1));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BushidoEffect(1));
        addActivatedAbility(new ActivatedAbility(false, "{B}", List.of(new BoostSelfEffect(1, 1)), "{B}: This creature gets +1/+1 until end of turn."));
    }
}
