package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestDreadEffect;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "77")
public class TwistReality extends Card {

    public TwistReality() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Counter target spell", new CounterSpellEffect()),
                new ChooseOneEffect.ChooseOneOption("Manifest dread", ManifestDreadEffect.forController())
        )));
    }
}
